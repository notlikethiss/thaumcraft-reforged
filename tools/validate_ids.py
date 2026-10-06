import argparse
import gzip
import hashlib
import io
import json
import re
import struct
import subprocess
import sys
import zipfile
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import mcformat

ROOT = Path(__file__).resolve().parent.parent
CACHE = ROOT / "tools" / "cache"
MANIFEST_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
JAVA = "/opt/homebrew/opt/openjdk@25/bin/java"
ASPECTS = ROOT / "src" / "main" / "java" / "thaumcraft" / "aspect" / "ConfigAspects.java"
ID_PATTERN = re.compile(r'"(#?minecraft:[a-z0-9_./]+)"')
LOOT_TYPES = ["loot_condition_type", "loot_function_type", "loot_pool_entry_type", "loot_number_provider_type", "enchantment_level_based_value_type"]
BONUS_FORMULAS = {"minecraft:uniform_bonus_count", "minecraft:binomial_with_bonus_count", "minecraft:ore_drops"}
BUILTIN_MODELS = {"builtin/entity", "builtin/generated"}


def curl(url):
    return subprocess.run(["curl", "-fsSL", url], check=True, capture_output=True).stdout


def download(url, target, sha1):
    data = curl(url)
    if hashlib.sha1(data).hexdigest() != sha1:
        raise RuntimeError(f"sha1 mismatch for {url}")
    target.write_bytes(data)


def fetch(version):
    directory = CACHE / version
    server = directory / "server.jar"
    client = directory / "client.jar"
    if server.exists() and client.exists():
        return directory
    directory.mkdir(parents=True, exist_ok=True)
    manifest = json.loads(curl(MANIFEST_URL))
    entry = next(entry for entry in manifest["versions"] if entry["id"] == version)
    package = json.loads(curl(entry["url"]))
    for kind, target in [("server", server), ("client", client)]:
        download(package["downloads"][kind]["url"], target, package["downloads"][kind]["sha1"])
    return directory


def run_reports(directory):
    reports = directory / "reports"
    registries = reports / "reports" / "registries.json"
    if not registries.exists():
        reports.mkdir(exist_ok=True)
        command = [JAVA, "-DbundlerMainClass=net.minecraft.data.Main", "-jar", "server.jar", "--reports", "--output", "reports"]
        result = subprocess.run(command, cwd=directory, capture_output=True, text=True)
        if result.returncode != 0 or not registries.exists():
            print(result.stdout[-2000:])
            print(result.stderr[-2000:])
            raise SystemExit("vanilla data generator failed")
    return json.loads(registries.read_text(encoding="utf-8"))


def jar_names(path):
    with zipfile.ZipFile(path) as archive:
        names = set(archive.namelist())
        for name in list(names):
            if name.startswith("META-INF/versions/") and name.endswith(".jar"):
                with zipfile.ZipFile(io.BytesIO(archive.read(name))) as nested:
                    names |= set(nested.namelist())
    return names


def datapack_ids(names, folders):
    result = set()
    for folder in folders:
        prefix = f"data/minecraft/{folder}/"
        result |= {"minecraft:" + name[len(prefix):-5] for name in names if name.startswith(prefix) and name.endswith(".json")}
    return result


def read_nbt(data):
    stream = io.BytesIO(gzip.decompress(data))

    def read(fmt):
        size = struct.calcsize(fmt)
        return struct.unpack(fmt, stream.read(size))[0]

    def read_string():
        return stream.read(read(">H")).decode("utf-8")

    def payload(kind):
        if kind == 1:
            return read(">b")
        if kind == 2:
            return read(">h")
        if kind == 3:
            return read(">i")
        if kind == 4:
            return read(">q")
        if kind == 5:
            return read(">f")
        if kind == 6:
            return read(">d")
        if kind == 7:
            return [read(">b") for _ in range(read(">i"))]
        if kind == 8:
            return read_string()
        if kind == 9:
            item_kind = read(">b")
            return [payload(item_kind) for _ in range(read(">i"))]
        if kind == 10:
            result = {}
            while True:
                child = read(">b")
                if child == 0:
                    return result
                name = read_string()
                result[name] = payload(child)
        if kind == 11:
            return [read(">i") for _ in range(read(">i"))]
        if kind == 12:
            return [read(">q") for _ in range(read(">i"))]
        raise ValueError(kind)

    stream.read(1)
    read_string()
    return payload(10)


class Validator:
    def __init__(self, version, out):
        self.version = version
        self.out = out
        self.problems = []
        directory = fetch(version)
        registries = run_reports(directory)
        self.registry = {name.split(":", 1)[1]: set(content["entries"]) for name, content in registries.items()}
        server = jar_names(directory / "server.jar")
        client = jar_names(directory / "client.jar")
        self.models = {name[len("assets/minecraft/models/"):-5] for name in client if name.startswith("assets/minecraft/models/") and name.endswith(".json")}
        self.textures = {name[len("assets/minecraft/textures/"):-4] for name in client if name.startswith("assets/minecraft/textures/") and name.endswith(".png")}
        profile = mcformat.PROFILES[version]
        self.loot_tables = datapack_ids(server, [profile.loot_folder])
        self.enchantments = datapack_ids(server, ["enchantment"]) | self.registry.get("enchantment", set())
        self.tags = {}
        for kind, folder in [("block", profile.block_tag_folder), ("item", profile.item_tag_folder), ("biome", "tags/worldgen/biome")]:
            self.tags[kind] = {"#" + tag for tag in datapack_ids(server, [folder])}
        for kind, folder in [("block", profile.block_tag_folder), ("item", profile.item_tag_folder)]:
            minecraft = out / "data" / "minecraft" / folder
            if minecraft.exists():
                for path in minecraft.rglob("*.json"):
                    tag = "#minecraft:" + path.relative_to(minecraft).with_suffix("").as_posix()
                    self.tags[kind].add(tag)
        self.loot_registry = set()
        for name in LOOT_TYPES:
            self.loot_registry |= self.registry.get(name, set())
        self.flat = set()
        for entries in self.registry.values():
            self.flat |= entries
        self.flat |= self.loot_tables | self.enchantments

    def report(self, source, value, expected):
        self.problems.append((source, value, expected))

    def check(self, source, value, kind):
        if kind == "item":
            known = value in self.registry["item"]
        elif kind == "block":
            known = value in self.registry["block"]
        elif kind == "entity":
            known = value in self.registry["entity_type"]
        elif kind == "block_entity":
            known = value in self.registry["block_entity_type"]
        elif kind == "loot_table":
            known = value in self.loot_tables
        elif kind == "enchantment":
            known = value in self.enchantments
        elif kind == "model":
            known = value.split(":", 1)[1] in self.models | BUILTIN_MODELS
        elif kind == "texture":
            known = value.split(":", 1)[1] in self.textures
        elif kind == "skip":
            known = True
        elif kind == "loot_registry":
            known = value in self.loot_registry
        elif kind.startswith("tag:"):
            known = value in self.tags[kind[4:]]
        else:
            known = value in self.flat
        if not known:
            self.report(source, value, kind)

    def walk(self, source, value, keys, file_kind):
        if isinstance(value, str):
            if keys and keys[-1] == "tag" and file_kind in ("recipe", "other"):
                value = "#" + value
            if value.startswith("minecraft:") or value.startswith("#minecraft:"):
                self.check(source, value, self.classify(value, keys, file_kind))
            return
        if isinstance(value, list):
            for entry in value:
                self.walk(source, entry, keys, file_kind)
        elif isinstance(value, dict):
            for key, entry in value.items():
                if key == "name" and value.get("type") == "minecraft:loot_table":
                    key = "value"
                self.walk(source, entry, keys + [key], file_kind)

    def classify(self, value, keys, file_kind):
        key = keys[-1] if keys else ""
        if value.startswith("#"):
            if file_kind == "block_tag" or key in ("blocks", "block"):
                return "tag:block"
            if "biomes" in keys:
                return "tag:biome"
            return "tag:item"
        if file_kind == "recipe":
            return "skip" if key == "type" else "item"
        if file_kind == "item_tag":
            return "item"
        if file_kind == "block_tag":
            return "block"
        if file_kind == "model":
            if "textures" in keys:
                return "texture"
            if key in ("parent", "model"):
                return "model"
            return "flat"
        if file_kind == "loot":
            if key == "name":
                return "item"
            if key == "value":
                return "loot_table"
            if key in ("condition", "function"):
                return "loot_registry"
            if key == "enchantment":
                return "enchantment"
            if key in ("blocks", "block"):
                return "block"
            if key == "formula":
                return "skip" if value in BONUS_FORMULAS else "flat"
            if key == "type" and len(keys) > 1:
                return "loot_registry"
            if key == "type":
                return "skip"
            return "flat"
        if key in ("enchantment", "exclusive_set") and file_kind != "recipe":
            return "enchantment"
        return "flat"

    def check_json(self, path):
        relative = path.relative_to(self.out).as_posix()
        profile = mcformat.PROFILES[self.version]
        if "/" + profile.recipe_folder + "/" in "/" + relative:
            file_kind = "recipe"
        elif "/" + profile.block_tag_folder + "/" in "/" + relative:
            file_kind = "block_tag"
        elif "/" + profile.item_tag_folder + "/" in "/" + relative:
            file_kind = "item_tag"
        elif "/" + profile.loot_folder + "/" in "/" + relative:
            file_kind = "loot"
        elif relative.startswith("assets/") and ("/models/" in relative or "/blockstates/" in relative):
            file_kind = "model"
        else:
            file_kind = "other"
        content = json.loads(path.read_text(encoding="utf-8"))
        self.walk(relative, content, [], file_kind)

    def check_structure(self, path):
        relative = path.relative_to(self.out).as_posix()
        root = read_nbt(path.read_bytes())
        for entry in root["palette"]:
            if entry["id"].startswith("minecraft:"):
                self.check(relative, entry["id"], "block")
        for entry in root["blocks"]:
            nbt = entry.get("nbt")
            if nbt and nbt["id"].startswith("minecraft:"):
                self.check(relative, nbt["id"], "block_entity")
        for entry in root["entities"]:
            nbt = entry["nbt"]
            self.check(relative, nbt["id"], "entity")
            villager_type = nbt.get("VillagerData", {}).get("type")
            if villager_type and villager_type not in self.registry["villager_type"]:
                self.report(relative, villager_type, "villager_type")
        if root["DataVersion"] != mcformat.PROFILES[self.version].data_version:
            self.report(relative, str(root["DataVersion"]), "DataVersion")

    def check_aspects(self, path):
        text = path.read_text(encoding="utf-8")
        for value in ID_PATTERN.findall(text):
            self.check(path.name, value, "tag:item" if value.startswith("#") else "item")

    def run(self, aspects):
        for path in sorted(self.out.rglob("*.json")):
            self.check_json(path)
        for path in sorted(self.out.rglob("*.nbt")):
            self.check_structure(path)
        if aspects:
            self.check_aspects(aspects)
        return self.problems


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--aspects", help="ConfigAspects.java to check (default: the committed one)")
    parser.add_argument("--no-aspects", action="store_true")
    parser.add_argument("--fetch-only", action="store_true")
    profile = mcformat.init(parser=parser)
    if profile.modern:
        raise SystemExit("validation needs a vanilla server jar, use --mc 1.21.1 or --mc 1.19.2")
    args, _ = parser.parse_known_args()
    if args.fetch_only:
        print(fetch(profile.name))
        return
    aspects = None if args.no_aspects else Path(args.aspects) if args.aspects else ASPECTS
    problems = Validator(profile.name, mcformat.out_root).run(aspects)
    for source, value, expected in problems:
        print(f"{source}: {value} (expected {expected})")
    print(f"{profile.name}: {len(problems)} problem(s)")
    sys.exit(1 if problems else 0)


if __name__ == "__main__":
    main()

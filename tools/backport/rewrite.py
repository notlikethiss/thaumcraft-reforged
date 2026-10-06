#!/usr/bin/env python3
import argparse
import re
import sys
from pathlib import Path

STRING_PATTERN = re.compile(
    r'"""(?:\\.|[^\\])*?"""|"(?:\\.|[^"\\\n])*"|\'(?:\\.|[^\'\\\n])\'|//[^\n]*|/\*.*?\*/',
    re.S,
)
PLACEHOLDER = re.compile(r"\x00(\d+)\x00")


def mask(text):
    literals = []

    def replace(match):
        literals.append(match.group(0))
        return "\x00" + str(len(literals) - 1) + "\x00"

    return STRING_PATTERN.sub(replace, text), literals


def unmask(text, literals):
    return PLACEHOLDER.sub(lambda match: literals[int(match.group(1))], text)


def word(name):
    return r"(?<![\w.$])" + re.escape(name) + r"(?![\w$])"


def qualified(name):
    return r"(?<![\w$])" + re.escape(name) + r"(?![\w$])"


def split_arguments(text, start):
    depth = 0
    arguments = []
    current = start
    index = start
    while index < len(text):
        char = text[index]
        if char in "([{":
            depth += 1
        elif char in ")]}":
            if depth == 0:
                arguments.append(text[current:index])
                return arguments, index
            depth -= 1
        elif char == "," and depth == 0:
            arguments.append(text[current:index])
            current = index + 1
        elif char == ";" and depth == 0:
            return None, index
        index += 1
    return None, index


def rewrite_calls(text, pattern, builder):
    regex = re.compile(pattern)
    result = []
    position = 0
    while True:
        match = regex.search(text, position)
        if not match:
            result.append(text[position:])
            break
        arguments, end = split_arguments(text, match.end())
        if arguments is None:
            result.append(text[position:match.end()])
            position = match.end()
            continue
        replacement = builder(match, arguments)
        if replacement is None:
            result.append(text[position:match.end()])
            position = match.end()
            continue
        result.append(text[position:match.start()])
        result.append(replacement)
        position = end + 1
    return "".join(result)


def drop_first_argument(match, arguments):
    if len(arguments) != 3:
        return None
    return match.group(1) + "hurt(" + arguments[1].strip() + ", " + arguments[2].strip() + ")"


def hurt_declaration(text):
    return re.sub(
        r"\bhurtServer\(\s*ServerLevel\s+\w+\s*,\s*DamageSource\s+(\w+)\s*,\s*float\s+(\w+)\s*\)",
        r"hurt(DamageSource \1, float \2)",
        text,
    )


def rename_all(mapping):
    def apply(text):
        for old, new in mapping.items():
            text = re.sub(word(old), new, text)
        return text

    return apply


def rename_qualified(mapping):
    def apply(text):
        for old, new in mapping.items():
            text = re.sub(qualified(old), new, text)
        return text

    return apply


def rename_members(owner, mapping):
    def apply(text):
        for old, new in mapping.items():
            text = re.sub(r"(?<![\w$])" + re.escape(owner) + r"\." + re.escape(old) + r"(?![\w$])", owner + "." + new, text)
        return text

    return apply


def regex_rules(pairs):
    def apply(text):
        for pattern, replacement in pairs:
            text = re.sub(pattern, replacement, text)
        return text

    return apply


def calls(pattern, builder):
    return lambda text: rewrite_calls(text, pattern, builder)


PACKAGE_MOVES_1_21_1 = {
    "net.minecraft.world.entity.animal.golem.AbstractGolem": "net.minecraft.world.entity.animal.AbstractGolem",
    "net.minecraft.world.entity.animal.golem.IronGolem": "net.minecraft.world.entity.animal.IronGolem",
    "net.minecraft.world.entity.animal.golem.SnowGolem": "net.minecraft.world.entity.animal.SnowGolem",
    "net.minecraft.world.entity.animal.sheep.Sheep": "net.minecraft.world.entity.animal.Sheep",
    "net.minecraft.world.entity.monster.spider.": "net.minecraft.world.entity.monster.",
    "net.minecraft.world.entity.monster.zombie.": "net.minecraft.world.entity.monster.",
    "net.minecraft.world.entity.npc.villager.": "net.minecraft.world.entity.npc.",
    "net.minecraft.world.entity.projectile.arrow.": "net.minecraft.world.entity.projectile.",
    "net.minecraft.world.entity.projectile.throwableitemprojectile.": "net.minecraft.world.entity.projectile.",
    "net.minecraft.server.level.ParticleStatus": "net.minecraft.client.ParticleStatus",
    "net.minecraft.client.renderer.block.BlockAndTintGetter": "net.minecraft.world.level.BlockAndTintGetter",
    "net.minecraft.client.model.player.PlayerModel": "net.minecraft.client.model.PlayerModel",
    "net.minecraft.world.level.storage.ValueInput": "thaumcraft.compat.ValueInput",
    "net.minecraft.world.level.storage.ValueOutput": "thaumcraft.compat.ValueOutput",
    "net.minecraft.resources.Identifier": "net.minecraft.resources.ResourceLocation",
    "net.minecraft.world.entity.EntitySpawnReason": "net.minecraft.world.entity.MobSpawnType",
    "net.minecraft.world.entity.EntityTypes": "net.minecraft.world.entity.EntityType",
    "net.minecraft.world.item.ItemUseAnimation": "net.minecraft.world.item.UseAnim",
    "net.minecraft.world.level.block.VegetationBlock": "net.minecraft.world.level.block.BushBlock",
    "net.minecraft.util.ARGB": "net.minecraft.util.FastColor",
    "org.jspecify.annotations.Nullable": "javax.annotation.Nullable",
}


def move_imports(moves):
    def apply(text):
        def replace(match):
            name = match.group(2)
            for old, new in moves.items():
                if old.endswith("."):
                    if name.startswith(old) and "." not in name[len(old):]:
                        name = new + name[len(old):]
                        break
                elif name == old:
                    name = new
                    break
            return match.group(1) + name + ";"

        return re.sub(r"^(import\s+(?:static\s+)?)([\w.]+);", replace, text, flags=re.M)

    return apply


def dedupe_imports(text):
    seen = set()
    lines = []
    for line in text.split("\n"):
        if line.startswith("import ") and line.endswith(";"):
            if line in seen:
                continue
            seen.add(line)
        lines.append(line)
    return "\n".join(lines)


def rules_1_21_1():
    return [
        move_imports(PACKAGE_MOVES_1_21_1),
        rename_all({
            "Identifier": "ResourceLocation",
            "EntitySpawnReason": "MobSpawnType",
            "EntityTypes": "EntityType",
            "ItemUseAnimation": "UseAnim",
            "VegetationBlock": "BushBlock",
        }),
        rename_members("MobSpawnType", {"SPAWN_ITEM_USE": "SPAWN_EGG"}),
        rename_members("EntitySpawnReason", {"SPAWN_ITEM_USE": "SPAWN_EGG"}),
        rename_members("MobEffects", {
            "SPEED": "MOVEMENT_SPEED",
            "SLOWNESS": "MOVEMENT_SLOWDOWN",
            "HASTE": "DIG_SPEED",
            "MINING_FATIGUE": "DIG_SLOWDOWN",
            "STRENGTH": "DAMAGE_BOOST",
            "INSTANT_HEALTH": "HEAL",
            "INSTANT_DAMAGE": "HARM",
            "JUMP_BOOST": "JUMP",
            "NAUSEA": "CONFUSION",
            "RESISTANCE": "DAMAGE_RESISTANCE",
        }),
        rename_members("PushReaction", {"POPPED": "DESTROY", "IMMOVEABLE": "BLOCK"}),
        regex_rules([
            (r"(?<![\w$])ARGB\.", "FastColor.ARGB32."),
            (r"\.identifier\(\)", ".location()"),
            (r"\.getMinY\(\)", ".getMinBuildHeight()"),
            (r"\.getSelectedSlot\(\)", ".selected"),
            (r"\.getNonEquipmentItems\(\)", ".items"),
        ]),
        calls(r"((?:\bsuper|\bthis|[\w)\]])\s*\.\s*)hurtServer\(", drop_first_argument),
        hurt_declaration,
        calls(r"(?<![\w$.])(\w+(?:\(\))?)\s*\.rotateDegrees\(", rotate_degrees_call),
        dedupe_imports,
    ]


def rotate_degrees_call(match, arguments):
    if len(arguments) != 2:
        return None
    axis = arguments[0].strip()
    if not re.fullmatch(r"(?:Axis\.)?\w+", axis):
        return None
    return match.group(1) + ".mulPose(" + axis + ".rotationDegrees(" + arguments[1].strip() + "))"


TARGETS = {
    "1.21.1": rules_1_21_1,
}


def rewrite_text(text, rules, whole_file=True):
    masked, literals = mask(text)
    for rule in rules:
        if not whole_file and rule is dedupe_imports:
            continue
        masked = rule(masked)
    return unmask(masked, literals)


def rewrite_line(line, rules):
    return rewrite_text(line, rules, whole_file=False)


def filter_diff(stream, rules, output):
    current_java = False
    for line in stream:
        body = line.rstrip("\n")
        ending = line[len(body):]
        if line.startswith("diff --git "):
            current_java = body.endswith(".java")
            output.write(line)
        elif line.startswith(("--- ", "+++ ", "index ", "@@", "new file", "deleted file", "similarity", "rename ", "old mode", "new mode")):
            output.write(line)
        elif current_java and body[:1] in ("+", "-", " "):
            output.write(body[0] + rewrite_line(body[1:], rules) + ending)
        else:
            output.write(line)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--target", required=True, choices=sorted(TARGETS))
    parser.add_argument("--files", nargs="*", default=[])
    parser.add_argument("--all", metavar="DIR")
    arguments = parser.parse_args()
    rules = TARGETS[arguments.target]()
    paths = [Path(path) for path in arguments.files]
    if arguments.all:
        paths += sorted(Path(arguments.all).rglob("*.java"))
    if not paths:
        filter_diff(sys.stdin, rules, sys.stdout)
        return
    changed = 0
    for path in paths:
        original = path.read_text(encoding="utf-8")
        rewritten = rewrite_text(original, rules)
        if rewritten != original:
            path.write_text(rewritten, encoding="utf-8")
            changed += 1
    print("rewritten " + str(changed) + " of " + str(len(paths)) + " files", file=sys.stderr)


if __name__ == "__main__":
    main()

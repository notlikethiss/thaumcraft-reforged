import gzip
import struct
from pathlib import Path

DATA_VERSION = 5023
NS = "thaumcraft"
BIOMES = {"plains": "plains", "taiga": "taiga", "savanna": "savanna", "snowy": "snow", "desert": "desert"}

TAG_BYTE, TAG_INT, TAG_DOUBLE, TAG_STRING, TAG_LIST, TAG_COMPOUND = 1, 3, 6, 8, 9, 10


class Byte(int):
    pass


class Int(int):
    pass


class Double(float):
    pass


class TagList(list):
    def __init__(self, items, kind=None):
        super().__init__(items)
        self.kind = kind


def tag_type(value):
    if isinstance(value, Byte):
        return TAG_BYTE
    if isinstance(value, Int):
        return TAG_INT
    if isinstance(value, Double):
        return TAG_DOUBLE
    if isinstance(value, str):
        return TAG_STRING
    if isinstance(value, TagList):
        return TAG_LIST
    if isinstance(value, dict):
        return TAG_COMPOUND
    raise TypeError(type(value))


def write_string(out, value):
    encoded = value.encode("utf-8")
    out.append(struct.pack(">H", len(encoded)))
    out.append(encoded)


def write_payload(out, value):
    kind = tag_type(value)
    if kind == TAG_BYTE:
        out.append(struct.pack(">b", value))
    elif kind == TAG_INT:
        out.append(struct.pack(">i", value))
    elif kind == TAG_DOUBLE:
        out.append(struct.pack(">d", value))
    elif kind == TAG_STRING:
        write_string(out, value)
    elif kind == TAG_LIST:
        item_kind = value.kind if value.kind is not None else (tag_type(value[0]) if value else 0)
        out.append(struct.pack(">bi", item_kind, len(value)))
        for item in value:
            write_payload(out, item)
    else:
        for key, item in value.items():
            out.append(struct.pack(">b", tag_type(item)))
            write_string(out, key)
            write_payload(out, item)
        out.append(b"\x00")


def to_nbt(root):
    out = [struct.pack(">b", TAG_COMPOUND)]
    write_string(out, "")
    write_payload(out, root)
    return gzip.compress(b"".join(out), mtime=0)


def block_state(name, **properties):
    return (name, tuple(sorted(properties.items())))


def stairs(name, facing):
    return block_state(name, facing=facing, half="bottom", shape="straight", waterlogged="false")


def pane(east_west=True):
    return block_state(
        "minecraft:glass_pane",
        east="true" if east_west else "false",
        west="true" if east_west else "false",
        north="false",
        south="false",
        waterlogged="false",
    )


def materials(biome):
    desert = biome == "desert"
    return {
        "planks": block_state("minecraft:smooth_sandstone" if desert else "minecraft:oak_planks"),
        "cobble": block_state("minecraft:sandstone" if desert else "minecraft:cobblestone"),
        "corner": block_state("minecraft:chiseled_sandstone" if desert else "minecraft:chiseled_stone_bricks"),
        "stairs": "minecraft:sandstone_stairs" if desert else "minecraft:cobblestone_stairs",
    }


def tower(biome):
    material = materials(biome)
    blocks = {}
    nbts = {}

    def fill(x1, y1, z1, x2, y2, z2, state):
        for x in range(x1, x2 + 1):
            for y in range(y1, y2 + 1):
                for z in range(z1, z2 + 1):
                    blocks[(x, y, z)] = state

    air = block_state("minecraft:air")
    fill(2, 1, 2, 4, 11, 4, air)
    for y in (0, 5, 10):
        fill(2, y, 2, 4, y, 4, material["planks"])
    fill(1, 0, 2, 1, 11, 4, material["cobble"])
    fill(2, 0, 1, 4, 11, 1, material["cobble"])
    fill(5, 0, 2, 5, 11, 4, material["cobble"])
    fill(2, 0, 5, 4, 11, 5, material["cobble"])
    for y in (0, 5, 10):
        for x in (1, 5):
            for z in (1, 5):
                blocks[(x, y, z)] = material["corner"]
    for position in [(3, 7, 1), (3, 8, 1), (3, 7, 5), (3, 8, 5), (3, 2, 5), (3, 3, 5)]:
        blocks[position] = pane()
    for y in range(1, 10):
        blocks[(4, y, 3)] = block_state("minecraft:ladder", facing="west", waterlogged="false")
    blocks[(4, 10, 3)] = block_state(
        "minecraft:oak_trapdoor", facing="north", half="bottom", open="true", powered="false", waterlogged="false"
    )
    blocks[(3, 5, 3)] = block_state("minecraft:glowstone")
    blocks[(2, 6, 2)] = block_state("minecraft:chest", facing="south", type="single", waterlogged="false")
    nbts[(2, 6, 2)] = {"id": "minecraft:chest", "LootTable": f"{NS}:chests/wizard_tower"}
    blocks[(3, 1, 1)] = block_state(
        "minecraft:oak_door", facing="south", half="lower", hinge="left", open="false", powered="false"
    )
    blocks[(3, 2, 1)] = block_state(
        "minecraft:oak_door", facing="south", half="upper", hinge="left", open="false", powered="false"
    )
    blocks[(2, 1, 4)] = block_state(f"{NS}:arcane_worktable")
    blocks[(3, 0, 0)] = block_state("minecraft:jigsaw", orientation="north_up")
    nbts[(3, 0, 0)] = {
        "id": "minecraft:jigsaw",
        "name": "minecraft:building_entrance",
        "target": "minecraft:building_entrance",
        "pool": "minecraft:empty",
        "joint": "aligned",
        "final_state": state_string(stairs(material["stairs"], "north")),
    }
    return blocks, nbts


def state_string(state):
    name, properties = state
    if not properties:
        return name
    return name + "[" + ",".join(f"{key}={value}" for key, value in properties) + "]"


def template(biome):
    blocks, nbts = tower(biome)
    offset_x = 1
    palette = []
    entries = []
    for position in sorted(blocks, key=lambda p: (p[1], p[0], p[2])):
        state = blocks[position]
        if state not in palette:
            palette.append(state)
        local = (position[0] - offset_x, position[1], position[2])
        entry = {
            "pos": TagList([Int(value) for value in local], TAG_INT),
            "state": Int(palette.index(state)),
        }
        if position in nbts:
            entry["nbt"] = nbts[position]
        entries.append(entry)
    palette_tag = []
    for name, properties in palette:
        entry = {"id": name}
        if properties:
            entry["properties"] = {key: value for key, value in properties}
        palette_tag.append(entry)
    villager_position = (2.5, 1.0, 3.5)
    villager = {
        "pos": TagList([Double(value) for value in villager_position], TAG_DOUBLE),
        "blockPos": TagList([Int(2), Int(1), Int(3)], TAG_INT),
        "nbt": {
            "id": "minecraft:villager",
            "VillagerData": {"profession": f"{NS}:wizard", "level": Int(1), "type": f"minecraft:{BIOMES[biome]}"},
            "PersistenceRequired": Byte(1),
        },
    }
    return {
        "DataVersion": Int(DATA_VERSION),
        "size": TagList([Int(5), Int(12), Int(6)], TAG_INT),
        "palette": TagList(palette_tag, TAG_COMPOUND),
        "blocks": TagList(entries, TAG_COMPOUND),
        "entities": TagList([villager], TAG_COMPOUND),
    }


def generate(data_dir):
    for biome in BIOMES:
        path = Path(data_dir) / NS / "structure" / "village" / biome / "wizard_tower.nbt"
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(to_nbt(template(biome)))

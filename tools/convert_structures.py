import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SOURCE = ROOT / "reference" / "decompiled" / "thaumcraft" / "common" / "world" / "WorldGenMound.java"
OUTPUT = ROOT / "src" / "main" / "resources" / "thaumcraft" / "structures" / "mound.txt"

CALL = re.compile(r"world\.setBlock\(i \+ (-?\d+), j \+ (-?\d+), k \+ (-?\d+), (0|Block\.(\w+)\.blockID)(?:, (\d+), \d+)?\);")


def main():
    lines = []
    for match in CALL.finditer(SOURCE.read_text(encoding="utf-8")):
        x, y, z, raw, name, meta = match.groups()
        block = "air" if raw == "0" else name
        lines.append(f"{x} {y} {z} {block} {meta or 0}")
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"mound: {len(lines)} blocks")


if __name__ == "__main__":
    main()

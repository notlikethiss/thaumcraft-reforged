import csv
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent
MAPPINGS = ROOT / "cache" / "mcp152"
SRG_PATTERN = re.compile(r"\b(func_\d+_[a-zA-Z_]+|field_\d+_[a-zA-Z_]+)\b")


def load_mappings():
    names = {}
    for file_name in ("methods.csv", "fields.csv"):
        with open(MAPPINGS / file_name, newline="", encoding="utf-8") as handle:
            for row in csv.DictReader(handle):
                names[row["searge"]] = row["name"]
    return names


def remap(source_dir):
    names = load_mappings()
    missing = set()
    files = list(Path(source_dir).rglob("*.java"))

    def replace(match):
        srg = match.group(1)
        if srg in names:
            return names[srg]
        missing.add(srg)
        return srg

    for path in files:
        text = path.read_text(encoding="utf-8")
        remapped = SRG_PATTERN.sub(replace, text)
        if remapped != text:
            path.write_text(remapped, encoding="utf-8")

    print(f"remapped {len(files)} files, unmapped names: {len(missing)}")
    for srg in sorted(missing):
        print(f"  {srg}")


if __name__ == "__main__":
    remap(sys.argv[1])

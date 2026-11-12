#!/usr/bin/env python3
"""Builds app/src/main/assets/puzzles.json from tools/puzzles/*.txt

Line format:  pack|ANSWER|emoji|clue|category|year
Hindi clues: tools/puzzles_hi/*.txt, one per line:  puzzle-id|हिन्दी सुराग
Answers use A-Z and single spaces only. A film may repeat only in the
'dialogues' and 'directors' packs. Lines with year 0 are ignored.
IDs come from the title, so never rename a film's answer after release.
Run from the repo root:  python3 tools/build_puzzles.py
"""
import glob, json, re, sys, collections

REPEAT_OK = {"dialogues", "directors"}
rows, seen, errors = [], {}, []
for path in sorted(glob.glob("tools/puzzles/*.txt")):
    for n, line in enumerate(open(path, encoding="utf-8"), 1):
        line = line.rstrip("\n")
        if not line.strip() or line.startswith("#"):
            continue
        parts = line.split("|")
        if len(parts) != 6:
            errors.append(f"{path}:{n}: expected 6 fields, got {len(parts)}")
            continue
        pack, answer, emoji, clue, cat, year = [p.strip() for p in parts]
        if year == "0":
            continue
        if not re.fullmatch(r"[A-Z]+( [A-Z]+)*", answer):
            errors.append(f"{path}:{n}: bad answer {answer!r}")
        if pack not in REPEAT_OK:
            if answer in seen:
                errors.append(f"{path}:{n}: duplicate {answer} (also in {seen[answer]})")
            seen[answer] = pack
        rows.append((pack, answer, emoji, clue, cat, year))

hindi = {}
for path in sorted(glob.glob("tools/puzzles_hi/*.txt")):
    for n, line in enumerate(open(path, encoding="utf-8"), 1):
        line = line.rstrip("\n")
        if not line.strip() or line.startswith("#"):
            continue
        pid, _, text = line.partition("|")
        hindi[pid.strip()] = text.strip()

if errors:
    print("\n".join(errors)); sys.exit(1)

count = collections.Counter()
out = []
for pack, answer, emoji, clue, cat, year in rows:
    count[pack] += 1
    # Stable id from the title, so saved progress survives adding or reordering films.
    pid = f"{pack}-{answer.lower().replace(' ', '-')}"
    entry = {"id": pid, "pack": pack, "answer": answer,
             "emoji": emoji, "clue": clue, "category": cat, "year": year}
    if pid in hindi:
        entry["clueHi"] = hindi[pid]
    out.append(entry)

with open("app/src/main/assets/puzzles.json", "w", encoding="utf-8") as f:
    f.write("[\n" + ",\n".join("  " + json.dumps(o, ensure_ascii=False) for o in out) + "\n]\n")
ids = {o["id"] for o in out}
unknown = [k for k in hindi if k not in ids]
missing = [o["id"] for o in out if "clueHi" not in o]
print(len(out), "puzzles"); print(dict(count))
print(f"Hindi clues: {len(out) - len(missing)} of {len(out)}")
if unknown:
    print("Hindi ids with no film:", unknown); sys.exit(1)
if missing:
    print("Missing Hindi:", missing[:20], "..." if len(missing) > 20 else "")

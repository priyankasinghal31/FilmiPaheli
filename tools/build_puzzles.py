#!/usr/bin/env python3
"""Builds app/src/main/assets/puzzles.json from tools/puzzles/*.txt

Line format:  pack|ANSWER|emoji|clue|category|year
Answers use A-Z and single spaces only. A film may repeat only in the
'dialogues' and 'directors' packs. Lines with year 0 are ignored.
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

if errors:
    print("\n".join(errors)); sys.exit(1)

count = collections.Counter()
out = []
for pack, answer, emoji, clue, cat, year in rows:
    count[pack] += 1
    out.append({"id": f"{pack}-{count[pack]:03d}", "pack": pack, "answer": answer,
                "emoji": emoji, "clue": clue, "category": cat, "year": year})

with open("app/src/main/assets/puzzles.json", "w", encoding="utf-8") as f:
    f.write("[\n" + ",\n".join("  " + json.dumps(o, ensure_ascii=False) for o in out) + "\n]\n")
print(len(out), "puzzles"); print(dict(count))

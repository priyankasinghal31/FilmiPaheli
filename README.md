# Filmi Paheli (फ़िल्मी पहेली)

A Bollywood guess-the-film puzzle game for Android, written in Java. Each puzzle is one emoji clue, one text clue and one hidden film title. Tap letters to fill the blanks. Five wrong guesses and the film is a flop.

## Features

- Emoji and text clues on a movie-ticket card
- 500+ Bollywood films in 15 packs: Black & White Classics, 70s & 80s, 90s, 2000s, 2010s, Latest Hits, star packs (Shah Rukh, Aamir, Salman, Amitabh, Akshay), Leading Ladies and an Emoji Round
- Dialogues and Directors packs unlock with 300 coins
- Daily puzzle with a +50 coin bonus, and a seven day streak
- Coins, stars (1 to 3 by hearts left) and saved progress
- Hints: spend 20 coins, or watch a rewarded ad for a free letter
- Rewarded ad for an extra life after a loss
- Ads only run when the player chooses to watch one (no forced ads)

## Project layout

```
app/src/main/
  assets/puzzles.json                 puzzle data (id, pack, answer, emoji, clue, category, year)
  java/com/filmipaheli/app/
    HomeActivity.java                 daily card, streak, pack grid
    GameActivity.java                 clue ticket, blanks, hints, keyboard, game rules
    ResultActivity.java               stars, coins, extra life, next puzzle
    AdManager.java                    AdMob rewarded and interstitial ads
    Prefs.java                        coins, solved puzzles, streak (SharedPreferences)
    PuzzleRepository.java             loads puzzles.json, picks daily and next puzzle
    Pack.java / Puzzle.java           models
  res/                                layouts, drawables, colours
```

## Run it

1. Open the folder in Android Studio (Hedgehog or newer). It will sync Gradle and create the Gradle wrapper.
2. Run the `app` configuration on a device or emulator (Android 7.0 / API 24 or newer).

## Add your own films

Edit the text files in `tools/puzzles/`. One film per line:

```
pack|ANSWER|emoji|clue|category|year
```

Answers use capital letters A to Z and single spaces only (write "PK", "MUGHAL E AZAM", "THREE IDIOTS"). A film may repeat only in the `dialogues` and `directors` packs. Then run this from the repo root to rebuild `app/src/main/assets/puzzles.json` and check for mistakes:

```
python3 tools/build_puzzles.py
```

Pack ids must match a `Pack` in `Pack.java`. Add a new `Pack` there to add a new pack.

## Ads

Ad ids are set per build type in `app/build.gradle`. Debug builds (run from Android Studio) use Google's public **test** ids, so tapping ads is safe. Release builds use the real AdMob ids. Never tap real ads on your own device.

## Fonts

Headings use the system serif. For the poster look from the design preview, add the Rozha One font (Google Fonts) under `res/font/` and set it as `android:fontFamily` on the heading text views.

## Ideas for next

Packs, Daily and Shop tabs, sound effects, a pack unlock flow with coins, more films, Hindi titles.

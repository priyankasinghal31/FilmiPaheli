# Filmi Paheli (फ़िल्मी पहेली)

A Bollywood guess-the-film puzzle game for Android, written in Java. Each puzzle is one emoji clue, one text clue and one hidden film title. Tap letters to fill the blanks. Five wrong guesses and the film is a flop.

## Features

- Emoji and text clues on a movie-ticket card
- Film packs: 90s Hits, Shah Rukh Khan, Emoji Round, Black & White Classics (Dialogues and Directors are locked placeholders)
- Daily puzzle with a +50 coin bonus, and a seven day streak
- Coins, stars (1 to 3 by hearts left) and saved progress
- Hints: spend 20 coins, or watch a rewarded ad for a free letter
- Rewarded ad for an extra life after a loss
- Interstitial ad after every 4th finished puzzle
- 20 sample films in `app/src/main/assets/puzzles.json`

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

Add an object to `puzzles.json`. Answers must use capital letters A to Z and single spaces only (write "PK", "MUGHAL E AZAM"). Give it one of the existing `pack` ids, or add a new `Pack` in `Pack.java`.

## Ads

The project uses Google's public **test** AdMob ids in `AndroidManifest.xml` and `AdManager.java`. Create your own app and ad units in AdMob and replace them before publishing.

## Fonts

Headings use the system serif. For the poster look from the design preview, add the Rozha One font (Google Fonts) under `res/font/` and set it as `android:fontFamily` on the heading text views.

## Ideas for next

Packs, Daily and Shop tabs, sound effects, a pack unlock flow with coins, more films, Hindi titles.

# Publishing Filmi Paheli to Google Play

Follow the steps in order. Things marked **You** need your accounts or your phone.

## 1. Before you build

- **You:** update Android Studio to the newest stable version. The project now builds with Android Gradle Plugin 8.11.1, Gradle 8.13 and Android 16 (API 36), which Google Play requires for new apps ([Google's rule](https://support.google.com/googleplay/android-developer/answer/11926878)). Android Studio will offer to download the SDK and Gradle. Accept, then run **File, Sync Project with Gradle Files**.
- **You:** fill in your contact email in `docs/index.html` (search for `ADD YOUR CONTACT EMAIL HERE`) and push.
- **You:** turn on GitHub Pages so the privacy policy has a web address. Repo **Settings, Pages**, source **Deploy from a branch**, branch `main`, folder `/docs`. The address will be `https://priyankasinghal31.github.io/FilmiPaheli/`. Play needs this link.

## 2. Create your signing key (once)

Google needs every update signed with the same key. Lose it and you cannot update the app, so back it up in two places.

1. In Android Studio: **Build, Generate Signed App Bundle or APK**, choose **Android App Bundle**, **Create new** under key store path. Choose a strong password, alias `upload`, validity 25 years or more.
2. Copy `keystore.properties.example` to `keystore.properties` and fill in the path, passwords and alias. Never commit this file or the `.jks` file. Both are in `.gitignore`.

## 3. Build the release file

1. Increase `versionCode` in `app/build.gradle` for every upload (1, 2, 3 and so on). Change `versionName` to whatever you want players to see.
2. **Build, Generate Signed App Bundle or APK, Android App Bundle, release**. The file is `app/build/outputs/bundle/release/app-release.aab`.
3. Test the release build on your phone before uploading (see the checklist below).

Ads: debug builds (the Run button) use Google's test ads. Release builds use your real ad IDs. Do not tap real ads on your own phone. In AdMob, **Settings, Test devices**, add your phone so it gets test ads even in release builds.

## 4. Create the app in Play Console

1. **You:** create a developer account at play.google.com/console (one-time fee, identity check).
2. **Create app**: name `Filmi Paheli: Bollywood Quiz`, default language English (United States) or Hindi, type **Game**, **Free**.
3. **Store listing**: paste the text from `store/listing.md`. Upload `store/icon-512.png` and `store/feature-graphic-1024x500.png`. Add the Hindi translation.
4. **Screenshots (You):** take at least 4 on your phone. Suggested screens: home, a puzzle with a clue, the solved screen, the spin wheel, a Hindi puzzle. Screenshots must show the real app.
5. **App content** (Policy, App content), answer like this:
   - **Privacy policy:** your GitHub Pages link.
   - **Ads:** Yes, the app contains ads.
   - **App access:** all functionality is available without special access.
   - **Target audience:** 13 and over. Do not choose a children's age group.
   - **Content rating:** fill in the questionnaire honestly. The game has no graphic violence. Some clues mention crime, alcohol or drugs in the plot of a film, in text only. No user-generated content, no purchases, no location sharing.
   - **Data safety:** see the table below.
   - **News app, government app, health, financial features:** No.
6. **Select countries** and pricing: Free.

### Data safety answers

We collect no accounts, names, emails, contacts, photos or precise location. The Google AdMob library collects some data for ads. Check AdMob's own guidance on the Data safety form when you file, because it changes.

| Data type | Collected | Shared | Why |
|---|---|---|---|
| Device or other IDs (advertising ID) | Yes | Yes, with Google AdMob | Advertising, fraud prevention |
| Approximate location (from IP) | Yes | Yes, with Google AdMob | Advertising |
| App interactions (ad interactions) | Yes | Yes, with Google AdMob | Advertising, analytics |
| Crash logs and diagnostics | Yes | Yes, with Google AdMob | Analytics |

- Data is encrypted in transit: **Yes**.
- Users can ask for data to be deleted: there is no account and we hold no data, so answer according to the form's wording. The privacy policy explains this.

## 5. Test, then release

New personal developer accounts must run a **closed test with at least 12 testers for 14 days** before they can apply for production ([Google's rule](https://support.google.com/googleplay/android-developer/answer/14151465)). Organization accounts are exempt.

1. **Testing, Internal testing**: upload the `.aab`, add yourself, install from the link and check everything works.
2. **Testing, Closed testing**: upload the same `.aab`, add 12 or more testers by email, ask them to install and keep it opted in for 14 days.
3. After 14 days, **Apply for production**. Review usually takes a few days.
4. **Production**: create the release, upload, roll out.

## 6. After it is live

- In AdMob, link the app to its Play Store listing (Apps, App settings).
- New ad units can take up to an hour to show real ads.
- Add films any time (see README). No app update is needed for new films in existing packs.

## Test checklist before every upload

- Fresh install opens to the home screen, all 15 packs show.
- Play a film to the end. Check stars, coins and the film name on the result screen.
- Lose all hearts: the ad pop-up shows, the film name does not.
- Switch language to Hindi and back. Switch theme to light and dark.
- Turn sound and vibration off and on.
- Spin the wheel. Open Achievements.
- Share a result and use Ask a friend.
- Turn on airplane mode: the game still works.
- Rotate or use a tablet if you can: the game should still be usable.

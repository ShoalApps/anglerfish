# Anglerfish

Reddit as an app, in Material 3 Expressive: Home, Popular, Search, Inbox and You tabs (a rail on tablets), one page that keeps your place as you switch tabs, Reddit's open-the-app nags removed, reddit.com links and shares opening here, and light or dark following your phone.

Reddit's data API now needs Reddit's approval, so Anglerfish shows Reddit's own website rather than talking to the API. You sign in on Reddit's own page. On some networks Reddit only shows itself to signed-in people; Anglerfish says so and offers the sign-in.

Part of [Shoal](https://github.com/ShoalApps). Install and update it with [Shoal](https://github.com/ShoalApps/shoal). GPL-3.0.

Anglerfish is an independent project, not affiliated with, endorsed by or sponsored by Reddit, Inc.

## Privacy and legal

No ads, no tracking, nothing sent to us unless you agree to anonymous error reports. See the [privacy policy](https://shoalapps.github.io/privacy.html) and [legal notices](https://shoalapps.github.io/legal.html).

## Support

Shoal is free, open source and has no ads. If you'd like to help it keep going, you can [support Shoal on Liberapay](https://liberapay.com/ShoalApps).

## Release notes

Update `app/src/main/assets/whats-new.json` with each release. Notes ship with the APK, appear once per installed version and can be reopened from Settings. Keep them specific to changes in that release.

## App language

The apps follow Android's system language by default, with an App language picker in Settings and right-to-left layout support. Translations are bundled and work offline. Coverage varies: many phrases are automatically translated, community catalogues may be partial, and missing strings fall back to British English. See `tools/README.md` and `app/src/main/assets/i18n/coverage.json` for maintenance and actual coverage.

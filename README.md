# Y5 Debloater

Shizuku-powered system app debloater and restorer for Huawei Y5 Prime 2018 (Android 8.1) and other Android devices.

Apps are disabled or removed only for user 0 (`pm uninstall --user 0` / `pm disable-user --user 0`), so they can be restored at any time.

## Download the APK

Every push to `main` builds a new APK with GitHub Actions and publishes it as a release.

**Latest APK:** https://github.com/chainatelapoka-hue/y5-debloater/releases/latest/download/Y5-Debloater.apk

All builds: https://github.com/chainatelapoka-hue/y5-debloater/releases

## Install

1. Install [Shizuku](https://shizuku.rikka.app/) on the phone and start it once (ADB start command is shown inside the app's ADB guide).
2. Open the downloaded APK and install it (allow "Install unknown apps" for your browser/file manager).
3. Open Y5 Debloater, grant the Shizuku permission, then tap an app to uninstall, disable, or restore it.

## Build locally (optional)

Requires JDK 17+ and the Android SDK (platform 36.1, build-tools 36.0.0).

```bash
gradle :app:assembleRelease
```

The release build reads signing info from environment variables: `KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_PASSWORD`. The `debugConfig` signing config expects `debug.keystore` in the project root. Both files are git-ignored.

## Notes

- Signing keys are generated in CI on each run. If an installed build refuses to update over a previous one ("signature mismatch"), uninstall the old version first, or set a `KEYSTORE_PASSWORD` secret and keep one keystore for all builds.
- Do not remove system apps listed as `System Core` in the app. The app warns about them and requires an extra confirmation.

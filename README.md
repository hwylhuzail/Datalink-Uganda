# DataLink Uganda

DataLink Uganda is an Android app and small Vercel site for finding Wi-Fi locations mapped in OpenStreetMap. The Android app downloads listings for the user's current area and stores the most recent result on-device for offline viewing.

## What this release does

- Searches mapped Wi-Fi within 5 km of the device in Uganda.
- Uses Android's available internet connection; it does not provide mobile data, bypass carrier restrictions, or connect to a network without its access details.
- Saves the most recently downloaded listings on the device. Refresh requires internet and location access.
- Opens a listing's coordinates in an installed maps app.
- Makes no promise that a listing is free, current, safe, or available. Users should confirm access with the venue.
- Does not issue points, airtime, or data rewards.

Listings are community supplied by OpenStreetMap contributors under the Open Database License (ODbL). Coverage and accuracy depend on map contributors.

## Android build

The Android project is in `android/`. Local builds require JDK 17 and Android SDK Platform 36. Use the included Gradle wrapper:

```sh
cd android
./gradlew testDebugUnitTest assembleDebug
```

The debug APK is written to `android/app/build/outputs/apk/debug/app-debug.apk`.

The app queries the public Overpass API directly by default. To use the Vercel API instead, set the non-secret Gradle property `DATALINK_API_BASE_URL` to the deployed site URL, for example:

```sh
./gradlew assembleDebug -PDATALINK_API_BASE_URL=https://your-project.vercel.app
```

## Vercel site and API

The repository root contains a static public site (`public/`) and a serverless endpoint (`api/hotspots.js`). The endpoint validates Uganda coordinates, limits search radius, queries OpenStreetMap, and returns ODbL attribution. Vercel can deploy this repository from its root directory.

To enable GitHub Actions deployments, configure the following repository settings:

- Repository variable `VERCEL_CONFIGURED=true`
- Repository secrets `VERCEL_TOKEN`, `VERCEL_ORG_ID`, and `VERCEL_PROJECT_ID`

The Vercel workflow otherwise reports that deployment was skipped. The deployment requires a Vercel project linked to this repository and valid credentials.

After deployment, the optional repository variable `DATALINK_API_BASE_URL` can point Android builds at the Vercel API. Without it, Android queries the public Overpass endpoints directly and tries a second endpoint if the first is unavailable.

Run API tests locally with Node.js:

```sh
node --test api/hotspots.test.js
```

## Android release signing

Pull requests and pushes to `main` build and upload an unsigned debug APK. To publish a signed APK from a version tag such as `v1.0.0`, add these repository secrets:

- `ANDROID_KEYSTORE_BASE64` — base64-encoded release keystore
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

Create and safeguard your own release key before publishing. The tagged release workflow fails with a clear error if any signing secret is missing; the keystore and passwords must never be committed. A GitHub release is created with the signed APK after the build succeeds.

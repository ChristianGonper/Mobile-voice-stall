# How to Generate an APK in Android Studio

This guide explains exactly what you need to do to create an installable APK for **Mobile Voice Stall** using Android Studio.

## Prerequisites

1. **Android Studio** installed (latest stable recommended).
2. **Android SDK 34** installed (the project target SDK).
3. A connected Android device or emulator (optional, for testing).

## 1) Open the project

1. Open Android Studio.
2. Click **Open**.
3. Select the project folder: `Mobile-voice-stall`.
4. Wait for Gradle sync to finish.

## 2) Build a debug APK (quick install)

Use this when you want to test quickly on your own device.

1. In Android Studio menu, click:
   - **Build > Build Bundle(s) / APK(s) > Build APK(s)**
2. Wait for build completion.
3. When Android Studio shows **APK(s) generated successfully**, click **locate**.
4. APK path is usually:

```text
app/build/outputs/apk/debug/app-debug.apk
```

You can now install this APK on your Android device.

## 3) Build a signed release APK (for sharing)

Use this for distributing the app to other users.

### 3.1 Create or use a keystore

If you do not have a keystore yet:

1. Android Studio menu:
   - **Build > Generate Signed Bundle / APK...**
2. Choose **APK** and click **Next**.
3. Click **Create new...** and fill in:
   - Keystore path
   - Password
   - Key alias
   - Key password
   - Certificate information
4. Click **OK**.

### 3.2 Configure this project for release signing

At project root, create a file named `keystore.properties` (you can copy from `keystore.properties.example`) with:

```properties
storeFile=/absolute/path/to/your/release.keystore
storePassword=your_store_password
keyAlias=your_key_alias
keyPassword=your_key_password
```

> `keystore.properties` is ignored by git and should **not** be committed.

### 3.3 Generate the release APK

Option A (UI wizard):
1. **Build > Generate Signed Bundle / APK...**
2. Select **APK**.
3. Select your keystore + key alias.
4. Choose **release** build variant.
5. Click **Finish**.

Option B (Build menu with existing signing config):
1. **Build > Build Bundle(s) / APK(s) > Build APK(s)** after release config is set.

Release APK output is usually:

```text
app/build/outputs/apk/release/app-release.apk
```

## 4) Install APK on your phone

1. Copy APK to the phone.
2. Enable install from unknown sources (if prompted).
3. Open APK file and install.

## 5) First run checklist

1. Open the app.
2. Go to **Settings**.
3. Add your **Groq API key**.
4. Grant required permissions (microphone, overlay, notifications).
5. Test recording and transcription.

---

If build fails during sync or APK generation, check:
- `File > Settings > Build, Execution, Deployment > Gradle` JDK version (use JDK 17 for this project).
- Android SDK Platform 34 + Build Tools are installed.
- `keystore.properties` values are correct for release builds.

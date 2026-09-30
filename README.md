# Remix Investment Tracker (Android)

A portfolio and SIP investment tracker built for Android using Kotlin and Jetpack Compose. Synced with Room database and Google Sheets.

---

## 🚀 Creating APK via GitHub (Automated)

This repository includes an automated **GitHub Actions CI/CD workflow** (`.github/workflows/build-apk.yml`) that builds your APK automatically.

### Step 1: Push Code to GitHub

If you haven't pushed this repository to GitHub yet:

```bash
# Add all project files
git add .

# Commit changes
git commit -m "Initialize Investment Tracker Android app with automated APK workflow"

# Set remote repository (replace with your repo URL)
git remote add origin https://github.com/AvinashDBA/Investment-Tracker.git

# Push to main branch
git branch -M main
git push -u origin main
```

*(Note: If using Google AI Studio, you can also push directly to GitHub from the top-right settings/export menu).*

---

### Step 2: Automated APK Build

Whenever you push to `main` (or run manually):
1. Navigate to your GitHub repository in your browser.
2. Click the **Actions** tab at the top.
3. Select the workflow titled **"Build Android APK"**.
4. You can monitor the progress while it compiles the code with JDK 21 and Gradle 9.3.1.
5. You can also manually trigger a build at any time by clicking **"Run workflow"** > **Run workflow**.

---

### Step 3: Download & Install APK

1. Once the workflow completes (green checkmark), click on the completed run.
2. Scroll down to the **Artifacts** section at the bottom of the page.
3. Click on **`investment-tracker-debug-apk`** to download the ZIP file containing the APK.
4. Extract the ZIP to get `app-debug.apk`.
5. Transfer `app-debug.apk` to your Android device (or download directly from your phone's browser) and tap to install.

---

## 🛠️ Local Build Instructions

To build the APK locally on your computer:

```bash
# Prerequisites: Java 21 JDK and Android SDK

# Build debug APK:
gradle :app:assembleDebug

# The APK will be generated at:
# app/build/outputs/apk/debug/app-debug.apk
```

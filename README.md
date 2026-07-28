# Quiz Maker Android App

An Android Quiz Maker application built with Android Studio, Kotlin, and Jetpack Compose.

## Features

* Create and manage quizzes
* Modern Jetpack Compose UI
* Android Studio project
* Automatic APK generation using GitHub Actions

---

# Requirements

* Android Studio (latest stable version)
* JDK 17 or newer (or the version required by the project)
* Android SDK
* Git

---

# Clone the Repository

```bash
git clone https://github.com/fasihrana-droid/quiz-maker-with-workflow.git
cd quiz-maker-with-workflow
```

---

# Build the Project Locally

## Windows

Open the project in Android Studio.

Wait for Gradle Sync to finish.

Build the Debug APK:

```bash
gradlew assembleDebug
```

or

```bash
.\gradlew assembleDebug
```

The APK will be generated in:

```
app/build/outputs/apk/debug/app-debug.apk
```

---

## Linux / macOS

```bash
chmod +x gradlew
./gradlew assembleDebug
```

APK location:

```
app/build/outputs/apk/debug/app-debug.apk
```

---

# Build APK with GitHub Actions

This repository includes a GitHub Actions workflow that automatically builds a Debug APK.

## Automatic Build

Every push to the `main` branch starts a build automatically.

Example:

```bash
git add .
git commit -m "Update project"
git push -u origin main
```

---

## Manual Build

1. Open the repository on GitHub.
2. Click the **Actions** tab.
3. Select **Build Debug APK**.
4. Click **Run workflow**.
5. Select the `main` branch.
6. Click **Run workflow**.

The build usually completes in 3–5 minutes.

---

# Download the APK

After the workflow succeeds:

1. Open the completed workflow run.
2. Scroll to **Artifacts**.
3. Download **quiz-maker-debug-apk**.
4. Extract the downloaded ZIP file.
5. Install `app-debug.apk` on your Android device.

---

# Project Structure

```
.
├── app
├── gradle
├── .github
│   └── workflows
├── gradlew
├── gradlew.bat
├── settings.gradle.kts
└── build.gradle.kts
```

---

# Troubleshooting

## Gradle Sync Failed

* Update Android Studio.
* Install the required Android SDK version.
* Verify that Gradle downloads successfully.

---

## GitHub Action Failed

Check the build logs under:

**Actions → Build Debug APK**

Common reasons include:

* Missing Gradle dependency
* Kotlin compilation error
* Android SDK version mismatch
* Java version mismatch

---

## APK Not Generated

Ensure the build completed successfully.

The generated APK is located at:

```
app/build/outputs/apk/debug/app-debug.apk
```

or can be downloaded from the GitHub Actions **Artifacts** section.

---

# License

This project is intended for educational and personal use. Add your preferred license if distributing publicly.


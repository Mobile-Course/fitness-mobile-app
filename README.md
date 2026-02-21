# FitTrack Android App

FitTrack is an Android fitness social app built with Kotlin and Jetpack Compose. It connects to a backend API for authentication, posts, achievements, profile data, and AI coaching.

## Current Features

- Email/password signup and login.
- Session restore on app launch (Splash -> Login/Main based on local user state).
- Google auth callback handling via deep link (`/app/auth/callback`).
- Feed with pagination, pull-to-refresh, optimistic likes, and comments.
- Create and edit posts (2-step flow) with optional workout details.
- Photo upload for posts from camera or gallery (type + size validation).
- Discover users with search and enriched profile stats.
- Profile screen with XP/level/streak, achievements, and user posts.
- Edit profile with extended fitness fields (age, height, weight, body fat, VO2max, 1RM values).
- AI Tips chat screen with streaming responses from backend.
- Token/cookie-aware networking with automatic refresh and forced logout handling.

## Tech Stack

- Kotlin + Coroutines/Flow
- Jetpack Compose (Material 3)
- Fragment host + Navigation Component (Safe Args)
- MVVM (`BaseViewModel` + UI state)
- Retrofit + OkHttp + Gson
- Room (local persistence/cache)
- Picasso/Coil for image loading

## Backend and API

- Base URL is currently hardcoded to `https://node86.cs.colman.ac.il`.
- Main API usage is under `/api/auth`, `/api/posts`, `/api/user-profiles`, `/api/achievements`, and coach streaming endpoints.
- `swagger.json` is included in the repo.

Important:

- The networking layer currently includes a trust-all TLS setup for development (`NetworkConfig`). This should be replaced with proper certificate validation for production.

## Deep Link Auth Callback

The app listens for:

- Scheme: `https`
- Host: `node86.cs.colman.ac.il`

When triggered, auth query params are consumed and stored for login continuation.

## Project Layout

```text
app/src/main/java/com/fitness/app/
  auth/            session + google auth result store
  data/            api services, repositories, local Room models/dao
  navigation/      screen routes
  network/         okhttp/auth/token refresh config
  ui/
    fragments/     Splash/Login/Signup/Main fragment hosts
    screens/       Compose feature screens (feed/post/discover/profile/ai)
    components/    reusable UI components
    viewmodels/    shared viewmodels (camera)
  utils/           cross-screen invalidation flows
```

## Getting Started

### Prerequisites

- Android Studio (recent stable)
- JDK 17
- Android SDK with:
  - `compileSdk 35`
  - `minSdk 24`
  - `targetSdk 34`

### Run

1. Open the project in Android Studio.
2. Sync Gradle.
3. Run the `app` configuration on an emulator or device.

Optional CLI build:

```bash
./gradlew assembleDebug
```

On Windows PowerShell:

```powershell
.\gradlew.bat assembleDebug
```

## Current Notes

- `PreferencesScreen` exists but is not part of the active main navigation flow.
- App database uses `fallbackToDestructiveMigration()` (local data can reset on schema changes).

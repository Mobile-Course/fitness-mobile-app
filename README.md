# FitTrack - Personal Fitness Companion 🏋️‍♂️

FitTrack is a modern, high-performance Android application designed to help users track their fitness journey, share progress, and get AI-powered training tips. Built with the latest Android technologies, it offers a premium user experience with a sleek dark-themed aesthetic.

## 📱 Features

-   **🔐 Secure Mock Auth**: Sleek Login and Signup screens with field validation.
-   **📰 Community Feed**: Stay updated with the latest workouts and posts from the community.
-   **👤 Comprehensive Profile**: Track your workouts, streaks, and posts. Earn achievements as you progress.
-   **⚙️ Advanced Preferences**: Customize your app experience with notification toggles, theme settings (Dark Mode), and unit preferences.
-   **✏️ Post Creation (Ready for Logic)**: A well-structured stub for creating new fitness posts.
-   **🤖 AI Fitness Coach (Ready for Logic)**: Integration-ready stub for personalized AI-generated training advice.

## 🛠 Tech Stack

-   **Language**: [Kotlin](https://kotlinlang.org/)
-   **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
-   **Architecture**: MVVM (Model-View-ViewModel) with StateFlow
-   **Navigation**: [Jetpack Compose Navigation](https://developer.android.com/jetpack/compose/navigation)
-   **Asynchronous Programming**: Coroutines & Flow

## 📂 Project Structure

```text
app/src/main/java/com/fitness/app/
├── navigation/          # Navigation routes and Graph (NavGraph.kt)
├── ui/
│   ├── base/            # Base classes (BaseViewModel)
│   ├── theme/           # Design System (Color, Type, Theme)
│   └── screens/         # Feature-specific screens
│       ├── login/       # Login UI & ViewModel
│       ├── signup/      # Signup UI & ViewModel
│       ├── main/        # Bottom Nav Host (MainScreen.kt)
│       ├── feed/        # Community Feed
│       ├── profile/     # User Profile & Stats
│       ├── preferences/ # Settings & Logout
│       ├── post/        # Post creation (Stub)
│       └── aitips/      # AI Training Tips (Stub)
```

## 🚀 Getting Started

### Prerequisites

-   [Android Studio Hedgehog](https://developer.android.com/studio) or newer.
-   JDK 17.

### Running the App

1.  Clone the repository.
2.  Open the project in Android Studio.
3.  Wait for Gradle sync to complete.
4.  Run the `app` module on an emulator (API 24+) or a physical device.

*Made with ❤️ for Fitness Enthusiasts.*
*By Guy Yablonka and Ethan Larrar*
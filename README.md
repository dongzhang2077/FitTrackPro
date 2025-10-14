# FitTrackPro

FitTrackPro is a comprehensive Android application designed to help users track their fitness journeys, manage workout plans, monitor progress, and achieve their health goals. Built with modern Android development practices, it provides an intuitive interface for workout tracking, exercise libraries, and personal records management.

## Features

- **User Authentication**: Secure login and registration system.
- **Workout Planning**: Create, copy, and manage personalized workout plans.
- **Exercise Library**: Integrated with Wger API for a vast database of exercises.
- **Session Tracking**: Real-time workout sessions with timers and progress tracking.
- **Progress Monitoring**: Weekly, monthly, and yearly workout summaries with visualizations.
- **Personal Records**: Track and notify personal bests in exercises.
- **Profile Management**: Customize user profiles with avatars, names, and weight goals.
- **Onboarding**: Guided setup for new users including avatar selection and goal setting.
- **Data Synchronization**: Auto-sync capabilities for data consistency.

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Use Cases
- **Dependency Injection**: Hilt
- **Database**: Room
- **Networking**: Retrofit for API calls (Wger API integration)
- **Navigation**: Jetpack Navigation Component
- **Coroutines & Flow**: For asynchronous operations
- **Testing**: Unit tests with JUnit and instrumented tests

## Installation

1. Clone the repository:
   ```
   git clone https://github.com/yourusername/FitTrackPro.git
   ```

2. Open the project in Android Studio.

3. Sync the project with Gradle files.

4. Build and run the app on an emulator or physical device (API level 21+).

## Usage

- **Onboarding**: New users are guided through profile setup, including avatar selection and initial goals.
- **Home Screen**: View daily recommendations, weekly goals, and quick stats.
- **Workout Tab**: Browse, search, and start workout plans.
- **Progress Tab**: Analyze workout history with charts and summaries.
- **Profile**: Edit personal information, view achievements, and manage settings.

For detailed usage, refer to the in-app guides or documentation.

## Project Structure

- `app/src/main/java/com/domcheung/fittrackpro/`: Core application code
  - `data/`: Data layer (local DB, remote API, repositories)
  - `domain/`: Business logic (use cases)
  - `presentation/`: UI components and ViewModels
  - `di/`: Dependency injection modules
  - `navigation/`: App navigation setup
- `app/src/test/`: Unit tests
- `app/src/androidTest/`: Instrumented tests

## Contributing

1. Fork the repository.
2. Create a feature branch (`git checkout -b feature/AmazingFeature`).
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4. Push to the branch (`git push origin feature/AmazingFeature`).
5. Open a Pull Request.

Please ensure code follows Kotlin conventions and includes tests where applicable.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Wger API for exercise data
- Jetpack libraries for modern Android development
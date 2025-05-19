# Smart To-Do List App

A professional Android To-Do list application with smart reminders, tag-based organization, voice input for task creation, and a Pomodoro timer per task.

## Features

- Add/edit/delete tasks
- Task priority levels (Low, Medium, High)
- Tag-based organization (Work, Personal, Urgent, Other)
- Voice input for task creation
- Smart reminders with notifications
- Pomodoro timer for focused work sessions
- Dark mode support

## How to Run the App

### Prerequisites

- Android Studio (latest version recommended)
- Android SDK (minimum API level 24)
- An Android device or emulator

### Steps to Run

1. **Open the project in Android Studio**:
   - Launch Android Studio
   - Select "Open an Existing Project"
   - Navigate to the `TodoListApp` folder and select it

2. **Sync Gradle**:
   - Android Studio should automatically sync the Gradle files
   - If not, click on "Sync Project with Gradle Files" in the toolbar

3. **Build the project**:
   - Click on "Build" > "Make Project" or press Ctrl+F9 (Windows/Linux) or Cmd+F9 (Mac)

4. **Run the app**:
   - Connect an Android device via USB or start an emulator
   - Click on "Run" > "Run 'app'" or press Shift+F10 (Windows/Linux) or Control+R (Mac)
   - Select your device/emulator when prompted

5. **Using the app**:
   - Add a new task by clicking the "+" button
   - Use voice input by clicking the microphone button
   - Filter tasks by clicking on the tag chips
   - Start a Pomodoro timer by clicking the "Start Pomodoro" button on a task

## Project Structure

- **data**: Database entities, DAOs, and repository
- **ui**: Activities, view models, and adapters
- **notification**: Notification and alarm-related classes

## Troubleshooting

If you encounter any issues:

1. **Gradle sync fails**:
   - Check your internet connection
   - Update Android Studio and Gradle plugin
   - Invalidate caches and restart (File > Invalidate Caches / Restart)

2. **Build errors**:
   - Check that all dependencies are correctly specified in the build.gradle files
   - Make sure you have the required SDK versions installed

3. **Runtime errors**:
   - Check Logcat for detailed error messages
   - Ensure all required permissions are granted on the device

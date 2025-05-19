# Smart To-Do List Web App

A web-based To-Do list application with smart organization, voice input, and Pomodoro timer functionality.

## Features

- Add/edit/delete tasks
- Task priority levels (Low, Medium, High)
- Tag-based organization (Work, Personal, Urgent, Other)
- Voice input for task creation
- Dark/Light theme toggle
- Pomodoro timer for focused work sessions
- Local storage for task persistence

## How to Run in VS Code

### Prerequisites

- Node.js installed on your system
- VS Code

### Steps to Run

1. **Open the project in VS Code**:
   - Launch VS Code
   - Open the `WebTodoApp` folder

2. **Install the Live Server extension**:
   - Go to Extensions (Ctrl+Shift+X)
   - Search for "Live Server" by Ritwick Dey
   - Install the extension

3. **Run with Live Server**:
   - Right-click on `index.html`
   - Select "Open with Live Server"
   - The app will open in your default browser

4. **Alternative: Run with Node.js**:
   - Open a terminal in VS Code (Ctrl+`)
   - Run `node server.js`
   - Open your browser and navigate to `http://localhost:3000`

## Using the App

1. **Add a task**:
   - Click the "+" button in the bottom right
   - Fill in the task details
   - Click "Save Task"

2. **Use voice input**:
   - Click the microphone button in the bottom left
   - Speak your task title
   - The Add Task modal will open with your spoken text

3. **Filter tasks**:
   - Click on the tag chips (Work, Personal, Urgent) to filter tasks

4. **Start a Pomodoro timer**:
   - Click "Start Pomodoro" on any task
   - Use the Start/Pause and Reset buttons to control the timer

5. **Toggle dark/light theme**:
   - Click the moon/sun icon in the top right

## Project Structure

- `index.html`: Main HTML file
- `styles.css`: CSS styles
- `app.js`: JavaScript functionality
- `server.js`: Simple Node.js server for running the app

## Troubleshooting

- **Voice input not working**: Make sure your browser supports the Web Speech API and you've granted microphone permissions
- **Tasks not saving**: Check if localStorage is enabled in your browser
- **Server not starting**: Make sure Node.js is installed and you're in the correct directory

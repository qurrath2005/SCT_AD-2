// DOM Elements
const tasksList = document.getElementById('tasks-list');
const addTaskBtn = document.getElementById('add-task-btn');
const taskModal = document.getElementById('task-modal');
const modalTitle = document.getElementById('modal-title');
const taskForm = document.getElementById('task-form');
const closeModalBtns = document.querySelectorAll('.close-modal');
const voiceInputBtn = document.getElementById('voice-input-btn');
const voiceStatus = document.querySelector('.voice-status');
const filterChips = document.querySelectorAll('.chip');
const themeToggle = document.querySelector('.theme-toggle');
const pomodoroModal = document.getElementById('pomodoro-modal');
const pomodoroTaskTitle = document.getElementById('pomodoro-task-title');
const timerDisplay = document.querySelector('.timer-display');
const progressBar = document.querySelector('.progress-bar');
const startPauseBtn = document.getElementById('start-pause-btn');
const resetBtn = document.getElementById('reset-btn');
const timerStatus = document.querySelector('.timer-status');

// App State
let tasks = JSON.parse(localStorage.getItem('tasks')) || [];
let currentTaskId = null;
let activeFilters = [];
let isDarkTheme = localStorage.getItem('darkTheme') === 'true';
let timer = null;
let timeLeft = 25 * 60; // 25 minutes in seconds
let isTimerRunning = false;
let isBreakTime = false;
let currentPomodoroTaskId = null;

// Initialize the app
function init() {
    renderTasks();
    setupEventListeners();
    applyTheme();
}

// Setup Event Listeners
function setupEventListeners() {
    // Add task button
    addTaskBtn.addEventListener('click', () => {
        currentTaskId = null;
        modalTitle.textContent = 'Add Task';
        taskForm.reset();
        openModal(taskModal);
    });

    // Close modals
    closeModalBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            closeAllModals();
        });
    });

    // Submit task form
    taskForm.addEventListener('submit', handleTaskFormSubmit);

    // Voice input
    voiceInputBtn.addEventListener('click', startVoiceRecognition);

    // Filter chips
    filterChips.forEach(chip => {
        chip.addEventListener('click', () => {
            const tag = chip.dataset.tag;
            chip.classList.toggle('active');
            
            if (chip.classList.contains('active')) {
                activeFilters.push(tag);
            } else {
                activeFilters = activeFilters.filter(filter => filter !== tag);
            }
            
            renderTasks();
        });
    });

    // Theme toggle
    themeToggle.addEventListener('click', toggleTheme);

    // Pomodoro timer controls
    startPauseBtn.addEventListener('click', toggleTimer);
    resetBtn.addEventListener('click', resetTimer);

    // Close modal when clicking outside
    window.addEventListener('click', (e) => {
        if (e.target === taskModal) {
            closeAllModals();
        }
        if (e.target === pomodoroModal) {
            closeAllModals();
        }
    });
}

// Render Tasks
function renderTasks() {
    const filteredTasks = activeFilters.length > 0 
        ? tasks.filter(task => task.tags.some(tag => activeFilters.includes(tag)))
        : tasks;
    
    if (filteredTasks.length === 0) {
        tasksList.innerHTML = `
            <div class="empty-state">
                <p>No tasks yet. Add one!</p>
            </div>
        `;
        return;
    }
    
    tasksList.innerHTML = '';
    
    filteredTasks.forEach(task => {
        const taskElement = createTaskElement(task);
        tasksList.appendChild(taskElement);
    });
}

// Create Task Element
function createTaskElement(task) {
    const taskElement = document.createElement('div');
    taskElement.className = 'task-item';
    taskElement.dataset.id = task.id;
    
    const priorityClass = task.priority === 'low' ? 'low' : task.priority === 'high' ? 'high' : '';
    
    const tagsHtml = task.tags.map(tag => `
        <span class="task-tag ${tag}">${capitalizeFirstLetter(tag)}</span>
    `).join('');
    
    const dueDateText = task.dueDate ? formatDueDate(task.dueDate, task.dueTime) : '';
    
    taskElement.innerHTML = `
        <div class="task-priority-indicator ${priorityClass}"></div>
        <div class="task-content">
            <div class="task-header">
                <div class="task-title">${task.title}</div>
                <div class="task-actions">
                    <button class="task-action-btn edit-task" aria-label="Edit task">
                        <span class="material-icons">edit</span>
                    </button>
                    <button class="task-action-btn delete-task" aria-label="Delete task">
                        <span class="material-icons">delete</span>
                    </button>
                </div>
            </div>
            ${task.description ? `<div class="task-description">${task.description}</div>` : ''}
            ${dueDateText ? `<div class="task-due-date">${dueDateText}</div>` : ''}
            <div class="task-tags">${tagsHtml}</div>
            <div class="task-pomodoro">
                <button class="pomodoro-btn" data-id="${task.id}">Start Pomodoro</button>
            </div>
        </div>
    `;
    
    // Add event listeners
    taskElement.querySelector('.edit-task').addEventListener('click', () => {
        editTask(task.id);
    });
    
    taskElement.querySelector('.delete-task').addEventListener('click', () => {
        deleteTask(task.id);
    });
    
    taskElement.querySelector('.pomodoro-btn').addEventListener('click', () => {
        startPomodoro(task.id);
    });
    
    return taskElement;
}

// Handle Task Form Submit
function handleTaskFormSubmit(e) {
    e.preventDefault();
    
    const title = document.getElementById('task-title').value;
    const description = document.getElementById('task-description').value;
    const dueDate = document.getElementById('task-due-date').value;
    const dueTime = document.getElementById('task-due-time').value;
    const priority = document.querySelector('input[name="priority"]:checked').value;
    
    const tagsInputs = document.querySelectorAll('input[name="tags"]:checked');
    const tags = Array.from(tagsInputs).map(input => input.value);
    
    if (currentTaskId) {
        // Update existing task
        const taskIndex = tasks.findIndex(task => task.id === currentTaskId);
        if (taskIndex !== -1) {
            tasks[taskIndex] = {
                ...tasks[taskIndex],
                title,
                description,
                dueDate,
                dueTime,
                priority,
                tags
            };
        }
    } else {
        // Add new task
        const newTask = {
            id: Date.now().toString(),
            title,
            description,
            dueDate,
            dueTime,
            priority,
            tags,
            createdAt: new Date().toISOString(),
            pomodoroCount: 0
        };
        
        tasks.push(newTask);
    }
    
    // Save to localStorage
    saveTasks();
    
    // Close modal and render tasks
    closeAllModals();
    renderTasks();
}

// Edit Task
function editTask(taskId) {
    const task = tasks.find(task => task.id === taskId);
    if (!task) return;
    
    currentTaskId = taskId;
    modalTitle.textContent = 'Edit Task';
    
    document.getElementById('task-title').value = task.title;
    document.getElementById('task-description').value = task.description || '';
    document.getElementById('task-due-date').value = task.dueDate || '';
    document.getElementById('task-due-time').value = task.dueTime || '';
    
    // Set priority
    const priorityInput = document.querySelector(`input[name="priority"][value="${task.priority}"]`);
    if (priorityInput) priorityInput.checked = true;
    
    // Set tags
    document.querySelectorAll('input[name="tags"]').forEach(input => {
        input.checked = task.tags.includes(input.value);
    });
    
    openModal(taskModal);
}

// Delete Task
function deleteTask(taskId) {
    if (confirm('Are you sure you want to delete this task?')) {
        tasks = tasks.filter(task => task.id !== taskId);
        saveTasks();
        renderTasks();
    }
}

// Start Pomodoro
function startPomodoro(taskId) {
    const task = tasks.find(task => task.id === taskId);
    if (!task) return;
    
    currentPomodoroTaskId = taskId;
    pomodoroTaskTitle.textContent = task.title;
    
    // Reset timer
    isBreakTime = false;
    timeLeft = 25 * 60; // 25 minutes
    isTimerRunning = false;
    startPauseBtn.textContent = 'Start';
    timerStatus.textContent = 'Focus Time';
    updateTimerDisplay();
    progressBar.style.width = '100%';
    progressBar.style.backgroundColor = 'var(--pomodoro-active)';
    
    openModal(pomodoroModal);
}

// Toggle Timer
function toggleTimer() {
    if (isTimerRunning) {
        pauseTimer();
    } else {
        startTimer();
    }
}

// Start Timer
function startTimer() {
    isTimerRunning = true;
    startPauseBtn.textContent = 'Pause';
    
    const totalTime = isBreakTime ? 5 * 60 : 25 * 60; // 5 or 25 minutes
    
    timer = setInterval(() => {
        timeLeft--;
        updateTimerDisplay();
        
        // Update progress bar
        const progressPercent = (timeLeft / totalTime) * 100;
        progressBar.style.width = `${progressPercent}%`;
        
        if (timeLeft <= 0) {
            clearInterval(timer);
            
            if (!isBreakTime) {
                // Pomodoro completed
                incrementPomodoroCount();
                isBreakTime = true;
                timeLeft = 5 * 60; // 5 minutes break
                timerStatus.textContent = 'Break Time';
                progressBar.style.backgroundColor = 'var(--pomodoro-break)';
                alert('Pomodoro completed! Take a break.');
            } else {
                // Break completed
                isBreakTime = false;
                timeLeft = 25 * 60; // Back to 25 minutes
                timerStatus.textContent = 'Focus Time';
                progressBar.style.backgroundColor = 'var(--pomodoro-active)';
                alert('Break completed! Ready for another Pomodoro?');
            }
            
            isTimerRunning = false;
            startPauseBtn.textContent = 'Start';
            updateTimerDisplay();
        }
    }, 1000);
}

// Pause Timer
function pauseTimer() {
    clearInterval(timer);
    isTimerRunning = false;
    startPauseBtn.textContent = 'Start';
    progressBar.style.backgroundColor = 'var(--pomodoro-paused)';
}

// Reset Timer
function resetTimer() {
    clearInterval(timer);
    timeLeft = isBreakTime ? 5 * 60 : 25 * 60;
    isTimerRunning = false;
    startPauseBtn.textContent = 'Start';
    updateTimerDisplay();
    progressBar.style.width = '100%';
    progressBar.style.backgroundColor = isBreakTime ? 'var(--pomodoro-break)' : 'var(--pomodoro-active)';
}

// Update Timer Display
function updateTimerDisplay() {
    const minutes = Math.floor(timeLeft / 60);
    const seconds = timeLeft % 60;
    timerDisplay.textContent = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
}

// Increment Pomodoro Count
function incrementPomodoroCount() {
    if (!currentPomodoroTaskId) return;
    
    const taskIndex = tasks.findIndex(task => task.id === currentPomodoroTaskId);
    if (taskIndex !== -1) {
        tasks[taskIndex].pomodoroCount = (tasks[taskIndex].pomodoroCount || 0) + 1;
        saveTasks();
    }
}

// Voice Recognition
function startVoiceRecognition() {
    if (!('webkitSpeechRecognition' in window)) {
        alert('Voice recognition is not supported in your browser.');
        return;
    }
    
    const recognition = new webkitSpeechRecognition();
    recognition.continuous = false;
    recognition.interimResults = false;
    
    voiceStatus.textContent = 'Listening...';
    voiceStatus.classList.add('active');
    
    recognition.onresult = (event) => {
        const transcript = event.results[0][0].transcript;
        
        // Open add task modal with the transcript as title
        currentTaskId = null;
        modalTitle.textContent = 'Add Task';
        taskForm.reset();
        document.getElementById('task-title').value = transcript;
        openModal(taskModal);
    };
    
    recognition.onerror = () => {
        voiceStatus.textContent = 'Error. Try again.';
        setTimeout(() => {
            voiceStatus.classList.remove('active');
        }, 2000);
    };
    
    recognition.onend = () => {
        voiceStatus.classList.remove('active');
    };
    
    recognition.start();
}

// Toggle Theme
function toggleTheme() {
    isDarkTheme = !isDarkTheme;
    localStorage.setItem('darkTheme', isDarkTheme);
    applyTheme();
}

// Apply Theme
function applyTheme() {
    if (isDarkTheme) {
        document.body.classList.add('dark-theme');
        themeToggle.innerHTML = '<span class="material-icons">light_mode</span>';
    } else {
        document.body.classList.remove('dark-theme');
        themeToggle.innerHTML = '<span class="material-icons">dark_mode</span>';
    }
}

// Helper Functions
function openModal(modal) {
    modal.style.display = 'block';
}

function closeAllModals() {
    taskModal.style.display = 'none';
    pomodoroModal.style.display = 'none';
}

function saveTasks() {
    localStorage.setItem('tasks', JSON.stringify(tasks));
}

function formatDueDate(date, time) {
    if (!date) return '';
    
    const dateObj = new Date(date);
    const options = { month: 'short', day: 'numeric', year: 'numeric' };
    let formattedDate = dateObj.toLocaleDateString('en-US', options);
    
    if (time) {
        formattedDate += ` at ${time}`;
    }
    
    return `Due: ${formattedDate}`;
}

function capitalizeFirstLetter(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
}

// Initialize the app
document.addEventListener('DOMContentLoaded', init);

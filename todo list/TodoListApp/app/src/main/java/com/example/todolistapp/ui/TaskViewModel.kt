package com.example.todolistapp.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.todolistapp.data.Task
import com.example.todolistapp.data.TaskRepository
import com.example.todolistapp.data.TaskTag
import kotlinx.coroutines.launch
import java.util.Date

/**
 * ViewModel for managing task data
 */
class TaskViewModel(private val repository: TaskRepository) : ViewModel() {
    // All tasks
    val allTasks: LiveData<List<Task>> = repository.allTasks
    
    // Incomplete tasks
    val incompleteTasks: LiveData<List<Task>> = repository.incompleteTasks
    
    // Completed tasks
    val completedTasks: LiveData<List<Task>> = repository.completedTasks
    
    // Tasks due today
    val tasksDueToday: LiveData<List<Task>> = repository.tasksDueToday
    
    // Tasks due this week
    val tasksDueThisWeek: LiveData<List<Task>> = repository.tasksDueThisWeek
    
    // Currently filtered tasks
    private val _filteredTasks = MutableLiveData<List<Task>>()
    val filteredTasks: LiveData<List<Task>> = _filteredTasks
    
    // Currently selected tags for filtering
    private val _selectedTags = MutableLiveData<Set<TaskTag>>(emptySet())
    val selectedTags: LiveData<Set<TaskTag>> = _selectedTags
    
    // Whether to show completed tasks
    private val _showCompleted = MutableLiveData(false)
    val showCompleted: LiveData<Boolean> = _showCompleted
    
    /**
     * Insert a new task
     * @param task The task to insert
     * @return The ID of the inserted task
     */
    fun insertTask(task: Task) = viewModelScope.launch {
        repository.insertTask(task)
    }
    
    /**
     * Update an existing task
     * @param task The task to update
     */
    fun updateTask(task: Task) = viewModelScope.launch {
        repository.updateTask(task)
    }
    
    /**
     * Delete a task
     * @param task The task to delete
     */
    fun deleteTask(task: Task) = viewModelScope.launch {
        repository.deleteTask(task)
    }
    
    /**
     * Get a task by ID
     * @param taskId The ID of the task to retrieve
     * @return The task with the specified ID
     */
    suspend fun getTaskById(taskId: Long): Task? {
        return repository.getTaskById(taskId)
    }
    
    /**
     * Update task completion status
     * @param taskId The ID of the task to update
     * @param isCompleted The new completion status
     */
    fun updateTaskCompletionStatus(taskId: Long, isCompleted: Boolean) = viewModelScope.launch {
        repository.updateTaskCompletionStatus(taskId, isCompleted)
    }
    
    /**
     * Update pomodoro count for a task
     * @param taskId The ID of the task to update
     * @param count The new pomodoro count
     */
    fun updatePomodoroCount(taskId: Long, count: Int) = viewModelScope.launch {
        repository.updatePomodoroCount(taskId, count)
    }
    
    /**
     * Update last notification time for a task
     * @param taskId The ID of the task to update
     * @param time The new notification time
     */
    fun updateLastNotificationTime(taskId: Long, time: Date) = viewModelScope.launch {
        repository.updateLastNotificationTime(taskId, time)
    }
    
    /**
     * Toggle tag selection for filtering
     * @param tag The tag to toggle
     */
    fun toggleTagSelection(tag: TaskTag) {
        val currentTags = _selectedTags.value ?: emptySet()
        _selectedTags.value = if (currentTags.contains(tag)) {
            currentTags - tag
        } else {
            currentTags + tag
        }
        applyFilters()
    }
    
    /**
     * Toggle showing completed tasks
     */
    fun toggleShowCompleted() {
        _showCompleted.value = !(_showCompleted.value ?: false)
        applyFilters()
    }
    
    /**
     * Apply current filters to tasks
     */
    private fun applyFilters() {
        val tags = _selectedTags.value ?: emptySet()
        val showCompleted = _showCompleted.value ?: false
        
        viewModelScope.launch {
            val tasks = allTasks.value ?: emptyList()
            
            _filteredTasks.value = tasks.filter { task ->
                // Filter by completion status
                (showCompleted || !task.isCompleted) &&
                // Filter by tags (if any tags are selected)
                (tags.isEmpty() || task.tags.any { it in tags })
            }
        }
    }
}

/**
 * Factory for creating TaskViewModel instances
 */
class TaskViewModelFactory(private val repository: TaskRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

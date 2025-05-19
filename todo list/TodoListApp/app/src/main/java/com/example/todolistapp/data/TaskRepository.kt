package com.example.todolistapp.data

import androidx.lifecycle.LiveData
import java.util.Calendar
import java.util.Date

/**
 * Repository for accessing task data
 */
class TaskRepository(private val taskDao: TaskDao) {
    /**
     * Get all tasks
     */
    val allTasks: LiveData<List<Task>> = taskDao.getAllTasks()
    
    /**
     * Get incomplete tasks
     */
    val incompleteTasks: LiveData<List<Task>> = taskDao.getTasksByCompletionStatus(false)
    
    /**
     * Get completed tasks
     */
    val completedTasks: LiveData<List<Task>> = taskDao.getTasksByCompletionStatus(true)
    
    /**
     * Get tasks with work tag
     */
    val workTasks: LiveData<List<Task>> = taskDao.getTasksByTag(TaskTag.WORK.name)
    
    /**
     * Get tasks with personal tag
     */
    val personalTasks: LiveData<List<Task>> = taskDao.getTasksByTag(TaskTag.PERSONAL.name)
    
    /**
     * Get tasks with urgent tag
     */
    val urgentTasks: LiveData<List<Task>> = taskDao.getTasksByTag(TaskTag.URGENT.name)
    
    /**
     * Get tasks due today
     */
    val tasksDueToday: LiveData<List<Task>> = getTasksDueInRange(0, 1)
    
    /**
     * Get tasks due this week
     */
    val tasksDueThisWeek: LiveData<List<Task>> = getTasksDueInRange(0, 7)
    
    /**
     * Insert a task
     * @param task The task to insert
     * @return The ID of the inserted task
     */
    suspend fun insertTask(task: Task): Long {
        return taskDao.insertTask(task)
    }
    
    /**
     * Update a task
     * @param task The task to update
     */
    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }
    
    /**
     * Delete a task
     * @param task The task to delete
     */
    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }
    
    /**
     * Get a task by ID
     * @param taskId The ID of the task to retrieve
     * @return The task with the specified ID
     */
    suspend fun getTaskById(taskId: Long): Task? {
        return taskDao.getTaskById(taskId)
    }
    
    /**
     * Update task completion status
     * @param taskId The ID of the task to update
     * @param isCompleted The new completion status
     */
    suspend fun updateTaskCompletionStatus(taskId: Long, isCompleted: Boolean) {
        taskDao.updateTaskCompletionStatus(taskId, isCompleted)
    }
    
    /**
     * Update pomodoro count for a task
     * @param taskId The ID of the task to update
     * @param count The new pomodoro count
     */
    suspend fun updatePomodoroCount(taskId: Long, count: Int) {
        taskDao.updatePomodoroCount(taskId, count)
    }
    
    /**
     * Update last notification time for a task
     * @param taskId The ID of the task to update
     * @param time The new notification time
     */
    suspend fun updateLastNotificationTime(taskId: Long, time: Date) {
        taskDao.updateLastNotificationTime(taskId, time)
    }
    
    /**
     * Get tasks due within a specified date range
     * @param startDaysOffset The number of days from today to start the range
     * @param endDaysOffset The number of days from today to end the range
     * @return LiveData list of tasks due within the specified date range
     */
    private fun getTasksDueInRange(startDaysOffset: Int, endDaysOffset: Int): LiveData<List<Task>> {
        val calendar = Calendar.getInstance()
        
        // Set to start of day
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        // Add start offset
        calendar.add(Calendar.DAY_OF_YEAR, startDaysOffset)
        val startDate = calendar.time
        
        // Add end offset
        calendar.add(Calendar.DAY_OF_YEAR, endDaysOffset - startDaysOffset)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endDate = calendar.time
        
        return taskDao.getTasksDueSoon(startDate, endDate)
    }
}

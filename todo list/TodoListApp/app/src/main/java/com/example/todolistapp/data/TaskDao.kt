package com.example.todolistapp.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import java.util.Date

/**
 * Data Access Object (DAO) for Task entity
 */
@Dao
interface TaskDao {
    /**
     * Insert a new task
     * @param task The task to insert
     * @return The ID of the inserted task
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    /**
     * Update an existing task
     * @param task The task to update
     */
    @Update
    suspend fun updateTask(task: Task)

    /**
     * Delete a task
     * @param task The task to delete
     */
    @Delete
    suspend fun deleteTask(task: Task)

    /**
     * Get all tasks
     * @return LiveData list of all tasks
     */
    @Query("SELECT * FROM tasks ORDER BY dueDate ASC, priority DESC, createdDate DESC")
    fun getAllTasks(): LiveData<List<Task>>

    /**
     * Get tasks by completion status
     * @param isCompleted Whether to get completed or incomplete tasks
     * @return LiveData list of tasks with the specified completion status
     */
    @Query("SELECT * FROM tasks WHERE isCompleted = :isCompleted ORDER BY dueDate ASC, priority DESC, createdDate DESC")
    fun getTasksByCompletionStatus(isCompleted: Boolean): LiveData<List<Task>>

    /**
     * Get tasks by tag
     * @param tag The tag to filter by
     * @return LiveData list of tasks with the specified tag
     */
    @Query("SELECT * FROM tasks WHERE tags LIKE '%' || :tag || '%' ORDER BY dueDate ASC, priority DESC, createdDate DESC")
    fun getTasksByTag(tag: String): LiveData<List<Task>>

    /**
     * Get tasks due soon
     * @param startDate The start date for the range
     * @param endDate The end date for the range
     * @return LiveData list of tasks due within the specified date range
     */
    @Query("SELECT * FROM tasks WHERE dueDate BETWEEN :startDate AND :endDate AND isCompleted = 0 ORDER BY dueDate ASC")
    fun getTasksDueSoon(startDate: Date, endDate: Date): LiveData<List<Task>>

    /**
     * Get task by ID
     * @param taskId The ID of the task to retrieve
     * @return The task with the specified ID
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): Task?

    /**
     * Update task completion status
     * @param taskId The ID of the task to update
     * @param isCompleted The new completion status
     */
    @Query("UPDATE tasks SET isCompleted = :isCompleted WHERE id = :taskId")
    suspend fun updateTaskCompletionStatus(taskId: Long, isCompleted: Boolean)

    /**
     * Update pomodoro count for a task
     * @param taskId The ID of the task to update
     * @param count The new pomodoro count
     */
    @Query("UPDATE tasks SET pomodoroCount = :count WHERE id = :taskId")
    suspend fun updatePomodoroCount(taskId: Long, count: Int)

    /**
     * Update last notification time for a task
     * @param taskId The ID of the task to update
     * @param time The new notification time
     */
    @Query("UPDATE tasks SET lastNotificationTime = :time WHERE id = :taskId")
    suspend fun updateLastNotificationTime(taskId: Long, time: Date)
}

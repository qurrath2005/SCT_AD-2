package com.example.todolistapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Date

/**
 * Enum class representing task priority levels
 */
enum class TaskPriority {
    LOW, MEDIUM, HIGH
}

/**
 * Enum class representing task tags
 */
enum class TaskTag {
    WORK, PERSONAL, URGENT, OTHER
}

/**
 * Entity class representing a task in the database
 */
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val title: String,
    val description: String = "",
    val dueDate: Date? = null,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val isCompleted: Boolean = false,
    val createdDate: Date = Date(),
    val tags: List<TaskTag> = emptyList(),
    val pomodoroCount: Int = 0,
    val lastNotificationTime: Date? = null
)

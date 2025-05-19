package com.example.todolistapp.data

import androidx.room.TypeConverter
import java.util.Date

/**
 * Type converters for Room database
 */
class Converters {
    /**
     * Convert Date to Long timestamp for storage in database
     */
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    /**
     * Convert Long timestamp to Date for retrieval from database
     */
    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }

    /**
     * Convert List of TaskTag to String for storage in database
     */
    @TypeConverter
    fun fromTaskTagList(tags: List<TaskTag>): String {
        return tags.joinToString(",") { it.name }
    }

    /**
     * Convert String to List of TaskTag for retrieval from database
     */
    @TypeConverter
    fun toTaskTagList(tagsString: String): List<TaskTag> {
        if (tagsString.isEmpty()) return emptyList()
        return tagsString.split(",").map { TaskTag.valueOf(it) }
    }

    /**
     * Convert TaskPriority to String for storage in database
     */
    @TypeConverter
    fun fromTaskPriority(priority: TaskPriority): String {
        return priority.name
    }

    /**
     * Convert String to TaskPriority for retrieval from database
     */
    @TypeConverter
    fun toTaskPriority(priorityString: String): TaskPriority {
        return TaskPriority.valueOf(priorityString)
    }
}

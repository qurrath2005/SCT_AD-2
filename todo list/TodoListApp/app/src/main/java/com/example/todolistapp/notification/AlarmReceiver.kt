package com.example.todolistapp.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.todolistapp.TodoApplication
import com.example.todolistapp.data.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

/**
 * BroadcastReceiver for handling task reminder alarms
 */
class AlarmReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1)
        val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Task reminder"
        
        if (taskId != -1L) {
            // Show notification
            val notificationHelper = NotificationHelper(context)
            notificationHelper.showTaskNotification(taskId, taskTitle)
            
            // Update last notification time in database
            val repository = (context.applicationContext as TodoApplication).repository
            CoroutineScope(Dispatchers.IO).launch {
                repository.updateLastNotificationTime(taskId, Date())
            }
        }
    }
    
    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
    }
}

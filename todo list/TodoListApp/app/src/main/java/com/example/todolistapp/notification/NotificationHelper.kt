package com.example.todolistapp.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.todolistapp.R
import com.example.todolistapp.TodoApplication
import com.example.todolistapp.data.Task
import com.example.todolistapp.ui.MainActivity
import java.util.Calendar
import java.util.Date

/**
 * Helper class for managing task notifications
 */
class NotificationHelper(private val context: Context) {
    
    /**
     * Schedule a notification for a task
     * @param task The task to schedule a notification for
     */
    fun scheduleTaskReminder(task: Task) {
        task.dueDate?.let { dueDate ->
            // Only schedule if the task is not completed and has a due date
            if (!task.isCompleted) {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                
                // Create intent for the alarm receiver
                val intent = Intent(context, AlarmReceiver::class.java).apply {
                    putExtra(AlarmReceiver.EXTRA_TASK_ID, task.id)
                    putExtra(AlarmReceiver.EXTRA_TASK_TITLE, task.title)
                }
                
                // Create pending intent
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    task.id.toInt(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                
                // Calculate notification time (1 hour before due date)
                val calendar = Calendar.getInstance()
                calendar.time = dueDate
                calendar.add(Calendar.HOUR, -1)
                
                // Only schedule if the notification time is in the future
                if (calendar.timeInMillis > System.currentTimeMillis()) {
                    // Schedule the alarm
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            }
        }
    }
    
    /**
     * Cancel a scheduled notification for a task
     * @param taskId The ID of the task to cancel the notification for
     */
    fun cancelTaskReminder(taskId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Create intent for the alarm receiver
        val intent = Intent(context, AlarmReceiver::class.java)
        
        // Create pending intent with the same parameters as when scheduling
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Cancel the alarm
        alarmManager.cancel(pendingIntent)
    }
    
    /**
     * Show a notification for a task
     * @param taskId The ID of the task to show a notification for
     * @param taskTitle The title of the task
     */
    fun showTaskNotification(taskId: Long, taskTitle: String) {
        // Create intent for when the notification is tapped
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build the notification
        val builder = NotificationCompat.Builder(context, TodoApplication.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.task_due_soon))
            .setContentText(taskTitle)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        
        // Show the notification
        with(NotificationManagerCompat.from(context)) {
            notify(taskId.toInt(), builder.build())
        }
    }
}

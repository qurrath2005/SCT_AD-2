package com.example.todolistapp.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.todolistapp.R
import com.example.todolistapp.data.Task
import com.example.todolistapp.data.TaskPriority
import com.example.todolistapp.data.TaskTag
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Adapter for displaying tasks in a RecyclerView
 */
class TaskAdapter(
    private val onTaskCheckedChanged: (Task, Boolean) -> Unit,
    private val onTaskClicked: (Task) -> Unit,
    private val onTaskDeleted: (Task) -> Unit,
    private val onPomodoroClicked: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)
        holder.bind(task)
    }

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val priorityIndicator: View = itemView.findViewById(R.id.priority_indicator)
        private val checkbox: CheckBox = itemView.findViewById(R.id.task_checkbox)
        private val titleTextView: TextView = itemView.findViewById(R.id.task_title)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.task_description)
        private val dueDateTextView: TextView = itemView.findViewById(R.id.task_due_date)
        private val tagsChipGroup: ChipGroup = itemView.findViewById(R.id.task_tags)
        private val menuButton: ImageButton = itemView.findViewById(R.id.task_menu)
        private val pomodoroButton: MaterialButton = itemView.findViewById(R.id.btn_pomodoro)
        
        private val dateFormat = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
        
        fun bind(task: Task) {
            // Set task title and description
            titleTextView.text = task.title
            descriptionTextView.text = task.description
            if (task.description.isEmpty()) {
                descriptionTextView.visibility = View.GONE
            } else {
                descriptionTextView.visibility = View.VISIBLE
            }
            
            // Set priority indicator color
            val priorityColor = when (task.priority) {
                TaskPriority.LOW -> R.color.priority_low
                TaskPriority.MEDIUM -> R.color.priority_medium
                TaskPriority.HIGH -> R.color.priority_high
            }
            priorityIndicator.setBackgroundColor(
                ContextCompat.getColor(itemView.context, priorityColor)
            )
            
            // Set due date
            if (task.dueDate != null) {
                dueDateTextView.text = "Due: ${dateFormat.format(task.dueDate)}"
                dueDateTextView.visibility = View.VISIBLE
            } else {
                dueDateTextView.visibility = View.GONE
            }
            
            // Set checkbox state
            checkbox.isChecked = task.isCompleted
            checkbox.setOnCheckedChangeListener { _, isChecked ->
                onTaskCheckedChanged(task, isChecked)
            }
            
            // Set tags
            tagsChipGroup.removeAllViews()
            for (tag in task.tags) {
                val chip = createTagChip(itemView.context, tag)
                tagsChipGroup.addView(chip)
            }
            
            // Set click listeners
            itemView.setOnClickListener {
                onTaskClicked(task)
            }
            
            menuButton.setOnClickListener {
                showPopupMenu(it, task)
            }
            
            pomodoroButton.setOnClickListener {
                onPomodoroClicked(task)
            }
        }
        
        private fun createTagChip(context: Context, tag: TaskTag): Chip {
            val chip = Chip(context)
            chip.text = when (tag) {
                TaskTag.WORK -> context.getString(R.string.tag_work)
                TaskTag.PERSONAL -> context.getString(R.string.tag_personal)
                TaskTag.URGENT -> context.getString(R.string.tag_urgent)
                TaskTag.OTHER -> context.getString(R.string.tag_other)
            }
            
            val chipColor = when (tag) {
                TaskTag.WORK -> R.color.tag_work
                TaskTag.PERSONAL -> R.color.tag_personal
                TaskTag.URGENT -> R.color.tag_urgent
                TaskTag.OTHER -> R.color.tag_other
            }
            
            chip.chipBackgroundColor = ContextCompat.getColorStateList(context, chipColor)
            chip.setTextColor(ContextCompat.getColor(context, R.color.on_primary))
            chip.isClickable = false
            
            return chip
        }
        
        private fun showPopupMenu(view: View, task: Task) {
            val popup = PopupMenu(view.context, view)
            popup.inflate(R.menu.menu_task_item)
            
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_edit -> {
                        onTaskClicked(task)
                        true
                    }
                    R.id.action_delete -> {
                        onTaskDeleted(task)
                        true
                    }
                    else -> false
                }
            }
            
            popup.show()
        }
    }
}

/**
 * DiffUtil callback for Task items
 */
class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
    override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem == newItem
    }
}

package com.example.todolistapp.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.todolistapp.R
import com.example.todolistapp.TodoApplication
import com.example.todolistapp.data.Task
import com.example.todolistapp.data.TaskPriority
import com.example.todolistapp.data.TaskTag
import com.example.todolistapp.databinding.ActivityAddEditTaskBinding
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddEditTaskActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddEditTaskBinding
    
    private val taskViewModel: TaskViewModel by viewModels {
        TaskViewModelFactory((application as TodoApplication).repository)
    }
    
    private var taskId: Long = -1
    private var dueDate: Date? = null
    private val calendar = Calendar.getInstance()
    
    private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    
    // Voice recognition launcher
    private val voiceRecognitionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            results?.get(0)?.let { spokenText ->
                binding.etTaskTitle.setText(spokenText)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // Check if we're editing an existing task
        taskId = intent.getLongExtra(EXTRA_TASK_ID, -1)
        
        if (taskId != -1L) {
            // We're editing an existing task
            supportActionBar?.title = "Edit Task"
            loadTask()
        } else {
            // We're creating a new task
            supportActionBar?.title = "Add Task"
        }
        
        setupDateTimePickers()
        setupPriorityRadioGroup()
        setupVoiceInput()
        setupSaveButton()
    }
    
    private fun loadTask() {
        lifecycleScope.launch {
            val task = taskViewModel.getTaskById(taskId)
            task?.let {
                // Populate UI with task data
                binding.etTaskTitle.setText(it.title)
                binding.etTaskDescription.setText(it.description)
                
                // Set due date and time
                dueDate = it.dueDate
                dueDate?.let { date ->
                    calendar.time = date
                    updateDateTimeUI()
                }
                
                // Set priority
                when (it.priority) {
                    TaskPriority.LOW -> binding.rbPriorityLow.isChecked = true
                    TaskPriority.MEDIUM -> binding.rbPriorityMedium.isChecked = true
                    TaskPriority.HIGH -> binding.rbPriorityHigh.isChecked = true
                }
                
                // Set tags
                for (tag in it.tags) {
                    when (tag) {
                        TaskTag.WORK -> binding.chipTagWork.isChecked = true
                        TaskTag.PERSONAL -> binding.chipTagPersonal.isChecked = true
                        TaskTag.URGENT -> binding.chipTagUrgent.isChecked = true
                        TaskTag.OTHER -> binding.chipTagOther.isChecked = true
                    }
                }
            }
        }
    }
    
    private fun setupDateTimePickers() {
        binding.etDueDate.setOnClickListener {
            showDatePicker()
        }
        
        binding.etDueTime.setOnClickListener {
            showTimePicker()
        }
    }
    
    private fun showDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            calendar.set(Calendar.YEAR, selectedYear)
            calendar.set(Calendar.MONTH, selectedMonth)
            calendar.set(Calendar.DAY_OF_MONTH, selectedDay)
            updateDateTimeUI()
        }, year, month, day).show()
    }
    
    private fun showTimePicker() {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        
        TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
            calendar.set(Calendar.MINUTE, selectedMinute)
            updateDateTimeUI()
        }, hour, minute, false).show()
    }
    
    private fun updateDateTimeUI() {
        dueDate = calendar.time
        binding.etDueDate.setText(dateFormat.format(dueDate!!))
        binding.etDueTime.setText(timeFormat.format(dueDate!!))
    }
    
    private fun setupPriorityRadioGroup() {
        // Default to medium priority
        binding.rbPriorityMedium.isChecked = true
    }
    
    private fun setupVoiceInput() {
        binding.btnVoiceInput.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_prompt))
            }
            
            try {
                voiceRecognitionLauncher.launch(intent)
            } catch (e: Exception) {
                Toast.makeText(this, R.string.voice_error, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun setupSaveButton() {
        binding.btnSaveTask.setOnClickListener {
            saveTask()
        }
    }
    
    private fun saveTask() {
        val title = binding.etTaskTitle.text.toString().trim()
        if (title.isEmpty()) {
            binding.tilTaskTitle.error = "Title is required"
            return
        }
        
        val description = binding.etTaskDescription.text.toString().trim()
        
        // Get priority
        val priority = when {
            binding.rbPriorityLow.isChecked -> TaskPriority.LOW
            binding.rbPriorityHigh.isChecked -> TaskPriority.HIGH
            else -> TaskPriority.MEDIUM
        }
        
        // Get tags
        val tags = mutableListOf<TaskTag>()
        if (binding.chipTagWork.isChecked) tags.add(TaskTag.WORK)
        if (binding.chipTagPersonal.isChecked) tags.add(TaskTag.PERSONAL)
        if (binding.chipTagUrgent.isChecked) tags.add(TaskTag.URGENT)
        if (binding.chipTagOther.isChecked) tags.add(TaskTag.OTHER)
        
        lifecycleScope.launch {
            if (taskId != -1L) {
                // Update existing task
                val existingTask = taskViewModel.getTaskById(taskId)
                existingTask?.let {
                    val updatedTask = it.copy(
                        title = title,
                        description = description,
                        dueDate = dueDate,
                        priority = priority,
                        tags = tags
                    )
                    taskViewModel.updateTask(updatedTask)
                }
            } else {
                // Create new task
                val newTask = Task(
                    title = title,
                    description = description,
                    dueDate = dueDate,
                    priority = priority,
                    tags = tags,
                    createdDate = Date()
                )
                taskViewModel.insertTask(newTask)
            }
            
            finish()
        }
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
    }
}

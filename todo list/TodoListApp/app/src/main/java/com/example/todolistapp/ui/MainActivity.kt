package com.example.todolistapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todolistapp.R
import com.example.todolistapp.TodoApplication
import com.example.todolistapp.data.Task
import com.example.todolistapp.data.TaskTag
import com.example.todolistapp.databinding.ActivityMainBinding
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    
    private val taskViewModel: TaskViewModel by viewModels {
        TaskViewModelFactory((application as TodoApplication).repository)
    }
    
    private lateinit var taskAdapter: TaskAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setSupportActionBar(binding.toolbar)
        
        setupRecyclerView()
        setupFilterChips()
        setupFab()
        
        // Observe tasks
        taskViewModel.allTasks.observe(this) { tasks ->
            updateTaskList(tasks)
        }
    }
    
    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            onTaskCheckedChanged = { task, isChecked ->
                taskViewModel.updateTaskCompletionStatus(task.id, isChecked)
                showSnackbar(
                    if (isChecked) "Task marked as completed" else "Task marked as incomplete"
                )
            },
            onTaskClicked = { task ->
                openEditTaskActivity(task)
            },
            onTaskDeleted = { task ->
                taskViewModel.deleteTask(task)
                showSnackbar("Task deleted")
            },
            onPomodoroClicked = { task ->
                openPomodoroActivity(task)
            }
        )
        
        binding.tasksRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = taskAdapter
        }
    }
    
    private fun setupFilterChips() {
        binding.chipWork.setOnClickListener {
            taskViewModel.toggleTagSelection(TaskTag.WORK)
        }
        
        binding.chipPersonal.setOnClickListener {
            taskViewModel.toggleTagSelection(TaskTag.PERSONAL)
        }
        
        binding.chipUrgent.setOnClickListener {
            taskViewModel.toggleTagSelection(TaskTag.URGENT)
        }
        
        // Observe selected tags
        taskViewModel.selectedTags.observe(this) { selectedTags ->
            binding.chipWork.isChecked = selectedTags.contains(TaskTag.WORK)
            binding.chipPersonal.isChecked = selectedTags.contains(TaskTag.PERSONAL)
            binding.chipUrgent.isChecked = selectedTags.contains(TaskTag.URGENT)
        }
        
        // Observe filtered tasks
        taskViewModel.filteredTasks.observe(this) { tasks ->
            if (tasks.isNotEmpty()) {
                updateTaskList(tasks)
            }
        }
    }
    
    private fun setupFab() {
        binding.fabAddTask.setOnClickListener {
            openAddTaskActivity()
        }
    }
    
    private fun updateTaskList(tasks: List<Task>) {
        taskAdapter.submitList(tasks)
        
        // Show empty view if there are no tasks
        if (tasks.isEmpty()) {
            binding.emptyView.visibility = View.VISIBLE
            binding.tasksRecyclerView.visibility = View.GONE
        } else {
            binding.emptyView.visibility = View.GONE
            binding.tasksRecyclerView.visibility = View.VISIBLE
        }
    }
    
    private fun openAddTaskActivity() {
        val intent = Intent(this, AddEditTaskActivity::class.java)
        startActivity(intent)
    }
    
    private fun openEditTaskActivity(task: Task) {
        val intent = Intent(this, AddEditTaskActivity::class.java).apply {
            putExtra(AddEditTaskActivity.EXTRA_TASK_ID, task.id)
        }
        startActivity(intent)
    }
    
    private fun openPomodoroActivity(task: Task) {
        val intent = Intent(this, PomodoroTimerActivity::class.java).apply {
            putExtra(PomodoroTimerActivity.EXTRA_TASK_ID, task.id)
        }
        startActivity(intent)
    }
    
    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_show_completed -> {
                taskViewModel.toggleShowCompleted()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

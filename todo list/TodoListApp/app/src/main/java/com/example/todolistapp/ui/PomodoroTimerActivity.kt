package com.example.todolistapp.ui

import android.os.Bundle
import android.os.CountDownTimer
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.todolistapp.R
import com.example.todolistapp.TodoApplication
import com.example.todolistapp.data.Task
import com.example.todolistapp.databinding.ActivityPomodoroTimerBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.Locale

class PomodoroTimerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPomodoroTimerBinding
    
    private val taskViewModel: TaskViewModel by viewModels {
        TaskViewModelFactory((application as TodoApplication).repository)
    }
    
    private var taskId: Long = -1
    private var task: Task? = null
    private var pomodoroCount: Int = 0
    
    private var timer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 0
    private var isTimerRunning = false
    private var isBreakTime = false
    
    // Pomodoro settings
    private val pomodoroTimeInMillis: Long = 25 * 60 * 1000 // 25 minutes
    private val shortBreakTimeInMillis: Long = 5 * 60 * 1000 // 5 minutes
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPomodoroTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        taskId = intent.getLongExtra(EXTRA_TASK_ID, -1)
        if (taskId == -1L) {
            finish()
            return
        }
        
        loadTask()
        setupTimerControls()
    }
    
    private fun loadTask() {
        lifecycleScope.launch {
            task = taskViewModel.getTaskById(taskId)
            task?.let {
                binding.taskTitle.text = it.title
                pomodoroCount = it.pomodoroCount
                
                // Initialize timer
                timeLeftInMillis = pomodoroTimeInMillis
                updateTimerUI()
            } ?: run {
                finish()
            }
        }
    }
    
    private fun setupTimerControls() {
        binding.btnStartPause.setOnClickListener {
            if (isTimerRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }
        
        binding.btnReset.setOnClickListener {
            resetTimer()
        }
    }
    
    private fun startTimer() {
        timer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerUI()
            }
            
            override fun onFinish() {
                if (!isBreakTime) {
                    // Pomodoro completed
                    pomodoroCount++
                    updatePomodoroCount()
                    
                    // Start break
                    isBreakTime = true
                    timeLeftInMillis = shortBreakTimeInMillis
                    updateTimerStatus("Break Time")
                    binding.timerProgress.progressTintList = ContextCompat.getColorStateList(
                        this@PomodoroTimerActivity, R.color.pomodoro_break
                    )
                    showSnackbar(getString(R.string.pomodoro_complete))
                } else {
                    // Break completed
                    isBreakTime = false
                    timeLeftInMillis = pomodoroTimeInMillis
                    updateTimerStatus("Focus Time")
                    binding.timerProgress.progressTintList = ContextCompat.getColorStateList(
                        this@PomodoroTimerActivity, R.color.pomodoro_active
                    )
                    showSnackbar(getString(R.string.take_a_break))
                }
                
                isTimerRunning = false
                binding.btnStartPause.text = getString(R.string.start_timer)
                updateTimerUI()
            }
        }.start()
        
        isTimerRunning = true
        binding.btnStartPause.text = getString(R.string.pause_timer)
    }
    
    private fun pauseTimer() {
        timer?.cancel()
        isTimerRunning = false
        binding.btnStartPause.text = getString(R.string.start_timer)
        binding.timerProgress.progressTintList = ContextCompat.getColorStateList(
            this, R.color.pomodoro_paused
        )
    }
    
    private fun resetTimer() {
        timer?.cancel()
        timeLeftInMillis = if (isBreakTime) shortBreakTimeInMillis else pomodoroTimeInMillis
        isTimerRunning = false
        binding.btnStartPause.text = getString(R.string.start_timer)
        updateTimerUI()
    }
    
    private fun updateTimerUI() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        
        val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        binding.timerText.text = timeFormatted
        
        // Update progress bar
        val totalTimeInMillis = if (isBreakTime) shortBreakTimeInMillis else pomodoroTimeInMillis
        val progress = (timeLeftInMillis.toFloat() / totalTimeInMillis * 100).toInt()
        binding.timerProgress.progress = progress
    }
    
    private fun updateTimerStatus(status: String) {
        binding.timerStatus.text = status
    }
    
    private fun updatePomodoroCount() {
        lifecycleScope.launch {
            taskViewModel.updatePomodoroCount(taskId, pomodoroCount)
        }
    }
    
    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
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
    
    override fun onPause() {
        super.onPause()
        if (isTimerRunning) {
            pauseTimer()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
    
    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
    }
}

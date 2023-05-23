package com.lionm.taptapwatch.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.lionm.taptapwatch.R
import com.lionm.taptapwatch.data.WatchMode
import com.lionm.taptapwatch.data.WatchState
import com.lionm.taptapwatch.databinding.ActivityMainBinding
import com.lionm.taptapwatch.util.CommonTimerService
import com.lionm.taptapwatch.util.CommonTimerService.Companion.TIMER_ALARM_REPEATABLY
import com.lionm.taptapwatch.util.CommonTimerService.Companion.TIMER_ALARM_TIME
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()

    private val stopWatchDialog: StopWatchSettingDialogFragment = StopWatchSettingDialogFragment()
    private val timerDialog: TimerSettingDialogFragment = TimerSettingDialogFragment()

    private var isCountDown = false
    private var timerStarted = false
    private lateinit var timerServiceIntent: Intent
    private var time = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        prepareObserving()

        timerServiceIntent = Intent(applicationContext, CommonTimerService::class.java)
        registerReceiver(updateTime, IntentFilter(CommonTimerService.TIMER_UPDATED))
    }

    private fun initView() {
        initWatchModeRadioGroup()
        initSetButton()
        initResetButton()
        initStartStopButton()
        initSettingDialog()
    }

    private fun initWatchModeRadioGroup() {
        binding.buttonGroupWatchMode.setOnWatchModeChange { mode: WatchMode? ->
            viewModel.changeWatchMode(mode)
        }
    }

    private fun initSetButton() {
        binding.buttonSet.setOnClickListener {
            if (isCountDown) {
                timerDialog.show(supportFragmentManager, timerDialog::class.java.name)
            } else {
                stopWatchDialog.show(supportFragmentManager, stopWatchDialog::class.java.name)
            }
            viewModel.changeTimerState(WatchState.RESET)
        }
    }

    private fun initResetButton() {
        binding.buttonReset.setOnClickListener {
            viewModel.changeTimerState(WatchState.RESET)
        }
    }

    private fun initStartStopButton() {
        binding.buttonStartStop.setOnClickListener {
            (binding.buttonStartStop.tag as? WatchState)?.let { state ->
                viewModel.changeTimerState(state)
            }
        }
    }

    private fun initSettingDialog() {
        stopWatchDialog.setDialogEventListener(object: StopWatchSettingDialogFragment.DialogEventListener {
            override fun onClickPositiveButton(alarmTime: Long, checkRepetitive: Boolean) {
                timerServiceIntent.putExtra(TIMER_ALARM_TIME, alarmTime)
                timerServiceIntent.putExtra(TIMER_ALARM_REPEATABLY, checkRepetitive)
            }

            override fun onClickNegativeButton() {
                stopWatchDialog.dismiss()
            }
        })

        timerDialog.setDialogEventListener(object: TimerSettingDialogFragment.DialogEventListener {
            override fun onClickPositiveButton(time: Long) {
                this@MainActivity.time = time
                formatTime(time)
            }

            override fun onClickNegativeButton() {
                timerDialog.dismiss()
            }
        })
    }

    private fun prepareObserving() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.watchUiState.collect { uiState ->
                    when (uiState.watchMode) {
                        WatchMode.STOP_WATCH -> {
                            isCountDown = false
                            timerServiceIntent.putExtra(CommonTimerService.TIMER_MODE, true)
                        }
                        WatchMode.TIMER -> {
                            isCountDown = true
                            timerServiceIntent.putExtra(CommonTimerService.TIMER_MODE, false)
                        }
                        WatchMode.CUSTOM -> {
                            // need to check custom mode
                        }
                    }
                    binding.textviewMode.text = uiState.watchMode.value


                    when (uiState.watchState) {
                        WatchState.RESET -> {
                            resetTimer()
                        }
                        WatchState.STARTED -> {
                            startTimer(uiState.watchMode)
                        }
                        WatchState.PAUSE -> {
                            pauseTimer()
                        }
                    }
                }
            }
        }
    }

    private fun resetTimer() {
        pauseTimer()
        time = 0L
        formatTime(time)
    }

    private fun startTimer(watchMode: WatchMode) {
        when (watchMode) {
            WatchMode.STOP_WATCH -> {
                timerServiceIntent.putExtra(CommonTimerService.TIME_EXTRA, time)
                startService(timerServiceIntent)
                binding.buttonStartStop.text = getString(R.string.watch_stop)
                binding.buttonStartStop.tag = WatchState.PAUSE
                timerStarted = true
            }
            WatchMode.TIMER -> {
                if (time == 0L) {
                    Snackbar.make(
                        binding.content,
                        getString(R.string.notice_set_timer),
                        Snackbar.LENGTH_SHORT
                    ).show()
                } else {
                    timerServiceIntent.putExtra(CommonTimerService.TIME_EXTRA, time)
                    startService(timerServiceIntent)
                    binding.buttonStartStop.text = getString(R.string.watch_start)
                    binding.buttonStartStop.tag = WatchState.PAUSE
                    timerStarted = true
                }
            }
            WatchMode.CUSTOM -> {
                // TODO
            }
        }
    }

    private fun pauseTimer() {
        stopService(timerServiceIntent)
        binding.buttonStartStop.text = getString(R.string.watch_start)
        binding.buttonStartStop.tag = WatchState.STARTED
        timerStarted = false
    }

    private val updateTime: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            time = intent.getLongExtra(CommonTimerService.TIME_EXTRA, 0L)

            val isCountUp = intent.getBooleanExtra(CommonTimerService.TIMER_MODE, true)
            if (isCountUp.not() && time <= 0L) {
                resetTimer()
            } else {
                formatTime(time)
            }
        }
    }

    private fun formatTime(time: Long) {
        runOnUiThread {
            val hh = time % 86400 / 3600
            val mm = time % 86400 % 3600 / 60
            val ss = time % 86400 % 3600 % 60

            binding.textviewHour.text = String.format("%02d", hh)
            binding.textviewMinute.text = String.format("%02d", mm)
            binding.textviewSecond.text = String.format("%02d", ss)
        }
    }
}
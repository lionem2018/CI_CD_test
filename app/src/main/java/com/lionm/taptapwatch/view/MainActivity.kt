package com.lionm.taptapwatch.view

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.lionm.taptapwatch.data.WatchMode
import com.lionm.taptapwatch.data.WatchState
import com.lionm.taptapwatch.databinding.ActivityMainBinding
import com.lionm.taptapwatch.util.CustomTimer
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()

    private var isCountDown = false
    private val timer = CustomTimer()

    private var dialog: CommonSettingDialogFragment? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        prepareObserving()
    }

    private fun initView() {
        initWatchModeRadioGroup()
        initSetButton()
        initResetButton()
        initStartStopButton()
        initTimer()
    }

    private fun initWatchModeRadioGroup() {
        binding.buttonGroupWatchMode.setOnWatchModeChange { mode: WatchMode? ->
            viewModel.changeWatchMode(mode)
        }
    }

    private fun initSetButton() {
        binding.buttonSet.setOnClickListener {
            // open setting timer dialog
            dialog?.show(supportFragmentManager, "TimerSettingDialogFragment")

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

    private fun initTimer() {
        timer.setOnTick { currentTime ->
            formatTime(currentTime)
        }

        timer.setOnTimeOver {
            viewModel.changeTimerState(WatchState.RESET)
        }
    }

    private fun formatTime(time: Long) {
        runOnUiThread {
            val h = (time / 3600000).toInt()
            val m = (time - h * 3600000).toInt() / 60000
            val s = (time - h * 3600000 - m * 60000).toInt() / 1000
            val hh = if (h < 10) "0$h" else h.toString() + ""
            val mm = if (m < 10) "0$m" else m.toString() + ""
            val ss = if (s < 10) "0$s" else s.toString() + ""

            binding.textviewHour.text = hh
            binding.textviewMinute.text = mm
            binding.textviewSecond.text = ss
        }
    }

    private fun prepareObserving() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.watchUiState.collect { uiState ->
                    when (uiState.watchMode) {
                        WatchMode.STOP_WATCH -> {
                            dialog = StopWatchSettingDialogFragment()
                            dialog?.setDialogEventListener(object: CommonSettingDialogFragment.DialogEventListener {
                                override fun onClickPositiveButton(time: Long) {
                                }

                                override fun onClickNegativeButton() {
                                    dialog?.dismiss()
                                }

                            })
                            isCountDown = false
                        }
                        WatchMode.TIMER -> {
                            dialog = TimerSettingDialogFragment()
                            dialog?.setDialogEventListener(object: CommonSettingDialogFragment.DialogEventListener {
                                override fun onClickPositiveButton(time: Long) {
                                    timer.currentTime = time
                                    formatTime(time)
                                }

                                override fun onClickNegativeButton() {
                                    dialog?.dismiss()
                                }

                            })
                            isCountDown = true
                        }
                        WatchMode.CUSTOM -> {
                            // need to check custom mode
                        }
                    }
                    binding.textviewMode.text = uiState.watchMode.value


                    when (uiState.watchState) {
                        WatchState.RESET -> {
                            timer.reset()
                            formatTime(0)
                            binding.buttonStartStop.text = "START"
                            binding.buttonStartStop.tag = WatchState.STARTED
                        }
                        WatchState.STARTED -> {
                            when (uiState.watchMode) {
                                WatchMode.STOP_WATCH -> {
                                    timer.startCountUp()
                                    binding.buttonStartStop.text = "STOP"
                                    binding.buttonStartStop.tag = WatchState.PAUSE
                                }
                                WatchMode.TIMER -> {
                                    if (timer.currentTime == 0L) {
                                        Snackbar.make(
                                            binding.content,
                                            "Please set the timer",
                                            Snackbar.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        timer.startCountDown()
                                        binding.buttonStartStop.text = "STOP"
                                        binding.buttonStartStop.tag = WatchState.PAUSE
                                    }
                                }
                                WatchMode.CUSTOM -> {
                                    // TODO
                                }
                            }
                        }
                        WatchState.PAUSE -> {
                            timer.pause()
                            binding.buttonStartStop.text = "START"
                            binding.buttonStartStop.tag = WatchState.STARTED
                        }
                    }

                    // for test
                    Toast.makeText(
                        this@MainActivity,
                        "Timer: " + uiState.watchState.name,
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }
        }
    }
}
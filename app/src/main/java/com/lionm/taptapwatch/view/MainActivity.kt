package com.lionm.taptapwatch.view

import android.media.AudioAttributes
import android.media.SoundPool
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
import com.lionm.taptapwatch.util.CustomTimer
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()

    private val timer = CustomTimer()
    private var soundPool: SoundPool? = null
    private var soundId: Int? = null

    private var dialog: CommonSettingDialogFragment? = null

    private var isCountDown = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        initSoundPool()
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
        timer.setTimerEventListener(object: CustomTimer.TimerEventListener {
            override fun onTick(time: Long) {
                formatTime(time)
            }

            override fun onTimeOver() {
                viewModel.changeTimerState(WatchState.RESET)
                soundId?.let { id ->
                    soundPool?.play(id, 1f, 1f, 0, 0, 1f)
                }
            }

            override fun onAlarm() {
                soundId?.let { id ->
                    soundPool?.play(id, 1f, 1f, 0, 0, 1f)
                }
            }

        })
    }

    private fun initSoundPool() {
        soundPool = SoundPool.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setMaxStreams(1)
            .build()

        soundId = soundPool?.load(applicationContext, R.raw.alert_simple, 1)
    }

    private fun formatTime(time: Long) {
        runOnUiThread {
            val h = (time / 3600000).toInt()
            val m = (time - h * 3600000).toInt() / 60000
            val s = (time - h * 3600000 - m * 60000).toInt() / 1000
            val hh = if (h < 10) "0$h" else h.toString()
            val mm = if (m < 10) "0$m" else m.toString()
            val ss = if (s < 10) "0$s" else s.toString()

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
                                override fun onClickPositiveButton(time: Long, checkRepetitive: Boolean) {
                                    timer.alarmTime = time
                                    timer.isRepetitive = checkRepetitive
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
                                override fun onClickPositiveButton(time: Long, checkRepetitive: Boolean) {
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
                            binding.buttonStartStop.text = getString(R.string.watch_start)
                            binding.buttonStartStop.tag = WatchState.STARTED
                        }
                        WatchState.STARTED -> {
                            when (uiState.watchMode) {
                                WatchMode.STOP_WATCH -> {
                                    timer.startCountUp()
                                    binding.buttonStartStop.text = getString(R.string.watch_stop)
                                    binding.buttonStartStop.tag = WatchState.PAUSE
                                }
                                WatchMode.TIMER -> {
                                    if (timer.currentTime == 0L) {
                                        Snackbar.make(
                                            binding.content,
                                            getString(R.string.notice_set_timer),
                                            Snackbar.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        timer.startCountDown()
                                        binding.buttonStartStop.text = getString(R.string.watch_stop)
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
                            binding.buttonStartStop.text = getString(R.string.watch_start)
                            binding.buttonStartStop.tag = WatchState.STARTED
                        }
                    }
                }
            }
        }
    }
}
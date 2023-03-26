package com.lionm.taptapwatch.util

import java.util.Timer
import java.util.TimerTask

private const val INTERVAL_TIME_UNIT = 1000L

class CustomTimer {
    private var timer: Timer? = null

    private var onTick: ((Long) -> Unit)? = null
    private var onTimeOver: (() -> Unit)? = null
    private var onAlarm: (() -> Unit)? = null

    var currentTime: Long = 0L
    var alarmTime: Long = 0L
    var isRepetitive: Boolean = false

    fun setOnTick(onTick: (Long) -> Unit) {
        this.onTick = onTick
    }

    fun setOnTimeOver(onTimeOver: () -> Unit) {
        this.onTimeOver = onTimeOver
    }

    fun setOnAlarm(onAlarm: () -> Unit) {
        this.onAlarm = onAlarm
    }

    fun startCountUp() {
        // set timer task
        val timerTask = object : TimerTask() {
            override fun run() {
                currentTime += INTERVAL_TIME_UNIT

                onTick?.let { it(currentTime) }

                if (alarmTime > 0L && ((currentTime == alarmTime) || (isRepetitive && currentTime > 0 && currentTime % alarmTime == 0L))) {
                    onAlarm?.let { it() }
                }

                if (currentTime <= 0L) {
                    timer?.cancel()
                    onTimeOver?.let { it() }
                }
            }

        }

        // set timer and start
        runTimerTask(timerTask)
    }

    fun startCountDown() {
        // set timer task
        val timerTask = object : TimerTask() {
            override fun run() {
                currentTime -= INTERVAL_TIME_UNIT

                onTick?.let { it(currentTime) }

                if (currentTime <= 0L) {
                    timer?.cancel()
                    onTimeOver?.let { it() }
                }
            }

        }

        // set timer and start
        runTimerTask(timerTask)
    }

    private fun runTimerTask(timerTask: TimerTask) {
        timer?.cancel()

        // set timer and start
        timer = Timer()
        timer?.schedule(timerTask, 0L, INTERVAL_TIME_UNIT)
    }

    fun pause() {
        // cancel timer
        timer?.cancel()
    }

    fun reset() {
        // remove time info and cancel timer
        currentTime = 0L
        isRepetitive = false
        alarmTime = 0L
        timer?.cancel()
    }

}
package com.lionm.taptapwatch.util

import android.os.SystemClock
import java.util.Timer
import java.util.TimerTask

private const val INTERVAL_TIME_UNIT = 1000L

class CustomTimer {
    interface TimerEventListener {
        fun onTick(time: Long)
        fun onTimeOver()
        fun onAlarm()
    }
    private var timer: Timer? = null
    private var listener: TimerEventListener? = null

    private var baseTime = SystemClock.elapsedRealtime()

    var currentTime: Long = 0L

    // for stopwatch
    var alarmTime: Long = 0L
    var isRepetitive: Boolean = false

    fun setTimerEventListener(listener: TimerEventListener) {
        this.listener = listener
    }

    fun startCountUp() {
        // set timer task
        val timerTask = object : TimerTask() {
            override fun run() {
                currentTime += INTERVAL_TIME_UNIT

                listener?.onTick(currentTime)

                if (alarmTime > 0L && ((currentTime == alarmTime) || (isRepetitive && currentTime > 0 && currentTime % alarmTime == 0L))) {
                    listener?.onAlarm()
                }
            }
        }

        // set timer and start
        runTimerTask(timerTask, INTERVAL_TIME_UNIT)
    }

    fun startCountDown() {
        // set timer task
        val timerTask = object : TimerTask() {
            override fun run() {
                currentTime -= INTERVAL_TIME_UNIT

                listener?.onTick(currentTime)

                if (currentTime <= 0L) {
                    timer?.cancel()
                    listener?.onTimeOver()
                }
            }

        }

        // set timer and start
        runTimerTask(timerTask)
    }

    private fun runTimerTask(timerTask: TimerTask, delay: Long = 0L) {
        timer?.cancel()

        // set timer and start
        timer = Timer()
        timer?.scheduleAtFixedRate(timerTask, delay, INTERVAL_TIME_UNIT)
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
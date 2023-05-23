package com.lionm.taptapwatch.util

import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.IBinder
import com.lionm.taptapwatch.R
import java.util.Timer
import java.util.TimerTask

class CommonTimerService : Service() {
    override fun onBind(p0: Intent?): IBinder? = null

    private val timer = Timer()

    // for alarm
    private var soundPool: SoundPool? = null
    private var soundId: Int? = null

    private var isCountUp: Boolean = true

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        isCountUp = intent.getBooleanExtra(TIMER_MODE, true)

        val time = intent.getLongExtra(TIME_EXTRA, 0L)
        val alarmTime = intent.getLongExtra(TIMER_ALARM_TIME, 0L)
        val alarmRepeatably = intent.getBooleanExtra(TIMER_ALARM_REPEATABLY, false)

        val task: TimerTask = if (isCountUp) {
            CountUpTask(time, alarmTime, alarmRepeatably)
        } else {
            CountDownTask(time)
        }
        
        timer.scheduleAtFixedRate(task, TIMER_INTERVAL, TIMER_INTERVAL)

        initSoundPool()

        return START_NOT_STICKY
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

        soundId = soundPool?.load(this, R.raw.alert_simple, 1)
    }

    override fun onDestroy() {
        timer.cancel()
        super.onDestroy()
    }

    private inner class CountUpTask(
        private var time: Long,
        private val alarmTime: Long,
        private val alarmRepeatably: Boolean
    ) : TimerTask() {
        override fun run() {
            time ++

            alarmTime.takeIf { it > 0L }?.let { alarmTime ->
                if ((time == alarmTime)
                    || (alarmRepeatably && time % alarmTime == 0L)) {
                    playAlarm(soundId, soundPool)
                }
            }

            val intent = Intent(TIMER_UPDATED)
            intent.putExtra(TIME_EXTRA, time)
            intent.putExtra(TIMER_MODE, isCountUp)
            sendBroadcast(intent)
        }
    }

    private inner class CountDownTask(
        private var time: Long
    ) : TimerTask() {
        override fun run() {
            time--

            if (time <= 0L) {
                playAlarm(soundId, soundPool)
            }

            val intent = Intent(TIMER_UPDATED)
            intent.putExtra(TIME_EXTRA, time)
            intent.putExtra(TIMER_MODE, isCountUp)
            sendBroadcast(intent)
        }
    }

    private fun playAlarm(soundId: Int?, soundPool: SoundPool?) {
        soundId?.let { id ->
            soundPool?.play(id, 1f, 1f, 0, 0, 1f)
        }
    }

    companion object {
        private const val TIMER_INTERVAL = 1000L

        const val TIMER_UPDATED = "timerUpdated"
        const val TIME_EXTRA = "timeExtra"
        const val TIMER_MODE = "timerMode"
        const val TIMER_ALARM_TIME = "timerAlarmTime"
        const val TIMER_ALARM_REPEATABLY = "timerAlarmRepeatably"
    }
}
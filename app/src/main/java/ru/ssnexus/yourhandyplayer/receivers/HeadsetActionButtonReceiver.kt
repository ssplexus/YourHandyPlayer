package ru.ssnexus.yourhandyplayer.receivers

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.view.KeyEvent
import timber.log.Timber
import java.util.*

class HeadsetActionButtonReceiver : BroadcastReceiver() {
    interface Delegate {
        fun onMediaButtonSingleClick()
        fun onMediaButtonDoubleClick()
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent == null || delegate == null || Intent.ACTION_MEDIA_BUTTON != intent.action) return
        val keyEvent = intent.extras!![Intent.EXTRA_KEY_EVENT] as KeyEvent?
        if (keyEvent == null || keyEvent.action != KeyEvent.ACTION_DOWN) return

        counter++
        if (doublePressTimer != null) {
            doublePressTimer?.cancel()
        }
        doublePressTimer = Timer()
        doublePressTimer?.schedule(object : TimerTask() {
            override fun run() {
                if (counter == 1) {
                    delegate?.onMediaButtonSingleClick()
                } else {
                    delegate?.onMediaButtonDoubleClick()
                }
                counter = 0
            }
        }, doublePressSpeed.toLong())
    }

    companion object {
        var delegate: Delegate? = null
        private var mAudioManager: AudioManager? = null
        private var mRemoteControlResponder: ComponentName? = null
        private const val doublePressSpeed = 300 // double keypressed in ms
        private var doublePressTimer: Timer? = null
        private var counter = 0
        fun register(context: Context) {
            mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            mRemoteControlResponder = ComponentName(
                context,
                HeadsetActionButtonReceiver::class.java
            )
            mAudioManager?.registerMediaButtonEventReceiver(mRemoteControlResponder)
        }

        fun unregister(context: Context?) {
            mAudioManager?.unregisterMediaButtonEventReceiver(mRemoteControlResponder)
            if (doublePressTimer != null) {
                doublePressTimer?.cancel()
                doublePressTimer = null
            }
        }
    }
}
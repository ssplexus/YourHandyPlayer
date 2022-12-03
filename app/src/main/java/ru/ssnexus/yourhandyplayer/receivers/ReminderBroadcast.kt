package ru.ssnexus.yourhandyplayer.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.*
import ru.ssnexus.database_module.data.entity.JamendoTrackData
import ru.ssnexus.yourhandyplayer.App
import ru.ssnexus.yourhandyplayer.domain.Interactor
import ru.ssnexus.yourhandyplayer.notifications.NotificationConstants
import javax.inject.Inject

class ReminderBroadcast : BroadcastReceiver() {

    //Инициализируем интерактор
    @Inject
    lateinit var interactor: Interactor

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        App.instance.dagger.inject(this)
    }

    override fun onReceive(context: Context?, intent: Intent?) {

        val bundle = intent?.getBundleExtra(NotificationConstants.TRACK_BUNDLE_KEY)
        val track: JamendoTrackData = bundle?.get(NotificationConstants.TRACK_KEY) as JamendoTrackData

//        NotificationHelper.createNotification(context!!, track)
//        MainScope().launch {
//            scope.async {
//                interactor.updateFilmWatchLaterState(film)
//            }
//        }

    }
}
package ru.ssnexus.yourhandyplayer.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import ru.ssnexus.database_module.data.entity.JamendoTrackData
import ru.ssnexus.yourhandyplayer.App
import ru.ssnexus.yourhandyplayer.domain.Interactor
import java.net.URL
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class DetailsViewModel : ViewModel() {
    //Инициализируем интерактор
    @Inject
    lateinit var interactor: Interactor

    init {
        App.instance.dagger.inject(this)
    }

    fun getTrackFavSate(trackData: JamendoTrackData) = interactor.getTrackFavState(trackData)

    fun getTrackLaterSate(trackData: JamendoTrackData) = interactor.getTrackLaterState(trackData)

    fun updateTrackFavState(trackData: JamendoTrackData){
        interactor.updateTrackFavState(trackData)
    }

    fun updateTrackListenLaterState(trackData: JamendoTrackData){
        interactor.updateTrackListenLaterState(trackData)
    }

    suspend fun loadWallpaper(url: String): Bitmap {
        return suspendCoroutine {
            val url = URL(url)
            val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
            it.resume(bitmap)
        }
    }
}
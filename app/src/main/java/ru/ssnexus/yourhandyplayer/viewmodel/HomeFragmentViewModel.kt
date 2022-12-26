package ru.ssnexus.yourhandyplayer.viewmodel

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.ssnexus.database_module.data.entity.JamendoTrackData
import ru.ssnexus.yourhandyplayer.App
import ru.ssnexus.yourhandyplayer.domain.Interactor
import ru.ssnexus.yourhandyplayer.utils.SingleLiveEvent
import timber.log.Timber
import javax.inject.Inject


class HomeFragmentViewModel : ViewModel(){

    //Отслеживание базы данных
    var tracksLiveData: MutableLiveData<List<JamendoTrackData>>

    val tagsPropertyLiveData = SingleLiveEvent<String>()
    val modePropertyLiveData: MutableLiveData<String>
    val connectedDeviceTypeLiveData: MutableLiveData<Int>
    //Отслеживание данных состояния прогрессбара
    val showProgressBar: BehaviorSubject<Boolean>

    var tags = ""

    //Инициализируем интерактор
    @Inject
    lateinit var interactor: Interactor

    init {
        App.instance.dagger.inject(this)

        tracksLiveData = interactor.getTracksLiveData()

        showProgressBar = interactor.progressBarState

        tagsPropertyLiveData.value = interactor.getDefaultTagsFromPreferences()
        modePropertyLiveData = interactor.getMusicModeLiveDataFromPreferences()
        connectedDeviceTypeLiveData = interactor.connectedDeviceTypeLiveData
    }

    fun getMusicMode() = interactor.getMusicModeFromPreferences()

    fun getTagsPreferences(): String{
        return interactor.getDefaultTagsFromPreferences()
    }
    fun changeMusicMode(){
        interactor.changeMusicMode()
    }

    //Получить трэки
    fun getNextTracks() {
        interactor.getTracksByTagsFromApi()
    }

    fun updateTracks(tags: String) {
        CoroutineScope(Dispatchers.IO).launch {
            interactor.clearTrackDataCache()
        }
        interactor.getTracksByTagsFromApi(tags = tags)
    }
}
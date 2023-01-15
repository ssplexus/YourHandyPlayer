package ru.ssnexus.yourhandyplayer.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import ru.ssnexus.database_module.data.entity.JamendoTrackData
import ru.ssnexus.yourhandyplayer.App
import ru.ssnexus.yourhandyplayer.domain.Interactor
import ru.ssnexus.yourhandyplayer.mediaplayer.HandyMediaPlayerSingle
import ru.ssnexus.yourhandyplayer.utils.SingleLiveEvent
import javax.inject.Inject


class HomeFragmentViewModel : ViewModel(){

    private val scope = CoroutineScope(Dispatchers.IO + Job())
//    private val waveDataFlowCollect = HandyMediaPlayerSingle.instance.waveDataFlow.asSharedFlow()

//    val savedWaveDataArray = arrayOfNulls<Int>(WAVE_STRIPS_CNT)
//    private var waveNum = 0

    //Отслеживание базы данных
    var tracksLiveData: MutableLiveData<List<JamendoTrackData>>

    val tagsPropertyLiveData = SingleLiveEvent<String>()
    val modePropertyLiveData: MutableLiveData<String>
    val connectedDeviceTypeLiveData: MutableLiveData<Int>
    //Отслеживание данных состояния прогрессбара
    val showProgressBar: BehaviorSubject<Boolean>

    var tags: String = ""

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

//        scope.launch {
//            waveDataFlowCollect.collect {
//                savedWaveDataArray[waveNum++] = it
//                if (waveNum >= WAVE_STRIPS_CNT) waveNum = 0
//            }
//        }

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
            interactor.getTracksByTagsFromApi(tags = tags)
        }
    }


    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }

    companion object{
        const val WAVE_STRIPS_CNT = 10
    }
}
package ru.ssnexus.yourhandyplayer.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.ssnexus.database_module.data.entity.JamendoTrackData
import ru.ssnexus.yourhandyplayer.App
import ru.ssnexus.yourhandyplayer.domain.Interactor
import timber.log.Timber
import javax.inject.Inject

class HomeFragmentViewModel : ViewModel(){

    //Отслеживание базы данных
    var tracksData: Observable<List<JamendoTrackData>>
    var favoritesTracksData: Observable<List<JamendoTrackData>>


    val tagsPropertyLiveData: MutableLiveData<String> = MutableLiveData()
    val modePropertyLiveData: MutableLiveData<String>
    //Отслеживание данных состояния прогрессбара
    val showProgressBar: BehaviorSubject<Boolean>

    //Инициализируем интерактор
    @Inject
    lateinit var interactor: Interactor

    init {
        App.instance.dagger.inject(this)

        tracksData = interactor.getTracksDataObservable()
        favoritesTracksData = interactor.getFavoritesTracksDataObservable()

        showProgressBar = interactor.progressBarState

        tagsPropertyLiveData.value = interactor.getDefaultTagsFromPreferences()
        modePropertyLiveData = interactor.getMusicModeLiveDataFromPreferences()
    }

    fun refreshData(){
        tracksData.repeat()
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
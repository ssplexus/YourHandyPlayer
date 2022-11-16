package ru.ssnexus.yourhandyplayer.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import ru.ssnexus.database_module.data.entity.JamendoTrackData
import ru.ssnexus.yourhandyplayer.App
import ru.ssnexus.yourhandyplayer.domain.Interactor
import timber.log.Timber
import javax.inject.Inject

class HomeFragmentViewModel : ViewModel(){

    //Отслеживание базы данных
    var tracksData: Observable<List<JamendoTrackData>>
    val tagsPropertyLifeData: MutableLiveData<String> = MutableLiveData()
    //Отслеживание данных состояния прогрессбара
    val showProgressBar: BehaviorSubject<Boolean>

    //Инициализируем интерактор
    @Inject
    lateinit var interactor: Interactor

    init {
        App.instance.dagger.inject(this)
        tracksData = interactor.getTracksDataObservable()
        showProgressBar = interactor.progressBarState
        getTagsProperty()
    }

    private fun getTagsProperty() {
        //Кладем категорию в LiveData
        tagsPropertyLifeData.value = interactor.getDefaultTagsFromPreferences()
    }

    //Получить данные 1 стрницы
    fun updateTracks() {
        interactor.getTracksByTagsFromApi(offset = 0)
    }

    //Получить трэки
    fun getNextTracks() {
        interactor.getTracksByTagsFromApi()
    }
}
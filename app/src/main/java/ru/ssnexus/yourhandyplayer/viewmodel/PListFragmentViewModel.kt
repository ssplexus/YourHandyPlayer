package ru.ssnexus.yourhandyplayer.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import ru.ssnexus.database_module.data.entity.JamendoTrackData
import ru.ssnexus.yourhandyplayer.App
import ru.ssnexus.yourhandyplayer.di.modules.remote_module.entity.jamendo.JamendoTrack
import ru.ssnexus.yourhandyplayer.domain.Interactor
import timber.log.Timber
import javax.inject.Inject

class PListFragmentViewModel : ViewModel() {
    //Отслеживание базы данных
    var tracksLiveData: MutableLiveData<List<JamendoTrackData>>
    //Отслеживание данных состояния прогрессбара
    val showProgressBar: BehaviorSubject<Boolean>
    //Инициализируем интерактор
    @Inject
    lateinit var interactor: Interactor

    init {
        App.instance.dagger.inject(this)

        tracksLiveData = interactor.getTracksLiveData()

        showProgressBar = interactor.progressBarState
    }

    //Получить данные 1 стрницы
    fun updateTracks() {
        interactor.getTracksByTagsFromApi(offset = 0)
    }

    //Получить трэки
    fun getNextTracks() {
        interactor.getTracksByTagsFromApi()
    }

    fun getMusicMode() = interactor.getMusicModeFromPreferences()
}
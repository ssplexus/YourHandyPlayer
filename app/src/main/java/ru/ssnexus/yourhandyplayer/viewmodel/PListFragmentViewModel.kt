package ru.ssnexus.yourhandyplayer.viewmodel

import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.core.Observable
import ru.ssnexus.database_module.data.entity.Track
import ru.ssnexus.yourhandyplayer.App
import ru.ssnexus.yourhandyplayer.di.modules.remote_module.entity.jamendo.JamendoResult
import ru.ssnexus.yourhandyplayer.domain.Interactor
import javax.inject.Inject

class PListFragmentViewModel : ViewModel() {
    //Отслеживание базы данных
    var trackListData: Observable<JamendoResult>? = null
    //Инициализируем интерактор
    @Inject
    lateinit var interactor: Interactor

    init {
        App.instance.dagger.inject(this)
         interactor.getCurrentTracksData().let {
             if (it != null) {
                 trackListData = it
             }
        }
    }

    fun getTracksFromApi(){
        interactor.getTracksByTagsFromApi()
    }
}
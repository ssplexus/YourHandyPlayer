package ru.ssnexus.yourhandyplayer.viewmodel

//import ru.ssnexus.database_module.data.entity.Film
//import ru.ssnexus.mymoviesearcher.domain.Interactor
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.core.Observable
import ru.ssnexus.database_module.data.entity.JamendoTrackData
import ru.ssnexus.yourhandyplayer.App
import ru.ssnexus.yourhandyplayer.di.modules.remote_module.entity.jamendo.JamendoTrack
import ru.ssnexus.yourhandyplayer.domain.Interactor
import javax.inject.Inject

class HomeFragmentViewModel : ViewModel(){

    //Отслеживание базы данных
    var tracksData: Observable<List<JamendoTrackData>>
    //Инициализируем интерактор
    @Inject
    lateinit var interactor: Interactor

    init {
        App.instance.dagger.inject(this)
        tracksData = interactor.getTracksDataObservable()
    }
}
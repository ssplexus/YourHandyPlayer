package ru.ssnexus.yourhandyplayer.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.core.Observable
import ru.ssnexus.database_module.data.entity.JamendoTrackData
import ru.ssnexus.yourhandyplayer.App
import ru.ssnexus.yourhandyplayer.domain.Interactor
import javax.inject.Inject

class HomeFragmentViewModel : ViewModel(){

    //Отслеживание базы данных
    var tracksData: Observable<List<JamendoTrackData>>

    val tagsPropertyLifeData: MutableLiveData<String> = MutableLiveData()

    //Инициализируем интерактор
    @Inject
    lateinit var interactor: Interactor

    init {
        App.instance.dagger.inject(this)
        tracksData = interactor.getTracksDataObservable()
        getTagsProperty()
    }

    private fun getTagsProperty() {
        //Кладем категорию в LiveData
        tagsPropertyLifeData.value = interactor.getDefaultTagsFromPreferences()
    }
}
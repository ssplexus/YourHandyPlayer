package ru.ssnexus.yourhandyplayer.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.ssnexus.yourhandyplayer.App
import ru.ssnexus.yourhandyplayer.domain.Interactor
import javax.inject.Inject

class TagsSetViewModel : ViewModel() {
    //Инициализируем интерактор
    @Inject
    lateinit var interactor: Interactor

    val tagsPropertyLifeData: MutableLiveData<String> = MutableLiveData()

    init {
        App.instance.dagger.inject(this)
        getTagsProperty()
    }

    fun saveTagsProperty(tags : String) {
        interactor.saveTagsToPreferences(tags)
        getTagsProperty()
    }

    private fun getTagsProperty() {
        //Кладем категорию в LiveData
        tagsPropertyLifeData.value = interactor.getDefaultTagsFromPreferences()
    }
}
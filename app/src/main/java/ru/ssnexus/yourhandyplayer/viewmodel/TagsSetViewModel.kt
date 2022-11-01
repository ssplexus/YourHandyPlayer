package ru.ssnexus.yourhandyplayer.viewmodel

import androidx.lifecycle.ViewModel
import ru.ssnexus.yourhandyplayer.App
import ru.ssnexus.yourhandyplayer.domain.Interactor
import javax.inject.Inject

class TagsSetViewModel : ViewModel() {
    //Инициализируем интерактор
    @Inject
    lateinit var interactor: Interactor

    init {
        App.instance.dagger.inject(this)

    }
}
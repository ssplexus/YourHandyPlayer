package ru.ssnexus.yourhandyplayer.viewmodel

import androidx.lifecycle.ViewModel
import ru.ssnexus.yourhandyplayer.domain.Interactor
import javax.inject.Inject

class DetailsViewModel : ViewModel() {
    //Инициализируем интерактор
    @Inject
    lateinit var interactor: Interactor
}
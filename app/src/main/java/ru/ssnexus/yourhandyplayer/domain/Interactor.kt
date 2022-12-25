package ru.ssnexus.yourhandyplayer.domain

import androidx.lifecycle.MutableLiveData
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.ssnexus.database_module.data.MainRepository
import ru.ssnexus.database_module.data.entity.JamendoTrackData
import ru.ssnexus.yourhandyplayer.data.API
import ru.ssnexus.yourhandyplayer.data.preferences.PreferenceProvider
import ru.ssnexus.yourhandyplayer.di.modules.remote_module.JamendoApi
import ru.ssnexus.yourhandyplayer.utils.AutoDisposable
import ru.ssnexus.yourhandyplayer.utils.Converter
import ru.ssnexus.yourhandyplayer.utils.addTo
import ru.ssnexus.yourhandyplayer.view.MainActivity
import timber.log.Timber

class Interactor(val repo: MainRepository, val retrofitService: JamendoApi, private val preferences: PreferenceProvider) {

    var progressBarState: BehaviorSubject<Boolean> = BehaviorSubject.create()
    var connectedDeviceTypeLiveData: MutableLiveData<Int> = MutableLiveData()

    private var tracksLiveData =  MutableLiveData<List<JamendoTrackData>>()
    private val modePropertyLiveData = preferences.modePropertyLiveData

    fun initDataObservers(main: MainActivity) {

        repo.getTracksDataObservable().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{
                    tracksLiveData.postValue(it)
            }.addTo(main.autoDisposable)

        modePropertyLiveData.observe(main){
            CoroutineScope(Dispatchers.IO).launch {
                clearTrackDataCache()
                when (it){
                    PreferenceProvider.TAGS_MODE -> getTracksByTagsFromApi()
                    PreferenceProvider.FAVORITES_MODE -> repo.putToDb(repo.getCachedFavoriteTracks())
                    PreferenceProvider.LISTEN_LATER_MODE -> repo.putToDb(repo.getCachedListenLaterTracks())
                }
            }
        }
    }

    fun getTracksByTagsFromApi(tags: String = "", offset: Int = -1) {
        var curTags = tags
        if (curTags.isBlank()) {
            if (getDefaultTagsFromPreferences() == PreferenceProvider.DEFAULT_TAGS) curTags = ""
            else curTags = getDefaultTagsFromPreferences()
        }
        curTags = curTags.filter {!it.isWhitespace() }

        //Показываем ProgressBar
        progressBarState.onNext(true)

        //Метод getDefaultCategoryFromPreferences() будет получать при каждом запросе нужный нам список фильмов
        retrofitService.getTracksByTags(API.CLIENT_ID,
                                        API.PAGE_LIMIT,
                                        if (offset < 0) getDBSize() else offset,
                                        curTags,
                                        API.MUSIC_INFO)
            .subscribeOn(Schedulers.io())
            .map {
                Converter.convertApiListToDtoList(it)
            }
            .subscribeBy(
                onError = {
                    progressBarState.onNext(false)
                },
                onNext = {
                    progressBarState.onNext(false)
                    repo.putToDb(it)

                }
            )
    }

    fun getTracksLiveData() = tracksLiveData

    fun updateCurrentTracksData() {
        tracksLiveData.postValue(repo.getTracksDataObservable().blockingFirst())
    }

    fun getDBSize(): Int {
        var result = 0
        CoroutineScope(Dispatchers.IO).launch {
            result = repo.getSize()
        }
        return result
    }

    fun isInFavorites(trackData: JamendoTrackData) = repo.isInFavorites(trackData.id)
    fun isInListenLater(trackData: JamendoTrackData) = repo.isInListenLater(trackData.id)

    // Получить состояние карточки трека (в избранном или нет)
    fun getTrackFavState(trackData: JamendoTrackData) : Int = repo.getTrackFavStateById(trackData.id)

    // Получить состояние карточки трека (в списке посмотрть позже или нет)
    fun getTrackLaterState(trackData: JamendoTrackData) : Int = repo.getTrackListenLaterStateById(trackData.id)

    // Обновить состояние "в избранном" карточки фильма
    fun updateTrackFavState(trackData: JamendoTrackData){
        repo.updateTrackFavStateById(trackData.id)
    }

    // Обновить состояние "послушать позже" карточки фильма
    fun updateTrackListenLaterState(trackData: JamendoTrackData){
        repo.updateTrackListenLaterStateById(trackData.id)
    }

    fun clearTrackDataCache(){
        repo.clearTrackDataCache()
    }

    //Метод для получения настроек
    fun getDefaultTagsFromPreferences() = preferences.getDefaultTags()

    fun saveTagsToPreferences(tags : String) {
        preferences.saveTags(tags)
    }

    fun getMusicModeFromPreferences() = preferences.getMode()

    fun getMusicModeLiveDataFromPreferences() = preferences.modePropertyLiveData

    fun setListenLaterPref(){
        preferences.setListenLaterMode()
    }

    fun setFavoritesPref(){
        preferences.setFavoritesMode()
    }

    fun setTagsPref(){
        preferences.setTagsMode()
    }

    fun changeMusicMode() = preferences.changeMode()

    //Получить время первого запуска
    fun getFirstLaunchTime() = preferences.getFirstLaunchTime()

}



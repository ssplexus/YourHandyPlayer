package ru.ssnexus.yourhandyplayer.domain

import io.reactivex.rxjava3.core.Observable
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
import ru.ssnexus.yourhandyplayer.di.modules.remote_module.entity.jamendo.JamendoResult
import ru.ssnexus.yourhandyplayer.di.modules.remote_module.entity.jamendo.JamendoTrack
import ru.ssnexus.yourhandyplayer.utils.Converter
import timber.log.Timber

class Interactor(val repo: MainRepository, val retrofitService: JamendoApi, private val preferences: PreferenceProvider) {

    var progressBarState: BehaviorSubject<Boolean> = BehaviorSubject.create()

    fun getTracksByTagsFromApi(tags: String = "", offset: Int = -1) {

        Timber.d("getTracksByTagsFromApi")
        var curTags = tags
        if (curTags.isBlank()) {
            if (getDefaultTagsFromPreferences() == PreferenceProvider.DEFAULT_TAGS) curTags = ""
            else curTags = getDefaultTagsFromPreferences()
        }
        curTags = curTags.filter {!it.isWhitespace() }

        //Показываем ProgressBar
        progressBarState.onNext(true)
        Timber.e("Info!-0 " + getDBSize())
        Timber.e("Info!-offset " + offset)
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
                          Timber.e("Error: Can't get tracks data")
                    progressBarState.onNext(false)
                },
                onNext = {
                    Timber.e("Success: Get SCloud data" + it.get(1).name)
                    progressBarState.onNext(false)
                    repo.putToDb(it)
                    Timber.e("Info!-1 " + repo.getTracksData().size)
                    Timber.e("Info!-2 " + getDBSize())
                }
            )
    }

    fun getTracksDataObservable(): Observable<List<JamendoTrackData>> = repo.getTracksDataObservable()
    fun getFavoritesTracksDataObservable(): Observable<List<JamendoTrackData>> = repo.getFavoritesTracksDataObservable()

    fun getTracksData() = repo.getTracksData()

    fun getFavoritesTracksData() = repo.getFavoritesTracksData()

    fun getDBSize(): Int {
        var result = 0
        CoroutineScope(Dispatchers.IO).launch {
            result = repo.getSize()
        }
        return result
    }

    fun isInFavorites(trackData: JamendoTrackData) = repo.isInFavorites(trackData.id)

//    // Обновление rview исходными значениями при очистки поля поиска фильмов
//    fun recallData(){
//        Completable.fromSingle<List<Film>> {
//            repo.putToDb(repo.getAllFromDBAsList())
//        }
//            .subscribeOn(Schedulers.io())
//            .subscribe()
//    }

    // Получить состояние карточки фильма (в избранном или нет)
    fun getTrackFavState(trackData: JamendoTrackData) : Int = repo.getTrackFavStateById(trackData.id)

    // Обновить состояние "в избранном" карточки фильма
    fun updateTrackFavState(trackData: JamendoTrackData){
        repo.updateTrackFavStateById(trackData.id)
    }

    fun addToFavorites (trackData: JamendoTrackData) {
        repo.addToFavorites(trackData)
    }

    fun removeFromFavorites(id: Int) {
        repo.removeFromFavorites(id)
    }

    fun clearTrackDataCache()    {
        repo.clearTrackDataCache()
    }

    fun clearFavoritesTrackDataCache()    {
        repo.clearFavoritesTrackDataCache()
    }

    //Метод для получения настроек
    fun getDefaultTagsFromPreferences() = preferences.getDefaultTags()

    fun saveTagsToPreferences(tags : String) {
        preferences.saveTags(tags)
    }

    fun getMusicModeFromPreferences() = preferences.getMode()

    fun getMusicModeLiveDataFromPreferences() = preferences.modePropertyLiveData

    fun changeMusicMode() = preferences.changeMode()

    //Получить время первого запуска
    fun getFirstLaunchTime() = preferences.getFirstLaunchTime()

}



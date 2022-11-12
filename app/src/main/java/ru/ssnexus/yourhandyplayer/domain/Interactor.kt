package ru.ssnexus.yourhandyplayer.domain

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
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

//    var progressBarState: BehaviorSubject<Boolean> = BehaviorSubject.create()

    fun getTracksByTagsFromApi() {

        Timber.d("getTracksByTagsFromApi")
//        //Показываем ProgressBar
//        progressBarState.onNext(true)
        //Метод getDefaultCategoryFromPreferences() будет получать при каждом запросе нужный нам список фильмов
        retrofitService.getTracksByTags(API.CLIENT_ID,10, "rock", "musicinfo")
            .subscribeOn(Schedulers.io())
            .map {
                Converter.convertApiListToDtoList(it)
            }
            .subscribeBy(
                onError = {
                          Timber.e("Error: Can't get tracks data")
//                    repo.u
                    //progressBarState.onNext(false)
                },
                onNext = {
                    Timber.e("Success: Get SCloud data" + it.get(1).name)
                    //repo.trackListData. = it
//                    repo.updateList(it.results)
                    //progressBarState.onNext(false)
                    repo.putToDb(it)
                    Timber.e("Info!" + repo.getTracksData().size)
                }
            )
    }

    fun getTracksDataObservable(): Observable<List<JamendoTrackData>> = repo.getTracksDataObservable()

    fun getTracksData(): List<JamendoTrackData> = repo.getTracksData()

    //fun getTrackListData(): Observable<List<Track>>? = repo.getTrackList()

//    // Получаем результат запроса поиска
//    fun getSearchResultFromApi(search: String, page: Int = 1): Observable<List<Film>> = retrofitService.getFilmFromSearch(API.KEY, "ru-RU", search, page)
//        .map {
//            Converter.convertApiListToDtoList(it.tmdbFilms)
//        }
//
//    fun getFilmFromApi(id: Int): Observable<Film> = retrofitService.getFilm(id, API.KEY, "ru-RU")
//        .map {
//            Converter.convertApiToDto(it)
//        }

//    // Обновление rview исходными значениями при очистки поля поиска фильмов
//    fun recallData(){
//        Completable.fromSingle<List<Film>> {
//            repo.putToDb(repo.getAllFromDBAsList())
//        }
//            .subscribeOn(Schedulers.io())
//            .subscribe()
//    }

    //fun getFilmsFromDB(): Observable<List<Film>> = repo.getAllFromDB()

//    fun getDBSize(): Int = repo.getSize()
//
    fun clearCache()
    {
        repo.clearCache()
    }

//    //Метод для сохранения настроек
//    fun saveDefaultCategoryToPreferences(category: String) {
//        preferences.saveDefaultCategory(category)
//    }
    //Метод для получения настроек
    fun getDefaultTagsFromPreferences() = preferences.getDefaultTags()

    fun saveTagsToPreferences(tags : String) {
        preferences.saveTags(tags)
    }

    //Получить время первого запуска
    fun getFirstLaunchTime() = preferences.getFirstLaunchTime()

}



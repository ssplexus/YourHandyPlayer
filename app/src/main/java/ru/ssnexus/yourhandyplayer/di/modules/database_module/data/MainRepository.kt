package ru.ssnexus.database_module.data

import io.reactivex.rxjava3.core.Observable
import ru.ssnexus.database_module.data.DAO.TrackDao
import ru.ssnexus.database_module.data.entity.Track
import ru.ssnexus.yourhandyplayer.di.modules.remote_module.entity.jamendo.JamendoResult

class MainRepository(private val trackDao: TrackDao) {

    var trackListData : Observable<JamendoResult>? = null

    fun getCurrentTracks(): Observable<JamendoResult>? = trackListData

//    fun updateList (newList: List<Track>){
//        newList.also { trackListData = Observable.fromArray(newList) }
//    }
//
//    fun getTrackList() : Observable<List<Track>>? = trackListData
//
//    fun putToDb(films: List<Track>) {
//        //Запросы в БД должны быть в отдельном потоке
//        trackDao.insertAll(films)
//
//    }
//
//    fun getAllFromDB(): Observable<List<Track>> = trackDao.getTracks()
//
//    fun getSize() : Int = trackDao.getData().size
//
//    fun clearCache()
//    {
//        //Timber.d("ClearCache")
//        trackDao.nukeTable()
//    }
}

package ru.ssnexus.database_module.data

import io.reactivex.rxjava3.core.Observable
import ru.ssnexus.database_module.data.DAO.TrackDao
import ru.ssnexus.database_module.data.entity.JamendoTrackData
import ru.ssnexus.yourhandyplayer.di.modules.remote_module.entity.jamendo.JamendoTrack

class MainRepository(private val trackDao: TrackDao) {

//    var trackListData : Observable<List<JamendoTrack>>

//    fun getCurrentTracks(): Observable<List<JamendoTrack>> = trackListData

//    fun updateList (newData: List<JamendoTrack>){
//        trackListData = Observable.fromArray(newData)
//    }

    fun getTracksDataObservable() : Observable<List<JamendoTrackData>> = trackDao.getCachedTracksObservable()

    fun getTracksData() : List<JamendoTrackData> = trackDao.getCachedTracks()

    fun putToDb(list: List<JamendoTrackData>) {
        //Запросы в БД должны быть в отдельном потоке
        trackDao.insertAll(list)

    }
//
//    fun getAllFromDB(): Observable<List<Track>> = trackDao.getTracks()
//
//    fun getSize() : Int = trackDao.getData().size
//
    fun clearCache()
    {
        //Timber.d("ClearCache")
        trackDao.nukeTable()
    }
}

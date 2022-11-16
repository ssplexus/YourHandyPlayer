package ru.ssnexus.database_module.data

import io.reactivex.rxjava3.core.Observable
import ru.ssnexus.database_module.data.DAO.TrackDao
import ru.ssnexus.database_module.data.entity.JamendoTrackData

class MainRepository(private val trackDao: TrackDao) {

    fun getTracksDataObservable() : Observable<List<JamendoTrackData>> = trackDao.getCachedTracksObservable()

    fun getTracksData() : List<JamendoTrackData> = trackDao.getCachedTracks()

    fun putToDb(list: List<JamendoTrackData>) {
        //Запросы в БД должны быть в отдельном потоке
        trackDao.insertAll(list)

    }

//    fun getAllFromDB(): Observable<List<Track>> = trackDao.getTracks()

    fun getSize() : Int = trackDao.getSize()

    fun clearCache()
    {
        //Timber.d("ClearCache")
        trackDao.nukeTable()
    }
}

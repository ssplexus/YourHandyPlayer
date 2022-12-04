package ru.ssnexus.database_module.data

import io.reactivex.rxjava3.core.Observable
import ru.ssnexus.database_module.data.DAO.TrackDao
import ru.ssnexus.database_module.data.entity.JamendoTrackData
import timber.log.Timber

class MainRepository(private val trackDao: TrackDao) {

    fun getTracksDataObservable() : Observable<List<JamendoTrackData>> = trackDao.getCachedTracksObservable()
    fun getFavoritesTracksDataObservable() : Observable<List<JamendoTrackData>> = trackDao.getCachedFavoritesTracksObservable()
    fun getListenLaterTracksDataObservable() : Observable<List<JamendoTrackData>> = trackDao.getCachedListenLaterTracksObservable()

    fun getTracksData() : List<JamendoTrackData> = trackDao.getCachedTracks()
    fun getFavoritesTracksData() : List<JamendoTrackData> = trackDao.getCachedFavoritesTracks()
    fun getListenLaterTracksData() : List<JamendoTrackData> = trackDao.getCachedListenLaterTracks()

    fun putToDb(list: List<JamendoTrackData>) {
        //Запросы в БД должны быть в отдельном потоке
        trackDao.insertAll(list)
    }

    fun updateTrackFavStateById(id : Int){
        trackDao.updateFavoriteById(id)
    }

    fun isInFavorites(id: Int) = trackDao.isInFavorites(id) > 0

    fun isInListenLater(id: Int) = trackDao.isInListenLater(id) > 0

    fun getTrackFavStateById(id : Int) : Int = trackDao.getFavStateById(id)

    fun getTrackListenLaterStateById(id : Int) : Int = trackDao.getTrackListenLaterStateById(id)

    fun getSize() : Int = trackDao.getSize()

    fun clearTrackDataCache()    {
        Timber.d("clearTrackDataCache")
        trackDao.nukeTracksData()
    }

}

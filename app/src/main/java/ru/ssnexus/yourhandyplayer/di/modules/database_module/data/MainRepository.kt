package ru.ssnexus.database_module.data

import io.reactivex.rxjava3.core.Observable
import ru.ssnexus.database_module.data.DAO.TrackDao
import ru.ssnexus.database_module.data.entity.JamendoTrackData
import timber.log.Timber

class MainRepository(private val trackDao: TrackDao) {

    fun getTracksDataObservable() : Observable<List<JamendoTrackData>> = trackDao.getCachedTracksObservable()
    fun getFavoritesTracksDataObservable() : Observable<List<JamendoTrackData>> = trackDao.getCachedFavoritesTracksObservable()

    fun getTracksData() : List<JamendoTrackData> = trackDao.getCachedTracks()
    fun getFavoritesTracksData() : List<JamendoTrackData> = trackDao.getCachedFavoritesTracks()

    fun putToDb(list: List<JamendoTrackData>) {
        //Запросы в БД должны быть в отдельном потоке
        trackDao.insertAll(list)
    }

    fun putToFavoritesDb(list: List<JamendoTrackData>) {
        //Запросы в БД должны быть в отдельном потоке
        trackDao.insertAll(list)
    }

    fun getTrackFavStateById(id : Int) : Int = trackDao.getFavStateById(id)

    fun updateTrackFavStateById(id : Int){
        trackDao.updateFavoriteById(id)
    }

    fun isInFavorites(id: Int) = trackDao.isInFavorites(id) > 0

    fun addToFavorites (trackData: JamendoTrackData) {
        trackDao.addToFavorites(trackData)
    }

    fun removeFromFavorites(id: Int) {
        trackDao.removeFormFavorites(id)
    }

//    fun getAllFromDB(): Observable<List<Track>> = trackDao.getTracks()

    fun getSize() : Int = trackDao.getSize()

    fun clearTrackDataCache()    {
        Timber.d("clearTrackDataCache")
        trackDao.nukeTracksData()
    }

    fun clearFavoritesTrackDataCache()    {
        Timber.d("clearFavoritesTrackDataCache")
        trackDao.nukeFavoritesTracksData()
    }
}

package ru.ssnexus.database_module.data

import androidx.room.ColumnInfo
import io.reactivex.rxjava3.core.Observable
import ru.ssnexus.database_module.data.DAO.TrackDao
import ru.ssnexus.database_module.data.entity.JamendoTrackData
import ru.ssnexus.yourhandyplayer.di.modules.database_module.data.entity.FavoritesTrackData
import ru.ssnexus.yourhandyplayer.di.modules.database_module.data.entity.ListenLaterTrackData
import timber.log.Timber

class MainRepository(private val trackDao: TrackDao) {

    fun getTracksDataObservable() : Observable<List<JamendoTrackData>> = trackDao.getCachedTracksObservable()
//    fun getFavoritesTracksDataObservable() : Observable<List<JamendoTrackData>> = trackDao.getFavoritesTracksObservable()
//    fun getListenLaterTracksDataObservable() : Observable<List<JamendoTrackData>> = trackDao.getListenLaterTracksObservable()

    fun putToDb(list: List<JamendoTrackData>) {
        val result = list
        result.forEach {
            if (isInCachedFavorites(it.id))
                it.fav_state = 1
            if (isInCachedListenLater(it.id))
                it.listen_later_state = 1
        }

        trackDao.insertAll(result)
    }

    fun getCachedFavoriteTracks(): List<JamendoTrackData> {
        val list = arrayListOf<JamendoTrackData>()
        trackDao.getCachedFavoritesTracks().forEach {
            list.add(convertFromFavorites(it))
        }
        return list
    }

    fun getCachedListenLaterTracks(): List<JamendoTrackData> {
        val list = arrayListOf<JamendoTrackData>()
        trackDao.getCachedListenLaterTracks().forEach {
            list.add(convertFromListenLater(it))
        }
        return list
    }

    private fun insertIntoFavorites(trackData: JamendoTrackData){
        trackDao.insertFavorite(convertToFavorites(trackData))
    }

    private fun convertToFavorites(trackData: JamendoTrackData): FavoritesTrackData{
        return FavoritesTrackData(
            album_id = trackData.album_id,
            album_image = trackData.album_image,
            album_name = trackData.album_name,
            artist_id = trackData.artist_id,
            artist_idstr = trackData.artist_idstr,
            artist_name = trackData.artist_name,
            audio = trackData.audio,
            audiodownload = trackData.audiodownload,
            audiodownload_allowed = trackData.audiodownload_allowed,
            duration = trackData.duration,
            id = trackData.id,
            image = trackData.image,
            license_ccurl = trackData.license_ccurl,
            acousticelectric = trackData.acousticelectric,
            gender = trackData.gender,
            lang = trackData.lang,
            speed = trackData.speed,
            genres = trackData.genres,
            instruments = trackData.instruments,
            vartags = trackData.vartags,
            vocalinstrumental = trackData.vocalinstrumental,
            name = trackData.name,
            position = trackData.position,
            prourl = trackData.prourl,
            releasedate = trackData.releasedate,
            shareurl = trackData.shareurl,
            shorturl = trackData.shorturl,
            waveform = trackData.waveform,
            listen_later_state = trackData.listen_later_state
        )
    }

    private fun convertFromFavorites(trackData: FavoritesTrackData): JamendoTrackData{
        return JamendoTrackData(
            album_id = trackData.album_id,
            album_image = trackData.album_image,
            album_name = trackData.album_name,
            artist_id = trackData.artist_id,
            artist_idstr = trackData.artist_idstr,
            artist_name = trackData.artist_name,
            audio = trackData.audio,
            audiodownload = trackData.audiodownload,
            audiodownload_allowed = trackData.audiodownload_allowed,
            duration = trackData.duration,
            id = trackData.id,
            image = trackData.image,
            license_ccurl = trackData.license_ccurl,
            acousticelectric = trackData.acousticelectric,
            gender = trackData.gender,
            lang = trackData.lang,
            speed = trackData.speed,
            genres = trackData.genres,
            instruments = trackData.instruments,
            vartags = trackData.vartags,
            vocalinstrumental = trackData.vocalinstrumental,
            name = trackData.name,
            position = trackData.position,
            prourl = trackData.prourl,
            releasedate = trackData.releasedate,
            shareurl = trackData.shareurl,
            shorturl = trackData.shorturl,
            waveform = trackData.waveform,
            listen_later_state = trackData.listen_later_state
        )
    }

    private fun insertIntoListenLater(trackData: JamendoTrackData){
        trackDao.insertListenLater(convertToListenLater(trackData))
    }


    private fun convertToListenLater(trackData: JamendoTrackData): ListenLaterTrackData{
        return ListenLaterTrackData(
            album_id = trackData.album_id,
            album_image = trackData.album_image,
            album_name = trackData.album_name,
            artist_id = trackData.artist_id,
            artist_idstr = trackData.artist_idstr,
            artist_name = trackData.artist_name,
            audio = trackData.audio,
            audiodownload = trackData.audiodownload,
            audiodownload_allowed = trackData.audiodownload_allowed,
            duration = trackData.duration,
            id = trackData.id,
            image = trackData.image,
            license_ccurl = trackData.license_ccurl,
            acousticelectric = trackData.acousticelectric,
            gender = trackData.gender,
            lang = trackData.lang,
            speed = trackData.speed,
            genres = trackData.genres,
            instruments = trackData.instruments,
            vartags = trackData.vartags,
            vocalinstrumental = trackData.vocalinstrumental,
            name = trackData.name,
            position = trackData.position,
            prourl = trackData.prourl,
            releasedate = trackData.releasedate,
            shareurl = trackData.shareurl,
            shorturl = trackData.shorturl,
            waveform = trackData.waveform,
            fav_state = trackData.fav_state
        )
    }

    private fun convertFromListenLater(trackData: ListenLaterTrackData): JamendoTrackData{
        return JamendoTrackData(
            album_id = trackData.album_id,
            album_image = trackData.album_image,
            album_name = trackData.album_name,
            artist_id = trackData.artist_id,
            artist_idstr = trackData.artist_idstr,
            artist_name = trackData.artist_name,
            audio = trackData.audio,
            audiodownload = trackData.audiodownload,
            audiodownload_allowed = trackData.audiodownload_allowed,
            duration = trackData.duration,
            id = trackData.id,
            image = trackData.image,
            license_ccurl = trackData.license_ccurl,
            acousticelectric = trackData.acousticelectric,
            gender = trackData.gender,
            lang = trackData.lang,
            speed = trackData.speed,
            genres = trackData.genres,
            instruments = trackData.instruments,
            vartags = trackData.vartags,
            vocalinstrumental = trackData.vocalinstrumental,
            name = trackData.name,
            position = trackData.position,
            prourl = trackData.prourl,
            releasedate = trackData.releasedate,
            shareurl = trackData.shareurl,
            shorturl = trackData.shorturl,
            waveform = trackData.waveform,
            fav_state = trackData.fav_state
        )
    }

    fun updateTrackFavStateById(id : Int){
        trackDao.updateFavoriteById(id)
        if (trackDao.getFavTrackState(id) > 0) insertIntoFavorites(trackDao.getTrack(id))
                                          else trackDao.removeFromFavoritesTracksData(id)
    }

    fun updateTrackListenLaterStateById(id : Int){
        trackDao.updateListenLaterById(id)
        if (trackDao.getListenLaterTrackState(id) > 0) insertIntoListenLater(trackDao.getTrack(id))
                                                  else trackDao.removeFromListenLaterTracksData(id)
    }

    fun isInFavorites(id: Int) = trackDao.isInFavorites(id) > 0

    fun isInListenLater(id: Int) = trackDao.isInListenLater(id) > 0

    // Проверка трека в сохраненных данных избранного
    private fun isInCachedFavorites(id: Int) = trackDao.isInCachedFavorites(id) > 0

    // Проверка трека в сохраненных данных отложенного
    private fun isInCachedListenLater(id: Int) = trackDao.isInCachedListenLater(id) > 0

    fun getTrackFavStateById(id : Int) : Int = trackDao.getFavStateById(id)

    fun getTrackListenLaterStateById(id : Int) : Int = trackDao.getTrackListenLaterStateById(id)

    fun getSize() : Int = trackDao.getSize()

    fun clearTrackDataCache()    {
        trackDao.nukeTracksData()
    }

}

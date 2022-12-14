package ru.ssnexus.database_module.data.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.rxjava3.core.Observable
import ru.ssnexus.database_module.data.entity.JamendoTrackData
import ru.ssnexus.yourhandyplayer.di.modules.database_module.data.entity.FavoritesTrackData
import ru.ssnexus.yourhandyplayer.di.modules.database_module.data.entity.ListenLaterTrackData

//Помечаем, что это не просто интерфейс, а Dao-объект
@Dao
interface TrackDao {
    //Запрос на всю таблицу
    @Query("SELECT * FROM tracks")
    fun getCachedTracksObservable(): Observable<List<JamendoTrackData>>

    //Запрос на избранное
    @Query("SELECT * FROM tracks WHERE fav_state > 0")
    fun getFavoritesTracksObservable(): Observable<List<JamendoTrackData>>

    //Запрос на посмотреть позднее
    @Query("SELECT * FROM tracks WHERE listen_later_state > 0")
    fun getListenLaterTracksObservable(): Observable<List<JamendoTrackData>>

    //Запрос трека
    @Query("SELECT * FROM tracks WHERE id = :id")
    fun getTrack(id: Int): JamendoTrackData


    //Кладём списком в БД, в случае конфликта перезаписываем
    @Insert(entity = JamendoTrackData::class, onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(list: List<JamendoTrackData>)

    @Query("SELECT * FROM tracks")
    fun getCachedTracks(): List<JamendoTrackData>

//    @Query("SELECT * FROM tracks WHERE fav_state > 0")
//    fun getCachedFavoritesTracks(): List<JamendoTrackData>
    @Query("SELECT * FROM favorites")
    fun getCachedFavoritesTracks(): List<FavoritesTrackData>

    @Query("SELECT fav_state FROM tracks WHERE id = :id")
    fun getFavStateById(id: Int): Int

    @Query("SELECT count(*) FROM tracks WHERE id = :id AND fav_state > 0")
    fun isInFavorites(id: Int): Int

    @Query("UPDATE tracks SET fav_state = fav_state * (-1) WHERE id = :id")
    fun updateFavoriteById(id : Int)

    @Query("SELECT fav_state FROM tracks WHERE id = :id")
    fun getFavTrackState(id: Int): Int

    @Query("SELECT listen_later_state FROM tracks WHERE id = :id")
    fun getListenLaterTrackState(id: Int): Int

    @Query("UPDATE tracks SET listen_later_state = listen_later_state * (-1) WHERE id = :id")
    fun updateListenLaterById(id : Int);

//    @Query("SELECT * FROM tracks WHERE listen_later_state > 0")
//    fun getCachedListenLaterTracks(): List<JamendoTrackData>
    @Query("SELECT * FROM later")
    fun getCachedListenLaterTracks(): List<ListenLaterTrackData>

    @Query("SELECT listen_later_state FROM tracks WHERE id = :id")
    fun getTrackListenLaterStateById(id: Int): Int

    @Query("SELECT count(*) FROM tracks WHERE id = :id AND listen_later_state > 0")
    fun isInListenLater(id: Int): Int

    // Очистка таблицы
    @Query("DELETE FROM tracks")
    fun nukeTracksData()

    @Query("SELECT COUNT(*) FROM tracks")
    fun getSize(): Int

    //Кладём в спиское избранных, в случае конфликта перезаписываем
    @Insert(entity = FavoritesTrackData::class, onConflict = OnConflictStrategy.REPLACE)
    fun insertFavorite(track: FavoritesTrackData)

    //Кладём в список отложенных, в случае конфликта перезаписываем
    @Insert(entity = ListenLaterTrackData::class, onConflict = OnConflictStrategy.REPLACE)
    fun insertListenLater(track: ListenLaterTrackData)

    // Убрать из списка избранных
    @Query("DELETE FROM favorites WHERE id == :id")
    fun removeFromFavoritesTracksData(id: Int)

    // Убрать из списка отложенных
    @Query("DELETE FROM later WHERE id == :id")
    fun removeFromListenLaterTracksData(id: Int)

    // Очистка таблицы
    @Query("DELETE FROM favorites")
    fun nukeFavoritesTracksData()

    // Очистка таблицы
    @Query("DELETE FROM later")
    fun nukeListenLaterTracksData()

    @Query("SELECT count(*) FROM favorites WHERE id = :id ")
    fun isInCachedFavorites(id: Int): Int

    @Query("SELECT count(*) FROM later WHERE id = :id ")
    fun isInCachedListenLater(id: Int): Int
}
package ru.ssnexus.database_module.data.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.rxjava3.core.Observable
import ru.ssnexus.database_module.data.entity.JamendoTrackData

//Помечаем, что это не просто интерфейс, а Dao-объект
@Dao
interface TrackDao {
    //Запрос на всю таблицу
    @Query("SELECT * FROM tracks")
    fun getCachedTracksObservable(): Observable<List<JamendoTrackData>>

    //Запрос на избранное
    @Query("SELECT * FROM tracks WHERE fav_state > 0")
    fun getCachedFavoritesTracksObservable(): Observable<List<JamendoTrackData>>

    //Запрос на посмотреть позднее
    @Query("SELECT * FROM tracks WHERE watch_later_state > 0")
    fun getCachedListenLaterTracksObservable(): Observable<List<JamendoTrackData>>

    //Кладём списком в БД, в случае конфликта перезаписываем
    @Insert(entity = JamendoTrackData::class, onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(list: List<JamendoTrackData>)

    @Query("SELECT * FROM tracks")
    fun getCachedTracks(): List<JamendoTrackData>

    @Query("SELECT * FROM tracks WHERE fav_state > 0")
    fun getCachedFavoritesTracks(): List<JamendoTrackData>

    @Query("SELECT fav_state FROM tracks WHERE id = :id")
    fun getFavStateById(id: Int): Int

    @Query("SELECT count(*) FROM tracks WHERE id = :id AND fav_state > 0")
    fun isInFavorites(id: Int): Int

    @Query("UPDATE tracks SET fav_state = fav_state * (-1) WHERE id = :id")
    fun updateFavoriteById(id : Int)

    @Query("UPDATE tracks SET watch_later_state = watch_later_state * (-1) WHERE id = :id")
    fun updateListenLaterById(id : Int);

    @Query("SELECT * FROM tracks WHERE watch_later_state > 0")
    fun getCachedListenLaterTracks(): List<JamendoTrackData>

    @Query("SELECT watch_later_state FROM tracks WHERE id = :id")
    fun getTrackListenLaterStateById(id: Int): Int

    @Query("SELECT count(*) FROM tracks WHERE id = :id AND watch_later_state > 0")
    fun isInListenLater(id: Int): Int

    // Очистка таблицы
    @Query("DELETE FROM tracks")
    fun nukeTracksData()

    @Query("SELECT COUNT(*) FROM tracks")
    fun getSize(): Int
}
package ru.ssnexus.database_module.data.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.rxjava3.core.Observable
import ru.ssnexus.database_module.data.entity.JamendoTrackData
import ru.ssnexus.yourhandyplayer.di.modules.database_module.data.entity.FavoritesTrackData

//Помечаем, что это не просто интерфейс, а Dao-объект
@Dao
interface TrackDao {
    //Запрос на всю таблицу
    @Query("SELECT * FROM tracks")
    fun getCachedTracksObservable(): Observable<List<JamendoTrackData>>

    //Запрос на избранное
    @Query("SELECT * FROM favorites")
    fun getCachedFavoritesTracksObservable(): Observable<List<JamendoTrackData>>

    //Кладём списком в БД, в случае конфликта перезаписываем
    @Insert(entity = JamendoTrackData::class, onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(list: List<JamendoTrackData>)

    //Кладём списком в БД, в случае конфликта перезаписываем
    @Insert(entity = JamendoTrackData::class, onConflict = OnConflictStrategy.REPLACE)
    fun insertAllFavorites(list: List<JamendoTrackData>)

    @Query("SELECT * FROM tracks")
    fun getCachedTracks(): List<JamendoTrackData>

    @Query("SELECT * FROM favorites")
    fun getCachedFavoritesTracks(): List<JamendoTrackData>

    @Query("SELECT fav_state FROM tracks WHERE id = :id")
    fun getFavStateById(id: Int): Int

    @Query("SELECT count(*) FROM favorites WHERE id = :id")
    fun isInFavorites(id: Int): Int

    @Query("UPDATE tracks SET fav_state = fav_state * (-1) WHERE id = :id")
    fun updateFavoriteById(id : Int)


    // Удаление из избранных
    @Insert(entity = FavoritesTrackData::class, onConflict = OnConflictStrategy.IGNORE)
    fun addToFavorites(trackData: JamendoTrackData)

    // Удаление из избранных
    @Query("DELETE FROM favorites WHERE id = :id")
    fun removeFormFavorites(id : Int)

    // Очистка таблицы
    @Query("DELETE FROM tracks")
    fun nukeTracksData()

    // Очистка таблицы
    @Query("DELETE FROM favorites")
    fun nukeFavoritesTracksData()

    @Query("SELECT COUNT(*) FROM tracks")
    fun getSize(): Int
}
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

    //Кладём списком в БД, в случае конфликта перезаписываем
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(list: List<JamendoTrackData>)

    @Query("SELECT * FROM tracks")
    fun getCachedTracks(): List<JamendoTrackData>

    @Query("SELECT fav_state FROM tracks WHERE id = :id")
    fun getFavStateById(id: Int): Int

    @Query("UPDATE tracks SET fav_state = fav_state * (-1) WHERE id = :id")
    fun updateFavoriteById(id : Int);

    // Очистка таблицы
    @Query("DELETE FROM tracks")
    fun nukeTable()

    @Query("SELECT COUNT(*) FROM tracks")
    fun getSize(): Int
}
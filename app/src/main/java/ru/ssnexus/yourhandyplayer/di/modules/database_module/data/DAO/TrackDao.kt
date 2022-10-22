package ru.ssnexus.database_module.data.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.rxjava3.core.Observable
import ru.ssnexus.database_module.data.entity.Track

//Помечаем, что это не просто интерфейс, а Dao-объект
@Dao
interface TrackDao {
    //Запрос на всю таблицу
    @Query("SELECT * FROM tracks")
    fun getTracks(): Observable<List<Track>>

    //Кладём списком в БД, в случае конфликта перезаписываем
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(list: List<Track>)

    @Query("SELECT * FROM tracks")
    fun getData(): List<Track>

    // Очистка таблицы
    @Query("DELETE FROM tracks")
    fun nukeTable()
}
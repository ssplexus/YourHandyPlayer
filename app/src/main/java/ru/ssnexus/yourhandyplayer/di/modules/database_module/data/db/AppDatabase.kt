package ru.ssnexus.database_module.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.ssnexus.database_module.data.DAO.TrackDao
import ru.ssnexus.database_module.data.entity.Track

@Database(entities = [Track::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun filmDao(): TrackDao
}
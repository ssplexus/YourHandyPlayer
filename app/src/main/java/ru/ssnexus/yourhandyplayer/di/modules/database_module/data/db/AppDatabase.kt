package ru.ssnexus.database_module.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.ssnexus.database_module.data.DAO.TrackDao
import ru.ssnexus.database_module.data.entity.JamendoTrackData
import ru.ssnexus.yourhandyplayer.di.modules.database_module.data.DBConstants
import ru.ssnexus.yourhandyplayer.di.modules.database_module.data.entity.FavoritesTrackData
import ru.ssnexus.yourhandyplayer.di.modules.database_module.data.entity.ListenLaterTrackData

@Database(entities = [JamendoTrackData::class, FavoritesTrackData::class, ListenLaterTrackData::class], version = DBConstants.DB_VERSION, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
}
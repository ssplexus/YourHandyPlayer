package ru.ssnexus.yourhandyplayer.di.modules.database_module

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import ru.ssnexus.database_module.data.DAO.TrackDao
import ru.ssnexus.database_module.data.MainRepository
import ru.ssnexus.database_module.data.db.AppDatabase
import javax.inject.Singleton

@Module
class DatabaseModule {

    @Singleton
    @Provides
    fun provideTrackDao(context: Context) =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            DB_NAME
        ).build().trackDao()

    @Provides
    @Singleton
    fun provideRepository(trackDao: TrackDao) = MainRepository(trackDao)

    companion object{
        const val DB_NAME = "tracks_db"
    }
}
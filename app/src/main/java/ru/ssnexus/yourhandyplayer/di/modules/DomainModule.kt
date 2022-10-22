package ru.ssnexus.yourhandyplayer.di.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import ru.ssnexus.database_module.data.MainRepository
import ru.ssnexus.yourhandyplayer.data.preferences.PreferenceProvider
import ru.ssnexus.yourhandyplayer.di.modules.remote_module.JamendoApi
import ru.ssnexus.yourhandyplayer.domain.Interactor
import javax.inject.Singleton

@Module
class DomainModule (val context: Context) {

    //Нам нужно контекст как-то провайдить, поэтому создаем такой метод
    @Provides
    fun provideContext() = context

    @Singleton
    @Provides
    //Создаем экземпляр SharedPreferences
    fun providePreferences(context: Context) = PreferenceProvider(context)

    @Singleton
    @Provides
    fun provideInteractor(repository: MainRepository, jamendoApi: JamendoApi, preferenceProvider: PreferenceProvider): Interactor {
        return Interactor(repo = repository, retrofitService = jamendoApi, preferences = preferenceProvider)
    }

}


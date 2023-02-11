package ru.ssnexus.yourhandyplayer.di

import dagger.Component
import ru.ssnexus.mymoviesearcher.view.rv_viewholders.TrackViewHolder
import ru.ssnexus.yourhandyplayer.di.modules.DomainModule
import ru.ssnexus.yourhandyplayer.di.modules.database_module.DatabaseModule
import ru.ssnexus.yourhandyplayer.di.modules.remote_module.RemoteModule
import ru.ssnexus.yourhandyplayer.mediaplayer.HandyMediaPlayerSingle
import ru.ssnexus.yourhandyplayer.receivers.ConnectionChecker
import ru.ssnexus.yourhandyplayer.receivers.ReminderBroadcast
import ru.ssnexus.yourhandyplayer.services.PlayerService
import ru.ssnexus.yourhandyplayer.view.MainActivity
import ru.ssnexus.yourhandyplayer.viewmodel.*
import javax.inject.Singleton

@Singleton
@Component(
    //Внедряем все модули, нужные для этого компонента
//    dependencies = [RemoteProvider::class, DatabaseProvider::class],
    modules = [
        RemoteModule::class,
        DatabaseModule::class,
        DomainModule::class
    ]
)

interface AppComponent {

    fun inject(homeFragmentViewModel: HomeFragmentViewModel)
    fun inject(pListFragmentViewModel: PListFragmentViewModel)
    fun inject(tagsSetViewModel: TagsSetViewModel)
    fun inject(detailsViewModel: DetailsViewModel)
    fun inject(trackViewHolder: TrackViewHolder)
    fun inject(reminder: ReminderBroadcast)
    fun inject(connectionChecker: ConnectionChecker)
    fun inject(handyMediaPlayerSingle: HandyMediaPlayerSingle)
    fun inject(playerService: PlayerService)
    fun inject(mainActivity: MainActivity)
}
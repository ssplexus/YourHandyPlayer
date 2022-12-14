package ru.ssnexus.yourhandyplayer.di

import android.content.BroadcastReceiver
import dagger.Component
import ru.ssnexus.mymoviesearcher.view.rv_viewholders.TrackViewHolder
import ru.ssnexus.yourhandyplayer.di.modules.DomainModule
import ru.ssnexus.yourhandyplayer.di.modules.database_module.DatabaseModule
import ru.ssnexus.yourhandyplayer.di.modules.remote_module.RemoteModule
import ru.ssnexus.yourhandyplayer.mediaplayer.HandyMediaPlayer
import ru.ssnexus.yourhandyplayer.receivers.ReminderBroadcast
import ru.ssnexus.yourhandyplayer.view.MainActivity
import ru.ssnexus.yourhandyplayer.viewmodel.DetailsViewModel
import ru.ssnexus.yourhandyplayer.viewmodel.TagsSetViewModel
import ru.ssnexus.yourhandyplayer.viewmodel.HomeFragmentViewModel
import ru.ssnexus.yourhandyplayer.viewmodel.PListFragmentViewModel
import javax.inject.Singleton

@Singleton
@Component(
//    //Внедряем все модули, нужные для этого компонента
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
    fun inject(mainActivity: MainActivity)
}
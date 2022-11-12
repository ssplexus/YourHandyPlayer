package ru.ssnexus.yourhandyplayer.di

import dagger.Component
import ru.ssnexus.yourhandyplayer.di.modules.DomainModule
import ru.ssnexus.yourhandyplayer.di.modules.database_module.DatabaseModule
import ru.ssnexus.yourhandyplayer.di.modules.remote_module.RemoteModule
import ru.ssnexus.yourhandyplayer.mediaplayer.HandyMediaPlayer
import ru.ssnexus.yourhandyplayer.view.MainActivity
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
//    fun inject(handyMediaPlayer: HandyMediaPlayer)
    fun inject(mainActivity: MainActivity)
}
package ru.ssnexus.yourhandyplayer

//import ru.ssnexus.yourhandyplayer.di.AppComponent
//import ru.ssnexus.yourhandyplayer.di.DaggerAppComponent
//import ru.ssnexus.yourhandyplayer.di.modules.DomainModule
//
//import ru.ssnexus.mymoviesearcher.view.notifications.NotificationConstants.CHANNEL_DESCRIPTION
//import ru.ssnexus.mymoviesearcher.view.notifications.NotificationConstants.CHANNEL_ID
//import ru.ssnexus.mymoviesearcher.view.notifications.NotificationConstants.CHANNEL_NAME
import android.app.Application
import android.content.res.Configuration
import ru.ssnexus.yourhandyplayer.di.AppComponent
import ru.ssnexus.yourhandyplayer.di.DaggerAppComponent
import ru.ssnexus.yourhandyplayer.di.modules.DomainModule
import ru.ssnexus.yourhandyplayer.di.modules.database_module.DatabaseModule
import ru.ssnexus.yourhandyplayer.di.modules.remote_module.RemoteModule
import timber.log.Timber

class App : Application() {

    lateinit var dagger: AppComponent

    // Этот метод вызывается при старте приложения до того, как будут созданы другие компоненты приложения
    // Этот метод необязательно переопределять, но это самое хорошее место для инициализации глобальных объектов
    override fun onCreate() {
        super.onCreate()

        if(BuildConfig.DEBUG)
        {
            Timber.plant(Timber.DebugTree())
        }
        instance = this

//        //Создаем компонент
        dagger = DaggerAppComponent.builder()
            .remoteModule(RemoteModule())
            .databaseModule(DatabaseModule())
            .domainModule(DomainModule(this))
            .build()
    }

    // Вызывается при изменении конфигурации, например, поворот
    // Этот метод тоже не обязателен к предопределению
    override fun onConfigurationChanged ( newConfig : Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    // Этот метод вызывается, когда у системы остается мало оперативной памяти
    // и система хочет, чтобы запущенные приложения поумерили аппетиты
    // Переопределять необязательно
    override fun onLowMemory() {
        super.onLowMemory()
    }

//    fun createNotificationChannel(){
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            //Задаем имя, описание и важность канала
//            //Создаем канал, передав в параметры его ID(строка), имя(строка), важность(константа)
//            val mChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
//            //Отдельно задаем описание
//            mChannel.description = CHANNEL_DESCRIPTION
//            //Получаем доступ к менеджеру нотификаций
//            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//            //Регистрируем канал
//            notificationManager.createNotificationChannel(mChannel)
//        }
//    }

    companion object {
        lateinit var instance: App
            private set
    }
}
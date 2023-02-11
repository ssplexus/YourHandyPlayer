package ru.ssnexus.yourhandyplayer.view

import android.bluetooth.BluetoothHeadset
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.ssnexus.database_module.data.entity.JamendoTrackData
import ru.ssnexus.yourhandyplayer.App
import ru.ssnexus.yourhandyplayer.R
import ru.ssnexus.yourhandyplayer.data.preferences.PreferenceProvider
import ru.ssnexus.yourhandyplayer.databinding.ActivityMainBinding
import ru.ssnexus.yourhandyplayer.domain.Interactor
import ru.ssnexus.yourhandyplayer.mediaplayer.HandyMediaPlayerSingle
import ru.ssnexus.yourhandyplayer.receivers.ConnectionChecker
import ru.ssnexus.yourhandyplayer.receivers.HeadsetActionButtonReceiver
import ru.ssnexus.yourhandyplayer.services.PlayerService
import ru.ssnexus.yourhandyplayer.utils.AutoDisposable
import ru.ssnexus.yourhandyplayer.view.fragments.DetailsFragment
import ru.ssnexus.yourhandyplayer.view.fragments.HomeFragment
import ru.ssnexus.yourhandyplayer.view.fragments.SplashScreenFragment
import timber.log.Timber
import java.util.concurrent.Executors
import javax.inject.Inject


class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var interactor: Interactor


    private lateinit var binding: ActivityMainBinding
    private lateinit var receiver: BroadcastReceiver
    private lateinit var tracksLiveData : MutableLiveData<List<JamendoTrackData>>

    private var isHome = false
    private var isExtras = false
    private  var extraTrack: JamendoTrackData? = null

    val autoDisposable = AutoDisposable()

    val handyMediaPlayer = HandyMediaPlayerSingle.instance


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        //Передаем его в метод
        setContentView(binding.root)
        autoDisposable.bindTo(this.lifecycle)
        App.instance.dagger.inject(this)

        // Приёмник внешних событий
        receiver = ConnectionChecker()

        // Фильтр событий
        val filters = IntentFilter().apply {
            addAction(Intent.ACTION_HEADSET_PLUG)
            addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_BATTERY_LOW)
            addAction(Intent.ACTION_BATTERY_OKAY)
        }

        // Регистрация приёмника
        registerReceiver(receiver, filters)

        // Регмстрация сервиса плеера
        bindService(Intent(this, PlayerService::class.java),
            HandyMediaPlayerSingle.instance.getServiceConnection(), BIND_AUTO_CREATE)

//        initBTHeadSetReceiver()

        title = ""

        //Запускаем фрагмент при старте
        if(savedInstanceState == null)
        {
            val extras = intent.extras
            if(extras != null)
            {
                isExtras = true
                extraTrack = extras.get(R.string.parcel_item_track.toString()) as JamendoTrackData
            }
        }

        Executors.newSingleThreadExecutor().execute {
            launchSplashScreenFragment()
        }

        initMainActivity()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Timber.d("Main:onSaveInstanceState Called")
    }

//    private fun initBTHeadSetReceiver(){
//        HeadsetActionButtonReceiver.delegate = object : HeadsetActionButtonReceiver.Delegate{
//            override fun onMediaButtonSingleClick() {
//                HandyMediaPlayerSingle.instance.onPlay()
//                Timber.d("onMediaButtonSingleClick")
//            }
//
//            override fun onMediaButtonDoubleClick() {
//                Timber.d("onMediaButtonDoubleClick")
//            }
//        }
//        HeadsetActionButtonReceiver.register(this)
//    }

    override fun onResume() {
        Timber.d("onResume")
        super.onResume()
//        initBTHeadSetReceiver()
    }

    override fun onPause() {
        super.onPause()
        HeadsetActionButtonReceiver.unregister(this)
    }

    fun launchStartFragment(){
        if(isExtras) {
            isExtras = false
            extraTrack?.let { launchDetailsFragment(it) }
            return
        }

        launchFragment(HomeFragment())
    }

    fun initMainActivity(){
        CoroutineScope(Dispatchers.IO).launch {
            interactor.clearTrackDataCache()
        }

        interactor.initDataObservers(this)
        tracksLiveData = interactor.getTracksLiveData()

        tracksLiveData.observe(this){
//            handyMediaPlayer.setTrackList(it)
//            if(!it.isEmpty()) handyMediaPlayer.setTrack(it.first())
        }

        when(interactor.getMusicModeFromPreferences()){
            PreferenceProvider.TAGS_MODE -> {
                interactor.setTagsPref()
            }
            PreferenceProvider.FAVORITES_MODE -> {
                interactor.setFavoritesPref()
            }
            else -> {
                interactor.setListenLaterPref()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun launchSplashScreenFragment() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_placeholder, SplashScreenFragment())
            .commit()
    }

    fun launchFragment(fragment: Fragment, tagsBundle:String = "") {
        if(tagsBundle.isBlank())
            //Запускаем фрагмент
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_placeholder, fragment)
                .addToBackStack(null)
                .commitAllowingStateLoss()
        else {
            //Создаем "посылку"
            val bundle = Bundle()
            //Кладем наш трек в "посылку"
            bundle.putString(R.string.parcel_item_tags.toString(), tagsBundle )
            //Прикрепляем нашу "посылку" к фрагменту
            fragment.arguments = bundle
            //Запускаем фрагмент
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_placeholder, fragment)
                .addToBackStack(null)
                .commitAllowingStateLoss()
        }

    }

    fun launchDetailsFragment(track: JamendoTrackData) {
        //Создаем "посылку"
        val bundle = Bundle()
        //Кладем наш трек в "посылку"
        bundle.putParcelable(R.string.parcel_item_track.toString(), track)
        //Кладем фрагмент с деталями в перменную
        val fragment = DetailsFragment()
        //Прикрепляем нашу "посылку" к фрагменту
        fragment.arguments = bundle

        //Запускаем фрагмент
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_placeholder, fragment)
            .addToBackStack(null)
            .commit()
    }


    override fun onBackPressed() {
        if(supportFragmentManager.backStackEntryCount == 1)
        {
            if(isHome)
            AlertDialog.Builder(this)
                .setTitle(R.string.is_exit)
                .setIcon(R.drawable.ic_round_menu_24)
                .setPositiveButton(R.string.yes) { _, _ ->
                    super.onBackPressed()
                    if(android.os.Build.VERSION.SDK_INT >= 21)
                    {
                        finishAndRemoveTask();
                    }
                    else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
                    {
                        finishAffinity();
                    } else{
                        finish()
                    }
                    System.exit(0);
                }
                .setNegativeButton(R.string.no) { _, _ ->

                }
                .show()
            else launchFragment(HomeFragment())
        }
        else
            super.onBackPressed()
    }

    fun getHandyMedialayer() = handyMediaPlayer

    override fun onDestroy() {
        super.onDestroy()
        handyMediaPlayer.onDestroy()
    }
}
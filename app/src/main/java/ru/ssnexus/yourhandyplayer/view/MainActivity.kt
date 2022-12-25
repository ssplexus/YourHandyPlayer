package ru.ssnexus.yourhandyplayer.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.ssnexus.database_module.data.entity.JamendoTrackData
import ru.ssnexus.yourhandyplayer.App
import ru.ssnexus.yourhandyplayer.R
import ru.ssnexus.yourhandyplayer.data.preferences.PreferenceProvider
import ru.ssnexus.yourhandyplayer.databinding.ActivityMainBinding
import ru.ssnexus.yourhandyplayer.domain.Interactor
import ru.ssnexus.yourhandyplayer.mediaplayer.HandyMediaPlayer
import ru.ssnexus.yourhandyplayer.receivers.ConnectionChecker
import ru.ssnexus.yourhandyplayer.utils.AutoDisposable
import ru.ssnexus.yourhandyplayer.utils.addTo
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
    private lateinit var audioManager: AudioManager

    private var isHome = false
    private var isExtras = false
    private  var extraTrack: JamendoTrackData? = null

    val autoDisposable = AutoDisposable()

    var handyMediaPlayer: HandyMediaPlayer? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        //Передаем его в метод
        setContentView(binding.root)
        autoDisposable.bindTo(this.lifecycle)
        App.instance.dagger.inject(this)

        // Приёмник внешних событий
        receiver = ConnectionChecker()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Фильтр событий
        val filters = IntentFilter().apply {
            addAction(Intent.ACTION_HEADSET_PLUG)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_BATTERY_LOW)
            addAction(Intent.ACTION_BATTERY_OKAY)
        }
        // Регистрация приёмника
        registerReceiver(receiver, filters)

        supportActionBar?.hide()
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
        tracksLiveData = MutableLiveData<List<JamendoTrackData>>()

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
        initPlayer()
    }


    fun initPlayer(){
        handyMediaPlayer = HandyMediaPlayer(interactor)
        tracksLiveData.observe(this){
            handyMediaPlayer?.setTrackList(it)
        }
    }

    fun isHomeFragment(flag: Boolean){

        isHome = flag
        var actionBar = getSupportActionBar()
        if (actionBar != null)
            if (!flag)
            {
                actionBar.setDisplayHomeAsUpEnabled(true)
                actionBar.setDisplayShowHomeEnabled(true)
            } else {
                actionBar.setDisplayHomeAsUpEnabled(false)
                actionBar.setDisplayShowHomeEnabled(false)
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

    fun launchFragment(fragment: Fragment) {
        //Запускаем фрагмент
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_placeholder, fragment)
            .addToBackStack(null)
            .commit()
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




    fun isWiredHeadsetOn() = audioManager.isWiredHeadsetOn

    fun getHandyMedialayer() = handyMediaPlayer

    override fun onDestroy() {
        super.onDestroy()
        if(handyMediaPlayer != null) {
            handyMediaPlayer?.onDestroy()
            handyMediaPlayer = null
        }
    }
}
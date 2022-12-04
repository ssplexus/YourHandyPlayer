package ru.ssnexus.yourhandyplayer.view

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
import ru.ssnexus.yourhandyplayer.utils.AutoDisposable
import ru.ssnexus.yourhandyplayer.utils.addTo
import ru.ssnexus.yourhandyplayer.view.fragments.DetailsFragment
import ru.ssnexus.yourhandyplayer.view.fragments.HomeFragment
import timber.log.Timber
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var interactor: Interactor

    private val autoDisposable = AutoDisposable()
    private lateinit var binding: ActivityMainBinding

    var handyMediaPlayer: HandyMediaPlayer? = null

    private lateinit var tracksLiveData : MutableLiveData<List<JamendoTrackData>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        //Передаем его в метод
        setContentView(binding.root)
        autoDisposable.bindTo(this.lifecycle)
        App.instance.dagger.inject(this)

        CoroutineScope(Dispatchers.IO).launch {
            interactor.clearTrackDataCache()
        }
        interactor.initDataObservers(autoDisposable)

        tracksLiveData = interactor.getTracksLiveData()

//        interactor.getTracksByTagsFromApi()
        if(interactor.getMusicModeFromPreferences() == PreferenceProvider.TAGS_MODE)
            interactor.getTracksByTagsFromApi()
        initPlayer()

        //Запускаем фрагмент при старте
        launchFragment(HomeFragment())
    }

    fun isHomeFragment(flag: Boolean){

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
        //Кладем наш фильм в "посылку"
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

    fun bottomNavigationShow(flag : Boolean){
        Timber.d("bottomNavigationShow")
        if(flag) {
            binding.bottomNavigation.visibility = View.VISIBLE
            binding.bottomNavigation.layoutParams.height = resources.getDimension(R.dimen.toolbar_max_height).toInt()
        }else{
            binding.bottomNavigation.visibility = View.INVISIBLE
            binding.bottomNavigation.layoutParams.height = resources.getDimension(R.dimen.toolbar_min_height).toInt()
        }
    }

    override fun onBackPressed() {
        if(supportFragmentManager.backStackEntryCount == 1)
        {
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
        }
        else
            super.onBackPressed()
    }

    fun initPlayer(){
        handyMediaPlayer = HandyMediaPlayer(interactor)
        if (handyMediaPlayer != null) {
            binding.trackControl.setOnClickListener(handyMediaPlayer!!.onClickListener)
            handyMediaPlayer?.playIconState?.observe(this){
                if (it) {
                    binding.trackControl.setImageResource(R.drawable.ic_baseline_stop_24)
                } else {

                    binding.trackControl.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                }
            }

            tracksLiveData.observe(this){
                handyMediaPlayer?.setTrackList(it)
            }
        }
    }

    fun setBottomNavigationTrack(track: JamendoTrackData){

        Timber.d("setBottomNavigationTrack")
        if(handyMediaPlayer?.isPlaying() == true){
            binding.bottomNavigation.visibility = View.VISIBLE
            binding.bottomNavigation.layoutParams.height = resources.getDimension(R.dimen.toolbar_max_height).toInt()
        }

        binding.trackTitle.text = track.name

        Glide.with(binding.bottomNavigation)
                        //Загружаем сам ресурс
                        .load(track.image)
                        //Центруем изображение
                        .centerCrop()
                        //Указываем ImageView, куда будем загружать изображение
                        .into(binding.artAvatar)

    }

    fun getHandyMedialayer() = handyMediaPlayer

    override fun onDestroy() {
        super.onDestroy()
        if(handyMediaPlayer != null) {
            handyMediaPlayer?.onDestroy()
            handyMediaPlayer = null
        }
    }
}
package ru.ssnexus.yourhandyplayer.view

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.ssnexus.database_module.data.entity.JamendoTrackData
import ru.ssnexus.yourhandyplayer.App
import ru.ssnexus.yourhandyplayer.R
import ru.ssnexus.yourhandyplayer.databinding.ActivityMainBinding
import ru.ssnexus.yourhandyplayer.domain.Interactor
import ru.ssnexus.yourhandyplayer.mediaplayer.HandyMediaPlayer
import ru.ssnexus.yourhandyplayer.utils.AutoDisposable
import ru.ssnexus.yourhandyplayer.utils.addTo
import ru.ssnexus.yourhandyplayer.view.fragments.HomeFragment
import timber.log.Timber
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var interactor: Interactor

    private val autoDisposable = AutoDisposable()
    private lateinit var binding: ActivityMainBinding

    var playIconState: BehaviorSubject<Boolean>? = null

    var handyMediaPlayer: HandyMediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        //Передаем его в метод
        setContentView(binding.root)

        autoDisposable.bindTo(lifecycle)

        App.instance.dagger.inject(this)

        CoroutineScope(Dispatchers.IO).launch {
            interactor.clearCache()
        }
        initPlayer()
        interactor.getTracksByTagsFromApi()
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
            playIconState = handyMediaPlayer!!.playIconState
            playIconState!!
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe{
                    if (it) {
                        binding.trackControl.setImageResource(R.drawable.ic_baseline_stop_24)
                    } else {

                        binding.trackControl.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                    }
                }.addTo(autoDisposable)
        }
    }

    fun setBottomNavigationTrack(track: JamendoTrackData){

        Timber.d("setBottomNavigationTrack")
        binding.bottomNavigation.visibility = View.VISIBLE
        binding.bottomNavigation.layoutParams.height = resources.getDimension(R.dimen.toolbar_max_height).toInt()
        binding.trackTitle.text = track.name

        Glide.with(binding.bottomNavigation)
                        //Загружаем сам ресурс
                        .load(track.image)
                        //Центруем изображение
                        .centerCrop()
                        //Указываем ImageView, куда будем загружать изображение
                        .into(binding.artAvatar)

    }

    fun getMedialayer() = handyMediaPlayer

    override fun onDestroy() {
        super.onDestroy()
        if(handyMediaPlayer != null) {
            handyMediaPlayer?.onDestroy()
            handyMediaPlayer = null
        }
    }
}
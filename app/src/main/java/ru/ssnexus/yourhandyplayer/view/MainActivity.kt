package ru.ssnexus.yourhandyplayer.view

import android.content.BroadcastReceiver
import android.media.AudioManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import ru.ssnexus.database_module.data.entity.JamendoTrackData
import ru.ssnexus.yourhandyplayer.App
import ru.ssnexus.yourhandyplayer.R
import ru.ssnexus.yourhandyplayer.databinding.ActivityMainBinding
import ru.ssnexus.yourhandyplayer.domain.Interactor
import ru.ssnexus.yourhandyplayer.view.fragments.HomeFragment
import ru.ssnexus.yourhandyplayer.view.fragments.PListFragment
import java.io.IOException
import java.net.URL
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    private var mediaPlayer:MediaPlayer? = null
//    @Inject
//    lateinit var interactor: Interactor

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        //Передаем его в метод
        setContentView(binding.root)

        initPlayer()
        //App.instance.dagger.inject(this)
        //Инициализируем объект


        //Запускаем фрагмент при старте
        launchFragment(HomeFragment())
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
        if(flag) {
            binding.bottomNavigation.visibility = View.VISIBLE
            binding.bottomNavigation.layoutParams.height = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                150f,
                resources.displayMetrics
            ).toInt()
        }else{
            binding.bottomNavigation.visibility = View.INVISIBLE
            binding.bottomNavigation.layoutParams.height = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                1f,
                resources.displayMetrics
            ).toInt()
        }
    }

    fun setTrack(track: JamendoTrackData){

        binding.bottomNavigation.visibility = View.VISIBLE
        binding.bottomNavigation.layoutParams.height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            150f,
            resources.displayMetrics).toInt()
        binding.trackTitle.text = track.name

        Glide.with(binding.bottomNavigation)
                        //Загружаем сам ресурс
                        .load(track.image)
//            //Центруем изображение
                        .centerCrop()
//            //Указываем ImageView, куда будем загружать изображение
                        .into(binding.artAvatar)

        mediaPlayer?.let {
            if(it.isPlaying){
                it.stop()
                it.reset()
            }
            try{
                it.setDataSource(track.audio)
                it.prepareAsync()
            }
            catch (e:IOException){
                e.printStackTrace()
            }
        }

    }

    fun isPlaying(): Boolean {
        if(mediaPlayer == null) return false
        else{
            mediaPlayer?.let {
                return it.isPlaying
            }
            return false
        }
    }



    fun togglePlayPause(){
        mediaPlayer?.let {
            if(it.isPlaying){
                it.pause()
                binding.trackControl.setImageResource(R.drawable.ic_baseline_play_arrow_24)
            }else{
                it.start()
                binding.trackControl.setImageResource(R.drawable.ic_baseline_stop_24)
            }
        }
    }

    fun initPlayer(){
        binding.trackControl.setOnClickListener {
            togglePlayPause()
        }
        mediaPlayer = MediaPlayer()
        mediaPlayer?.let {
            it.setAudioStreamType(AudioManager.STREAM_MUSIC)
            it.setOnPreparedListener{
                togglePlayPause()
            }
            it.setOnCompletionListener {
                binding.trackControl.setImageResource(R.drawable.ic_baseline_play_arrow_24)
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer != null){
            if(mediaPlayer?.isPlaying == true) mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null

        }
    }
}
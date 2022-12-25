package ru.ssnexus.yourhandyplayer.mediaplayer

import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import android.util.TypedValue
import android.view.View
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import ru.ssnexus.database_module.data.entity.JamendoTrackData
import ru.ssnexus.yourhandyplayer.R
import ru.ssnexus.yourhandyplayer.domain.Interactor
import ru.ssnexus.yourhandyplayer.utils.SingleLiveEvent
import timber.log.Timber
import java.io.IOException
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.concurrent.scheduleAtFixedRate
import kotlin.math.roundToInt

class HandyMediaPlayer (val interactor: Interactor) {

    var playIconState: MutableLiveData<Boolean> = MutableLiveData()
    var progress: MutableLiveData<Int> = MutableLiveData()
    var bufferingLevel: MutableLiveData<Int> = MutableLiveData()
    var duration: MutableLiveData<Int> = MutableLiveData()

    private val mediaPlayer: MediaPlayer = MediaPlayer()
    var onClickListener: View.OnClickListener? = null

    //Плейлист
    private var trackList = listOf<JamendoTrackData>()
    private var currTrack: JamendoTrackData? = null
    private var isOnPlaying  = false

    private var wave = arrayListOf<Int>()

    var waveLiveData: MutableLiveData<Int> = MutableLiveData()

    private val timer: Timer = Timer()
    private var waveTimer: Timer ?= null
    private var wavePos: Int = 0

    init {
        initPlayer()
    }

    private fun onStartTimer(){
        timer.scheduleAtFixedRate(0,1000) {
            if (isOnPlaying) progress.postValue(mediaPlayer.currentPosition)
        }
    }

    private fun onStartWaveTimer(duration : Int){
        Timber.d("onStartWaveTimer")

        if(waveTimer != null) onStopWaveTimer()
        waveTimer = Timer()
        wavePos = 0
//        if(wave.isEmpty() || wave.size == 0 || wave.size > duration) return
//        val period = duration / wave.size
        if(wave.isEmpty()) return
        val period = duration / wave.size

        Timber.d("Period = " + period)
        waveTimer?.scheduleAtFixedRate(0,period.toLong()) {
            if (isOnPlaying) {
                if(!wave.isEmpty()) {
                    var pos = (mediaPlayer.currentPosition.toFloat() / period).roundToInt()
                    if(wavePos < pos) wavePos = pos
                    if(wavePos < wave.size){
                        Timber.d("WavePos (${wave.size}) = " + wavePos + " | " + wave[wavePos] + " | " + mediaPlayer.currentPosition + " | " + pos)
                        waveLiveData.postValue( wave[wavePos++])
                    }
                }
            }
        }
    }

    private fun onStopWaveTimer(){
        Timber.d("onStopWaveTimer")
        waveTimer?.let {
            it.cancel()
            it.purge()
        }
        waveTimer = null
    }


    fun setTrackList(list:List<JamendoTrackData>){
        if(mediaPlayer.isPlaying) mediaPlayer.pause()
        isOnPlaying = false
        playIconState.postValue(false)

        trackList = list
    }

    fun setTrack(track: JamendoTrackData, async : Boolean = true){
        currTrack = track
        if(currTrack != null)
        {
            Timber.d("Set track")
            currTrack?.let {
                val waveForm = it.waveform
                Timber.d("WaveForm = " + waveForm)
                wave = waveForm.split(",").filter { it.isDigitsOnly() }.map { it.toInt() } as ArrayList<Int>
                Timber.d("Wave data size = " + wave.size + " | " + waveForm.split(",").size)
            }


            mediaPlayer?.let {
                if(it.isPlaying) it.stop()
                it.reset()
                try{
                    it.setDataSource(currTrack?.audio)
                    if (async) it.prepareAsync()
                          else {
                                it.prepare()
                                togglePlayPause()
                          }
                }
                catch (e: IOException){
                    e.printStackTrace()
                }
            }
        }
    }

    fun getCurrTrackPos() : Int {
        if (isSetTrack()) {
            return trackList.indexOf(currTrack)
        }
        else return -1
    }

    fun getCurrTrackData() = currTrack

    fun getTrackDataByPos(pos: Int): JamendoTrackData?{
        if(trackList.isEmpty()) return null
        if (pos < 0 || pos >= trackList.size) return null
        return trackList.get(pos)
    }

    fun isSetTrack() : Boolean = if(currTrack != null) true else false

    fun onPlay() {
        if(trackList.isEmpty()) return
        if (!isPlaying()) {
            isOnPlaying = true
            if( !isSetTrack()) trackList?.first().let { setTrack(it) }
            else togglePlayPause()
        }
        else {
            togglePlayPause()
        }
    }

    fun onNextTrack() {
        if(trackList.isEmpty()) return
        var pos : Int = getCurrTrackPos()
        if (pos < 0) if (trackList.size > 1) pos += 1
        if (pos + 1 < trackList.size) pos += 1
        setTrack(trackList.get(pos))
    }

    fun onPrevTrack() {
        if(trackList.isEmpty()) return
        var pos : Int = getCurrTrackPos()
        if (pos < 0) pos += 1
        else if (pos > 0) pos -= 1
        setTrack(trackList.get(pos))
    }

    fun isPlaying(): Boolean {
        var result = false
        if(mediaPlayer != null)
        {
            result = mediaPlayer.isPlaying
        }
        return result
    }

    fun togglePlayPause(){
        mediaPlayer?.let {
            if(it.isPlaying){
                it.pause()
                isOnPlaying = false
                onStopWaveTimer()
                playIconState.postValue(false)
            }else{
                it.start()
                onStartWaveTimer(it.duration)
                isOnPlaying = true
                playIconState.postValue(true)

            }
        }
    }

    fun initPlayer(){
        onClickListener = View.OnClickListener {
            togglePlayPause()
        }
        playIconState.postValue(false)
        mediaPlayer.let {
            it.setOnBufferingUpdateListener { mp, percent ->
                var ratio: Float = percent / 100.0f
                val result = mp.duration * ratio
                bufferingLevel.postValue(result.toInt())
            }
            it.setAudioStreamType(AudioManager.STREAM_MUSIC)
            it.setOnPreparedListener{
                Timber.d("OnPreparedListener")
                progress.postValue(0)
                bufferingLevel.postValue(0)
                duration.postValue(it.duration)
                onStartTimer()
                if (isOnPlaying) togglePlayPause()

            }
            it.setOnCompletionListener {
                onNextTrack()
            }
        }

    }

    fun getMediaPlayer() = mediaPlayer

    fun onDestroy() {
        if (mediaPlayer != null){
            if(mediaPlayer.isPlaying == true) mediaPlayer.stop()
            mediaPlayer.release()
        }
        timer.cancel()
        timer.purge()
    }
}
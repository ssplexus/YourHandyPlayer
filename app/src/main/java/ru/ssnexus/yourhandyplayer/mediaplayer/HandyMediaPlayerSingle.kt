package ru.ssnexus.yourhandyplayer.mediaplayer

import android.media.AudioManager
import android.media.MediaPlayer
import android.view.View
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import ru.ssnexus.database_module.data.entity.JamendoTrackData
import ru.ssnexus.yourhandyplayer.domain.Interactor
import ru.ssnexus.yourhandyplayer.utils.SingleLiveEvent
import timber.log.Timber
import java.io.IOException
import java.util.*
import java.util.concurrent.Executors
import javax.inject.Inject

class HandyMediaPlayerSingle () {

    //Инициализируем интерактор
    @Inject
    lateinit var interactor: Interactor

    var scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    var playIconState: MutableLiveData<Boolean> = MutableLiveData()
    var progress: MutableLiveData<Int> = MutableLiveData()
    var bufferingLevel: MutableLiveData<Int> = MutableLiveData()
    var duration: MutableLiveData<Int> = MutableLiveData()

    var onSetTrackLiveEvent: SingleLiveEvent<Boolean> = SingleLiveEvent()
    private val mediaPlayer: MediaPlayer = MediaPlayer()
    var onClickListener: View.OnClickListener? = null

    //Плейлист
    private var trackList = listOf<JamendoTrackData>()
    private var currTrack: JamendoTrackData? = null
    private var isOnPlaying  = false

    private var wave = arrayListOf<Int>()

    var waveLiveData: SingleLiveEvent<Int> = SingleLiveEvent()

    private var wavePos: Int = 0

    init {
        initPlayer()
    }

    private fun onStartProgress(){
        Timber.d("onStartProgress")
        scope.launch {
            Timber.d("scope.launch 1")
            while(true){
                delay(1000)
                progress.postValue(mediaPlayer.currentPosition)
            }
        }
        scope.launch {
            Timber.d("scope.launch 2")
            if(!wave.isEmpty()) {
            val period = mediaPlayer.duration / wave.size
            if(period > 0)
                while (true){
                    delay(period.toLong())
                    val pos = mediaPlayer.currentPosition / period
                    if(wavePos != pos && pos !=0){
                        wavePos = pos
                        if(wavePos < wave.size){
                            waveLiveData.postValue( wave[wavePos])
                        }
                    }
                }
            }
        }
    }

    private fun onStopProgress(){
        scope.cancel()
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    }

    fun setIsOnPlaying(flag:Boolean){
        isOnPlaying = flag
    }

    fun isOnPlaying() = isOnPlaying

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
            currTrack?.let {
                val waveForm = it.waveform
                wave = waveForm.split(",").map { it.replace(Regex("[^0-9]"), "").toInt() } as ArrayList<Int>
            }
            onSetTrackLiveEvent.postValue(true)
            mediaPlayer.let {
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

    fun getTrackDataByPos(pos: Int): JamendoTrackData?{
        if(trackList.isEmpty()) return null
        if (pos < 0 || pos >= trackList.size) return null
        return trackList.get(pos)
    }

    fun isSetTrack() : Boolean = if(currTrack != null) true else false

    fun onPlay() {
        if(trackList.isEmpty()) return
        if( !isSetTrack()) trackList?.first().let {
            isOnPlaying = true
            setTrack(it)
        }
        else togglePlayPause()
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

    fun isPlaying() = mediaPlayer.isPlaying

    fun togglePlayPause(){
        mediaPlayer.let {
            if(it.isPlaying){
                playIconState.postValue(false)
                it.pause()
                isOnPlaying = false
                onStopProgress()

            }else{
                playIconState.postValue(true)
                it.start()
                onStartProgress()
                isOnPlaying = true
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
                val ratio: Float = percent / 100.0f
                val result = mp.duration * ratio
                bufferingLevel.postValue(result.toInt())
            }
            it.setAudioStreamType(AudioManager.STREAM_MUSIC)
            it.setOnPreparedListener{
                progress.postValue(0)
                bufferingLevel.postValue(0)
                duration.postValue(it.duration)
                if(isOnPlaying) togglePlayPause()
            }
            it.setOnCompletionListener {
                onNextTrack()
            }
        }

    }

    fun getCurrTrack() = currTrack

    fun getMediaPlayer() = mediaPlayer

    fun onDestroy() {
        if (mediaPlayer != null){
            if(mediaPlayer.isPlaying == true) mediaPlayer.stop()
            mediaPlayer.release()
        }
    }
}
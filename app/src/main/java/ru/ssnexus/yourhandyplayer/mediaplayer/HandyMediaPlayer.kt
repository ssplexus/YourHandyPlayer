package ru.ssnexus.yourhandyplayer.mediaplayer

import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import android.util.TypedValue
import android.view.View
import com.bumptech.glide.Glide
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import ru.ssnexus.database_module.data.entity.JamendoTrackData
import ru.ssnexus.yourhandyplayer.R
import ru.ssnexus.yourhandyplayer.domain.Interactor
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class HandyMediaPlayer (val interactor: Interactor) {

    //Отслеживание базы данных
    var tracksData: Observable<List<JamendoTrackData>>

    var playIconState: BehaviorSubject<Boolean> = BehaviorSubject.create()
    var progress: BehaviorSubject<Int> = BehaviorSubject.create()
    var bufferingLevel: BehaviorSubject<Int> = BehaviorSubject.create()
    var duration: BehaviorSubject<Int> = BehaviorSubject.create()

    private val mediaPlayer: MediaPlayer = MediaPlayer()
    var onClickListener: View.OnClickListener? = null

    //Плейлист
    private var trackList = listOf<JamendoTrackData>()
    private var currTrack: JamendoTrackData? = null
    private var isOnPlaying  = false

    private var runnable: Runnable? = null
    private val handler: Handler = Handler()


    init {
        tracksData = interactor.getTracksDataObservable()
        tracksData.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{tracks_data ->
                trackList = tracks_data
            }
        initPlayer()
    }


    fun setTrack(track: JamendoTrackData){
        currTrack = track
        if(currTrack != null) {
            mediaPlayer?.let {
                if(it.isPlaying){
                    //isOnPlaying = true
                    it.stop()
                } //else isOnPlaying = false
                it.reset()
                try{
                    it.setDataSource(currTrack?.audio)
                    it.prepareAsync()
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
                playIconState.onNext(false)
            }else{
                it.start()
                playIconState.onNext(true)
//                updateSeekBar()
            }
        }
    }

    fun initPlayer(){
        onClickListener = View.OnClickListener {
            togglePlayPause()
        }
        playIconState.onNext(false)
        mediaPlayer.let {
            it.setOnBufferingUpdateListener { mp, percent ->
                var ratio: Float = percent / 100.0f
                val result = mp.duration * ratio
                bufferingLevel.onNext(result.toInt())
            }
            it.setAudioStreamType(AudioManager.STREAM_MUSIC)
            it.setOnPreparedListener{
                Timber.d("OnPreparedListener")
                progress.onNext(0)
                bufferingLevel.onNext(0)
                duration.onNext(it.duration)
                updateSeekBar()
                if (isOnPlaying) togglePlayPause()
            }
            it.setOnCompletionListener {
                playIconState.onNext(true)
            }
        }

    }

    fun updateSeekBar(){
        Timber.d("updateSeekBar")
        if (isOnPlaying) progress.onNext(mediaPlayer.currentPosition)
        runnable = Runnable {
            updateSeekBar()
        }
        handler?.postDelayed(runnable!!, 1000)
    }

    fun getMediaPlayer() = mediaPlayer

    fun onDestroy() {
        if (mediaPlayer != null){
            if(mediaPlayer.isPlaying == true) mediaPlayer.stop()
            mediaPlayer.release()
        }
    }
}
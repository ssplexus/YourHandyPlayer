package ru.ssnexus.yourhandyplayer.mediaplayer

import android.content.ComponentName
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.os.IBinder
import android.os.RemoteException
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import ru.ssnexus.database_module.data.entity.JamendoTrackData
import ru.ssnexus.yourhandyplayer.App
import ru.ssnexus.yourhandyplayer.domain.Interactor
import ru.ssnexus.yourhandyplayer.services.PlayerService
import ru.ssnexus.yourhandyplayer.services.PlayerService.PlayerServiceBinder
import ru.ssnexus.yourhandyplayer.utils.SingleLiveEvent
import timber.log.Timber
import javax.inject.Inject


class HandyMediaPlayerSingle () {

    //Инициализируем интерактор
    @Inject
    lateinit var interactor: Interactor

    var scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var exoPlayer: ExoPlayer? = null

    var playIconState: MutableLiveData<Boolean> = MutableLiveData()
    var progress: MutableLiveData<Int> = MutableLiveData()
    var bufferingLevel: MutableLiveData<Int> = MutableLiveData()
    var duration: MutableLiveData<Int> = MutableLiveData()

    var onSetTrackLiveEvent: SingleLiveEvent<Boolean> = SingleLiveEvent()
    private val mediaPlayer: MediaPlayer = MediaPlayer()
    var onClickListener: View.OnClickListener? = null


    private var playerServiceBinder: PlayerServiceBinder? = null
    private var mediaController: MediaControllerCompat? = null
    private lateinit var callback: MediaControllerCompat.Callback
    private lateinit var serviceConnection: ServiceConnection

    //Плейлист
    private var trackList = listOf<JamendoTrackData>()
    private var currTrack: JamendoTrackData? = null
    private var isOnPlaying  = false

    private var wave = arrayListOf<Int>()

    var waveLiveData: SingleLiveEvent<Int> = SingleLiveEvent()
//    val waveDataFlow: MutableSharedFlow<Int> = MutableSharedFlow(replay = 10, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private var wavePos: Int = -1

    private val exoPlayerListener: Player.Listener = object : Player.Listener {
        fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {

        }

        override fun onIsLoadingChanged(isLoading: Boolean) {
            super.onIsLoadingChanged(isLoading)
        }

        fun onPlayBackStateChanged(playWhenReady: Boolean, playbackState: Int) {
            if (playWhenReady && playbackState == ExoPlayer.STATE_ENDED) {
                onNextTrack()
            }

            if(playbackState == ExoPlayer.STATE_BUFFERING){
                Timber.d("Buffering")
                exoPlayer?.let {
                    val ratio: Float = it.bufferedPercentage / 100.0f
                    val result = it.duration * ratio
                    bufferingLevel.postValue(result.toInt())
                }
            }

            if(playbackState == ExoPlayer.STATE_READY){
                Timber.d("Ready")
                progress.postValue(0)
                bufferingLevel.postValue(0)
                exoPlayer?.let{
                    duration.postValue(it.duration.toInt())
                }
                progress.postValue(0)
                bufferingLevel.postValue(0)

                if(isOnPlaying) onPlay()
            }
        }

        fun onPlayerError(error: ExoPlaybackException?) {}

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
            super.onPlaybackParametersChanged(playbackParameters)
        }

        override fun onSeekForwardIncrementChanged(seekForwardIncrementMs: Long) {
            super.onSeekForwardIncrementChanged(seekForwardIncrementMs)
        }
    }

    init {
        initPlayer()
    }

    fun setExoPlayer(player: ExoPlayer?){
        exoPlayer = player
    }

    fun initPlayer(){
        playIconState.postValue(false)

        onClickListener = View.OnClickListener { onPlay() }

        callback = object : MediaControllerCompat.Callback() {
            override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
                if (state == null) return
                val playing = state.state == PlaybackStateCompat.STATE_PLAYING
                isOnPlaying = playing
            }
        }

        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                playerServiceBinder = service as PlayerServiceBinder
                try {
                    mediaController = playerServiceBinder?.getMediaSessionToken()?.let {
                        MediaControllerCompat(
                            App.instance.applicationContext,
                            it
                        )
                    }
                    mediaController?.registerCallback(callback)
                    callback.onPlaybackStateChanged(mediaController?.playbackState)
                } catch (e: RemoteException) {
                    mediaController = null
                }
            }

            override fun onServiceDisconnected(name: ComponentName) {
                playerServiceBinder = null
                if (mediaController != null) {
                    mediaController?.unregisterCallback(callback)
                    mediaController = null
                }
            }
        }
    }

    fun onDestroy(){
        playerServiceBinder = null
        if (mediaController != null) {
            mediaController?.unregisterCallback(callback)
            mediaController = null
        }
    }

    fun getServiceConnection() = serviceConnection

    fun onPlay(){
        mediaController?.let {
            if(isOnPlaying){
                it.transportControls.pause()
                //                onStopProgress()
            } else {
                it.transportControls.play()
                //                onStartProgress()
            }
            playIconState.postValue(!isOnPlaying)
        }
    }

    fun onNextTrack(){
        mediaController?.let {
         it.getTransportControls().skipToNext()
        }
    }

    fun onPrevTrack(){
        mediaController?.let {
            it.getTransportControls().skipToPrevious()
        }
    }

    fun getExoPlayerListener() = exoPlayerListener

    fun isOnPlaying() = isOnPlaying

    companion object {
        val instance = HandyMediaPlayerSingle()
    }
}
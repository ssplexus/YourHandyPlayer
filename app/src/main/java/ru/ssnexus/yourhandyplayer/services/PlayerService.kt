package ru.ssnexus.yourhandyplayer.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media.session.MediaButtonReceiver
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.database.DatabaseProvider
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSource
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import okhttp3.OkHttpClient
import ru.ssnexus.yourhandyplayer.notifications.NotificationConstants
import ru.ssnexus.yourhandyplayer.view.MainActivity
import java.io.File


class PlayerService : Service() {

    private val NOTIFICATION_ID = 404
    private val NOTIFICATION_DEFAULT_CHANNEL_ID = "default_channel"

    private val metadataBuilder = MediaMetadataCompat.Builder()

    private val stateBuilder = PlaybackStateCompat.Builder().setActions(
        PlaybackStateCompat.ACTION_PLAY
                or PlaybackStateCompat.ACTION_STOP
                or PlaybackStateCompat.ACTION_PAUSE
                or PlaybackStateCompat.ACTION_PLAY_PAUSE
                or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
    )

    private var mediaSession: MediaSessionCompat? = null

    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var audioFocusRequested = false

    private var exoPlayer: ExoPlayer? = null
    private var extractorsFactory: ExtractorsFactory? = null

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant") val notificationChannel = NotificationChannel(
                NOTIFICATION_DEFAULT_CHANNEL_ID,
                NotificationConstants.CHANNEL_AUDIO,
                NotificationManagerCompat.IMPORTANCE_DEFAULT
            )
            val notificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
            val audioAttributes: AudioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .setAcceptsDelayedFocusGain(false)
                .setWillPauseWhenDucked(true)
                .setAudioAttributes(audioAttributes)
                .build()
        }

        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        mediaSession = MediaSessionCompat(this, "PlayerService")
        mediaSession?.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        mediaSession?.setCallback(mediaSessionCallback)


        val appContext = applicationContext

        val activityIntent = Intent(appContext, MainActivity::class.java)
        mediaSession?.setSessionActivity(
            PendingIntent.getActivity(
                appContext,
                0,
                activityIntent,
                0
            )
        )

        val mediaButtonIntent = Intent(
            Intent.ACTION_MEDIA_BUTTON, null, appContext,
            MediaButtonReceiver::class.java
        )
        mediaSession?.setMediaButtonReceiver(
            PendingIntent.getBroadcast(
                appContext,
                0,
                mediaButtonIntent,
                0
            )
        )


//
//        (
//            this,
//            DefaultRenderersFactory(this),
//            DefaultTrackSelector(),
//            DefaultLoadControl()
//        )
//        exoPlayer.addListener(exoPlayerListener)
        val httpDataSourceFactory: DataSource.Factory = OkHttpDataSource.Factory(
            OkHttpClient()
        )

        val evictor = LeastRecentlyUsedCacheEvictor((100 * 1024 * 1024).toLong())
        val databaseProvider: DatabaseProvider = StandaloneDatabaseProvider(appContext)

        val cache = SimpleCache(File(this.cacheDir.absolutePath + "/exoplayer"), evictor, databaseProvider)

        val dataSourceFactory: DataSource.Factory = CacheDataSource.Factory()
            .setCache(cache)
            .setFlags(CacheDataSource.FLAG_BLOCK_ON_CACHE or CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
            .setUpstreamDataSourceFactory(httpDataSourceFactory)

        val mediaSourceFactory : MediaSource.Factory = DefaultMediaSourceFactory(appContext)
            .setDataSourceFactory(dataSourceFactory)

        val loadControl =
            DefaultLoadControl.Builder().setBufferDurationsMs(25000, 50000, 1500, 2000).build()
        val trackSelector = DefaultTrackSelector(appContext)
        val bandwidthMeter = DefaultBandwidthMeter.Builder(appContext).build()

        exoPlayer = ExoPlayer.Builder(appContext)
            .setRenderersFactory(DefaultRenderersFactory(appContext))
            .setMediaSourceFactory(mediaSourceFactory)
            .setTrackSelector(trackSelector)
            .setLoadControl(loadControl)
            .setBandwidthMeter(bandwidthMeter)
            .build()

        extractorsFactory = DefaultExtractorsFactory()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession!!.release()
        exoPlayer!!.release()
    }


    private val audioFocusChangeListener =
        OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_GAIN -> mediaSessionCallback.onPlay() // Не очень красиво
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> mediaSessionCallback.onPause()
                else -> mediaSessionCallback.onPause()
            }
        }
/*
    private val mediaSessionCallback: MediaSessionCompat.Callback =
        object : MediaSessionCompat.Callback() {
            private var currentUri: Uri? = null
            var currentState = PlaybackStateCompat.STATE_STOPPED
            override fun onPlay() {
                if (!exoPlayer!!.playWhenReady) {
                    startService(Intent(applicationContext, PlayerService::class.java))
                    val track: MusicRepository.Track = musicRepository.getCurrent()
                    updateMetadataFromTrack(track)
                    prepareToPlay(track.getUri())
                    if (!audioFocusRequested) {
                        audioFocusRequested = true
                        val audioFocusResult: Int
                        audioFocusResult = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            audioManager!!.requestAudioFocus(audioFocusRequest!!)
                        } else {
                            audioManager!!.requestAudioFocus(
                                audioFocusChangeListener,
                                AudioManager.STREAM_MUSIC,
                                AudioManager.AUDIOFOCUS_GAIN
                            )
                        }
                        if (audioFocusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) return
                    }
                    mediaSession!!.isActive = true // Сразу после получения фокуса
                    registerReceiver(
                        becomingNoisyReceiver,
                        IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
                    )
                    exoPlayer.playWhenReady = true
                }
                mediaSession!!.setPlaybackState(
                    stateBuilder.setState(
                        PlaybackStateCompat.STATE_PLAYING,
                        PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                        1f
                    ).build()
                )
                currentState = PlaybackStateCompat.STATE_PLAYING
                refreshNotificationAndForegroundStatus(currentState)
            }

            override fun onPause() {
                if (exoPlayer!!.playWhenReady) {
                    exoPlayer.playWhenReady = false
                    unregisterReceiver(becomingNoisyReceiver)
                }
                mediaSession!!.setPlaybackState(
                    stateBuilder.setState(
                        PlaybackStateCompat.STATE_PAUSED,
                        PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                        1f
                    ).build()
                )
                currentState = PlaybackStateCompat.STATE_PAUSED
                refreshNotificationAndForegroundStatus(currentState)
            }

            override fun onStop() {
                if (exoPlayer!!.playWhenReady) {
                    exoPlayer.playWhenReady = false
                    unregisterReceiver(becomingNoisyReceiver)
                }
                if (audioFocusRequested) {
                    audioFocusRequested = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        audioManager!!.abandonAudioFocusRequest(audioFocusRequest!!)
                    } else {
                        audioManager!!.abandonAudioFocus(audioFocusChangeListener)
                    }
                }
                mediaSession!!.isActive = false
                mediaSession.setPlaybackState(
                    stateBuilder.setState(
                        PlaybackStateCompat.STATE_STOPPED,
                        PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                        1f
                    ).build()
                )
                currentState = PlaybackStateCompat.STATE_STOPPED
                refreshNotificationAndForegroundStatus(currentState)
                stopSelf()
            }

            override fun onSkipToNext() {
                val track: MusicRepository.Track = musicRepository.getNext()
                updateMetadataFromTrack(track)
                refreshNotificationAndForegroundStatus(currentState)
                prepareToPlay(track.getUri())
            }

            override fun onSkipToPrevious() {
                val track: MusicRepository.Track = musicRepository.getPrevious()
                updateMetadataFromTrack(track)
                refreshNotificationAndForegroundStatus(currentState)
                prepareToPlay(track.getUri())
            }

            private fun prepareToPlay(uri: Uri) {
                if (!uri.equals(currentUri)) {
                    currentUri = uri
                    val mediaSource =
                        ExtractorMediaSource(uri, dataSourceFactory, extractorsFactory, null, null)
                    exoPlayer!!.prepare(mediaSource)
                }
            }

            private fun updateMetadataFromTrack(track: MusicRepository.Track) {
                metadataBuilder.putBitmap(
                    MediaMetadataCompat.METADATA_KEY_ART, BitmapFactory.decodeResource(
                        resources, track.getBitmapResId()
                    )
                )
                metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.getTitle())
                metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, track.getArtist())
                metadataBuilder.putString(
                    MediaMetadataCompat.METADATA_KEY_ARTIST,
                    track.getArtist()
                )
                metadataBuilder.putLong(
                    MediaMetadataCompat.METADATA_KEY_DURATION,
                    track.getDuration()
                )
                mediaSession!!.setMetadata(metadataBuilder.build())
            }
        }*/


}
package ru.ssnexus.yourhandyplayer.services

import android.R
import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.Nullable
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.media.app.NotificationCompat
import androidx.media.session.MediaButtonReceiver
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.database.DatabaseProvider
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSource
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import okhttp3.OkHttpClient
import ru.ssnexus.database_module.data.entity.JamendoTrackData
import ru.ssnexus.yourhandyplayer.domain.Interactor
import ru.ssnexus.yourhandyplayer.mediaplayer.HandyMediaPlayerSingle
import ru.ssnexus.yourhandyplayer.notifications.MediaStyleHelper
import ru.ssnexus.yourhandyplayer.notifications.NotificationConstants
import ru.ssnexus.yourhandyplayer.view.MainActivity
import java.io.File
import javax.inject.Inject


class PlayerService : Service() {
    //Инициализируем интерактор
    @Inject
    lateinit var interactor: Interactor

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

    var dataSourceFactory: DataSource.Factory? = null

    private var mediaSession: MediaSessionCompat? = null

    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var audioFocusRequested = false

    private var exoPlayer: ExoPlayer? = null
    private var extractorsFactory = DefaultExtractorsFactory()


    
    private val becomingNoisyReceiver = object:BroadcastReceiver(){
        override fun onReceive(context: Context, intent: Intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                mediaSessionCallback.onPause();
            }
        }
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
        val httpDataSourceFactory: DataSource.Factory = OkHttpDataSource.Factory(
            OkHttpClient()
        )

        val evictor = LeastRecentlyUsedCacheEvictor((100 * 1024 * 1024).toLong())
        val databaseProvider: DatabaseProvider = StandaloneDatabaseProvider(this)

        val cache = SimpleCache(File(this.cacheDir.absolutePath + "/exoplayer"), evictor, databaseProvider)

        dataSourceFactory = CacheDataSource.Factory()
            .setCache(cache)
            .setFlags(CacheDataSource.FLAG_BLOCK_ON_CACHE or CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
            .setUpstreamDataSourceFactory(httpDataSourceFactory)

        val mediaSourceFactory : MediaSource.Factory = DefaultMediaSourceFactory(this)
            .setDataSourceFactory(dataSourceFactory as CacheDataSource.Factory)

        val loadControl =
            DefaultLoadControl.Builder().setBufferDurationsMs(25000, 50000, 1500, 2000).build()
        val trackSelector = DefaultTrackSelector(this)
        val bandwidthMeter = DefaultBandwidthMeter.Builder(this).build()

        exoPlayer = ExoPlayer.Builder(this)
            .setRenderersFactory(DefaultRenderersFactory(this))
            .setMediaSourceFactory(mediaSourceFactory)
            .setTrackSelector(trackSelector)
            .setLoadControl(loadControl)
            .setBandwidthMeter(bandwidthMeter)
            .build()
        exoPlayer?.addListener(HandyMediaPlayerSingle.instance.getExoPlayerListener())
        HandyMediaPlayerSingle.instance.setExoPlayer(exoPlayer)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession?.release()
        exoPlayer?.release()
    }


    private val audioFocusChangeListener =
        OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_GAIN -> mediaSessionCallback.onPlay() // Не очень красиво
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> mediaSessionCallback.onPause()
                else -> mediaSessionCallback.onPause()
            }
        }

    private val mediaSessionCallback: MediaSessionCompat.Callback =
        object : MediaSessionCompat.Callback() {
            private var currentUri: Uri? = null
            var currentState = PlaybackStateCompat.STATE_STOPPED
            override fun onPlay() {
                if (!exoPlayer?.playWhenReady!!) {
                    startService(Intent(applicationContext, PlayerService::class.java))
                    val track = interactor.repo.getCurrent()
                    updateMetadataFromTrack(track)
                    prepareToPlay(track.audio.toUri())
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
                    mediaSession?.isActive = true // Сразу после получения фокуса
                    registerReceiver(
                        becomingNoisyReceiver,
                        IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
                    )
                    exoPlayer?.playWhenReady = true
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
                    exoPlayer?.playWhenReady = false
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
                    exoPlayer?.playWhenReady = false
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
                mediaSession?.setPlaybackState(
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
                val track: JamendoTrackData = interactor.repo.getNext()
                updateMetadataFromTrack(track)
                refreshNotificationAndForegroundStatus(currentState)
                prepareToPlay(track.audio.toUri())
            }

            override fun onSkipToPrevious() {
                val track: JamendoTrackData = interactor.repo.getPrevious()
                updateMetadataFromTrack(track)
                refreshNotificationAndForegroundStatus(currentState)
                prepareToPlay(track.audio.toUri())
            }

            private fun prepareToPlay(uri: Uri) {
                if (!uri.equals(currentUri)) {
                    currentUri = uri
                    if(dataSourceFactory != null){
                        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory!!,extractorsFactory)
                            .createMediaSource(MediaItem.fromUri(uri))
                        exoPlayer?.setMediaSource(mediaSource)
                        exoPlayer?.prepare()
                    }
                }
            }

            private fun updateMetadataFromTrack(jamendoTrackData: JamendoTrackData) {

                Glide.with(applicationContext)
                    //говорим, что нужен битмап
                    .asBitmap()
                    //указываем, откуда загружать, это ссылка, как на загрузку с API
                    .load(jamendoTrackData.image)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onLoadCleared(placeholder: Drawable?) {
                        }
                        //Этот коллбэк отрабатывает, когда мы успешно получим битмап
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, resource)
                        }
                    })

                metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, jamendoTrackData.name)
                metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, jamendoTrackData.album_name)
                metadataBuilder.putString(
                    MediaMetadataCompat.METADATA_KEY_ARTIST,
                    jamendoTrackData.artist_name
                )
                metadataBuilder.putLong(
                    MediaMetadataCompat.METADATA_KEY_DURATION,
                    jamendoTrackData.duration.toLong()
                )
                mediaSession?.setMetadata(metadataBuilder.build())
            }
        }


    private fun refreshNotificationAndForegroundStatus(playbackState: Int) {
        when (playbackState) {
            PlaybackStateCompat.STATE_PLAYING -> {
                startForeground(NOTIFICATION_ID, getNotification(playbackState))
            }
            PlaybackStateCompat.STATE_PAUSED -> {
                getNotification(playbackState)?.let {
                    NotificationManagerCompat.from(this@PlayerService)
                        .notify(NOTIFICATION_ID, it)
                }
                stopForeground(false)
            }
            else -> {
                stopForeground(true)
            }
        }
    }

    private fun getNotification(playbackState: Int): Notification? {
        val builder = mediaSession?.let { MediaStyleHelper.from(this, it) }
        builder?.addAction(
            androidx.core.app.NotificationCompat.Action(
                R.drawable.ic_media_previous,
                getString(ru.ssnexus.yourhandyplayer.R.string.previous),
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this,
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                )
            )
        )
        if (playbackState == PlaybackStateCompat.STATE_PLAYING) builder?.addAction(
            androidx.core.app.NotificationCompat.Action(
                R.drawable.ic_media_pause,
                getString(ru.ssnexus.yourhandyplayer.R.string.pause),
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this,
                    PlaybackStateCompat.ACTION_PLAY_PAUSE
                )
            )
        ) else builder?.addAction(
            androidx.core.app.NotificationCompat.Action(
                R.drawable.ic_media_play,
                getString(ru.ssnexus.yourhandyplayer.R.string.play),
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this,
                    PlaybackStateCompat.ACTION_PLAY_PAUSE
                )
            )
        )
        builder?.addAction(
            androidx.core.app.NotificationCompat.Action(
                R.drawable.ic_media_next,
                getString(ru.ssnexus.yourhandyplayer.R.string.next),
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this,
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                )
            )
        )
        builder?.setStyle(
            NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(1)
                .setShowCancelButton(true)
                .setCancelButtonIntent(
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this,
                        PlaybackStateCompat.ACTION_STOP
                    )
                )
                .setMediaSession(mediaSession!!.sessionToken)
        ) // setMediaSession требуется для Android Wear
        builder?.setSmallIcon(ru.ssnexus.yourhandyplayer.R.mipmap.ic_launcher)
        builder?.setColor(
            ContextCompat.getColor(
                this,
                ru.ssnexus.yourhandyplayer.R.color.design_default_color_primary_dark
            )
        ) // The whole background (in MediaStyle), not just icon background
        builder?.setShowWhen(false)
        builder?.setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
        builder?.setOnlyAlertOnce(true)
        builder?.setChannelId(NOTIFICATION_DEFAULT_CHANNEL_ID)
        return builder?.build()
    }

    @Nullable
    override fun onBind(intent: Intent?): IBinder? {
        return PlayerServiceBinder()
    }

    inner class PlayerServiceBinder : Binder() {
        fun getMediaSessionToken(): MediaSessionCompat.Token{
            return mediaSession?.sessionToken!!
        }
    }

}
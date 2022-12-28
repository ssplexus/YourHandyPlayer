package ru.ssnexus.yourhandyplayer.view.fragments

import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.bumptech.glide.Glide
import kotlinx.coroutines.*
import ru.ssnexus.database_module.data.entity.JamendoTrackData
import ru.ssnexus.yourhandyplayer.R
import ru.ssnexus.yourhandyplayer.databinding.FragmentDetailsBinding
import ru.ssnexus.yourhandyplayer.databinding.FragmentHomeBinding
import ru.ssnexus.yourhandyplayer.notifications.NotificationHelper
import ru.ssnexus.yourhandyplayer.utils.AutoDisposable
import ru.ssnexus.yourhandyplayer.view.MainActivity
import ru.ssnexus.yourhandyplayer.viewmodel.DetailsViewModel
import ru.ssnexus.yourhandyplayer.viewmodel.HomeFragmentViewModel
import timber.log.Timber

class DetailsFragment : Fragment() {

    private lateinit var binding: FragmentDetailsBinding

    private val autoDisposable = AutoDisposable()

    private val viewModel by lazy {
        ViewModelProvider.NewInstanceFactory().create(DetailsViewModel::class.java)
    }

    private lateinit var track: JamendoTrackData
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        autoDisposable.bindTo(lifecycle)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Получаем наш фильм из переданного бандла
        track = arguments?.get(R.string.parcel_item_track.toString()) as JamendoTrackData

        //Устанавливаем заголовок
        binding.title.text = track.name
        //Устанавливаем описание
        binding.description.text = "Artist: " + track.artist_name + "\nAlbum name: " +
                track.album_name + "\nGenres: " +
                track.genres + "\n" + "Release date: " +
                track.releasedate

        //Устанавливаем картинку
        Glide.with(this)
            .load(track.image)
            .centerCrop()
            .into(binding.poster)

        initButtons()
        initSeekBar()
        (requireActivity() as MainActivity).supportActionBar?.show()
        (requireActivity() as MainActivity).title = "Details"
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).isHomeFragment(false)
    }

    override fun onStop() {
        super.onStop()
        (requireActivity() as MainActivity).isHomeFragment(false)
    }

    private fun initButtons(){

        MainScope().launch {
            scope.async {
                binding.detailsFabFav.setImageResource(
                    if (viewModel.interactor.isInFavorites(track)) R.drawable.ic_baseline_favorite_24
                    else R.drawable.ic_baseline_favorite_border_24
                )

                binding.detailsFabLater.setImageResource(
                    if (viewModel.interactor.isInListenLater(track)) R.drawable.ic_baseline_watch_later_24
                    else R.drawable.ic_outline_watch_later_24
                )
            }
        }

        binding.detailsFabFav.setOnClickListener {

            MainScope().launch {
                scope.async {
                    viewModel.updateTrackFavState(track)
                    if(viewModel.getTrackFavSate(track) >= 1)
                        binding.detailsFabFav.setImageResource(R.drawable.ic_baseline_favorite_24)
                    else
                        binding.detailsFabFav.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                }
            }
        }

        binding.detailsFabLater.setOnClickListener {
            MainScope().launch {
                val job = scope.async {
                    viewModel.updateTrackListenLaterState(track)
                    if(viewModel.getTrackLaterSate(track) >= 1)
                        binding.detailsFabLater.setImageResource(R.drawable.ic_baseline_watch_later_24)
                    else
                        binding.detailsFabLater.setImageResource(R.drawable.ic_outline_watch_later_24)
                }
            }
            NotificationHelper.notificationSet(requireContext(), track)
        }

        binding.detailsFabShare.setOnClickListener {
            (requireActivity() as MainActivity).handyMediaPlayer?.let {

                //Создаем интент
                val intent = Intent()
                //Указываем action с которым он запускается
                intent.action = Intent.ACTION_SEND


                //Кладем данные о нашем фильме
                intent.putExtra(
                    Intent.EXTRA_TEXT,
                    "Check out this track: ${it.getCurrTrackData()?.name} \n\n ${it.getCurrTrackData()?.shareurl}"
                )
                //Указываем MIME тип, чтобы система знала, какое приложения предложить
                intent.type = "text/plain"
                //Запускаем наше активити
                startActivity(Intent.createChooser(intent, "Share To:"))
            }
        }

        binding.forwardButton.setOnClickListener {
            (requireActivity() as MainActivity).handyMediaPlayer?.onNextTrack()
            (requireActivity() as MainActivity).handyMediaPlayer?.getCurrTrackData()?.let {
                //Устанавливаем картинку
                Glide.with(this)
                    .load(it.image)
                    .centerCrop()
                    .into(binding.poster)
                //Устанавливаем заголовок
                binding.title.text = it.name
                //Устанавливаем описание
                binding.description.text = "Artist: " + it.artist_name + "\nAlbum name: " +
                        it.album_name + "\nGenres: " +
                        it.genres + "\n" + "Release date: " +
                        it.releasedate
            }
        }

        binding.backwardButton.setOnClickListener {
            (requireActivity() as MainActivity).handyMediaPlayer?.onPrevTrack()
            (requireActivity() as MainActivity).handyMediaPlayer?.getCurrTrackData()?.let {
                //Устанавливаем картинку
                Glide.with(this)
                    .load(it.image)
                    .centerCrop()
                    .into(binding.poster)
                //Устанавливаем заголовок
                binding.title.text = it.name
                //Устанавливаем описание
                binding.description.text = "Artist: " + it.artist_name + "\nAlbum name: " +
                        it.album_name + "\nGenres: " +
                        it.genres + "\n" + "Release date: " +
                        it.releasedate
            }
        }

        binding.playButton.setOnClickListener {
            (requireActivity() as MainActivity).handyMediaPlayer?.onPlay()
        }

        if ((requireActivity() as MainActivity).handyMediaPlayer != null) {
            (requireActivity() as MainActivity).handyMediaPlayer?.let {
                it.playIconState.observe (viewLifecycleOwner){
                    if (it) {
                        binding.playButton.setIconResource(R.drawable.ic_baseline_pause_24)
                    } else {
                        binding.playButton.setIconResource(R.drawable.ic_baseline_play_arrow_24)
                    }
                }
            }
        }
    }

    private fun initSeekBar(){

        if ((requireActivity() as MainActivity).handyMediaPlayer != null) {
            (requireActivity() as MainActivity).handyMediaPlayer!!.let {
                binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            it.getMediaPlayer()
                                ?.seekTo(progress)
                            seekBar?.setProgress(progress)
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                        //seekBarHint.setVisibility(View.VISIBLE);
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        if(it.isPlaying()) if (seekBar != null) {
                            it.getMediaPlayer().seekTo(seekBar.progress)
                        }
                    }
                })

                it.duration.observe(viewLifecycleOwner){
                    binding.seekBar.max = it
                    binding.seekBar.secondaryProgress = 0
                }
                it.bufferingLevel.observe(viewLifecycleOwner){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        binding.seekBar.secondaryProgress = it
                    }
                }
                it.progress.observe(viewLifecycleOwner){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        binding.seekBar.setProgress(it, false)
                    }
                }
            }
        }
    }
}
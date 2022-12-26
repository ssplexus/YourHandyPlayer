package ru.ssnexus.yourhandyplayer.view.fragments

import android.graphics.drawable.AnimationDrawable
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.annotation.Dimension
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import ru.ssnexus.database_module.data.entity.JamendoTrackData
import ru.ssnexus.mymoviesearcher.view.rv_adapters.TopSpacingItemDecoration
import ru.ssnexus.mymoviesearcher.view.rv_adapters.TrackListRecyclerAdapter
import ru.ssnexus.yourhandyplayer.R
import ru.ssnexus.yourhandyplayer.data.preferences.PreferenceProvider
import ru.ssnexus.yourhandyplayer.databinding.FragmentHomeBinding
import ru.ssnexus.yourhandyplayer.utils.AutoDisposable
import ru.ssnexus.yourhandyplayer.utils.addTo
import ru.ssnexus.yourhandyplayer.view.MainActivity
import ru.ssnexus.yourhandyplayer.viewmodel.HomeFragmentViewModel
import timber.log.Timber
import java.text.SimpleDateFormat


class HomeFragment : Fragment() {

    companion object{
        const val WAVE_STRIPS_CNT = 10
    }

    private var bundle: Bundle? = null
    private lateinit var binding: FragmentHomeBinding
    private lateinit var tracksAdapter: TrackListRecyclerAdapter
    private lateinit var playAnimation: AnimationDrawable

    private var tracksDataBase = listOf<JamendoTrackData>()
        //Используем backing field
        set(value) {
            //Если пришло другое значение, то кладем его в переменную
//            if(field != value){
                field = value
                //Обновляем RV адаптер
                tracksAdapter.addItems(field)
//            }
        }

    private val autoDisposable = AutoDisposable()

    private var waveNum: Int = WAVE_STRIPS_CNT
    private var maxWaveHeight = 0

    private val viewModel by lazy {
        ViewModelProvider.NewInstanceFactory().create(HomeFragmentViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        autoDisposable.bindTo(lifecycle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bundle = savedInstanceState

        initWaveForm()
        initRecycler()
        initSeekBar()
        initButtons()
//        (requireActivity() as MainActivity).supportActionBar?.hide()
        (requireActivity() as MainActivity).title = "Home page"

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).isHomeFragment(true)
    }

    override fun onStop() {
        super.onStop()
        (requireActivity() as MainActivity).isHomeFragment(true)
    }

    fun initWaveForm() {
        (requireActivity() as MainActivity).handyMediaPlayer?.let {
            it.onSetTrackLiveEvent.observe(viewLifecycleOwner) {
                for (i in binding.waveForm.childCount - 1 downTo 0 step 1) {
                    binding.waveForm.getChildAt(i).layoutParams.height =
                        resources.getDimension(R.dimen.wave_height).toInt()
                }
            }

            it.waveLiveData?.observe(viewLifecycleOwner){

                val dip = it.toFloat()
                var px = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    dip,
                    resources.displayMetrics
                )
                var dim = px.toInt()

                maxWaveHeight = binding.waveForm.measuredHeight
                if(dim < resources.getDimension(R.dimen.wave_height).toInt())
                    dim = resources.getDimension(R.dimen.wave_height).toInt()
                else if(dim > maxWaveHeight)
                    dim = maxWaveHeight

                binding.waveForm.getChildAt(--waveNum).layoutParams.height = dim

                if (waveNum <= 0) waveNum = WAVE_STRIPS_CNT
            }
        }
    }

    private fun initButtons(){
        binding.musicModeTview.setOnClickListener {
            if(viewModel.interactor.getMusicModeFromPreferences() == PreferenceProvider.TAGS_MODE)
                (requireActivity() as MainActivity).launchFragment(TagsSetFragment())
        }
        binding.musicListButton.setOnClickListener {
            (requireActivity() as MainActivity).launchFragment(PListFragment())
        }
        
        binding.musicModeButton.setOnClickListener {
            viewModel.changeMusicMode()
        }

        binding.forwardButton.setOnClickListener {
            (requireActivity() as MainActivity).handyMediaPlayer?.onNextTrack()
            (requireActivity() as MainActivity).handyMediaPlayer?.getCurrTrackPos()
                ?.let { it1 -> binding.mainRecycler.scrollToPosition(it1)
                }
        }
        binding.backwardButton.setOnClickListener {
            (requireActivity() as MainActivity).handyMediaPlayer?.onPrevTrack()
            (requireActivity() as MainActivity).handyMediaPlayer?.getCurrTrackPos()
                ?.let{it1 -> binding.mainRecycler.scrollToPosition(it1) }
        }
        binding.playButton.setOnClickListener {
            (requireActivity() as MainActivity).handyMediaPlayer?.onPlay()
        }

        if ((requireActivity() as MainActivity).handyMediaPlayer != null) {
            (requireActivity() as MainActivity).handyMediaPlayer?.let {
                it.playIconState.observe (viewLifecycleOwner){
                    if (it) {
                        binding.playButton.setIconResource(R.drawable.ic_baseline_pause_24)
                        playAnimation.start()
                    } else {
                        binding.playButton.setIconResource(R.drawable.ic_baseline_play_arrow_24)
                        playAnimation.stop()
                        playAnimation.selectDrawable(0)
                    }
                }
            }
        }

        viewModel.modePropertyLiveData.observe(viewLifecycleOwner, Observer<String> {
            when(it){
                PreferenceProvider.TAGS_MODE -> {
                    binding.musicModeTview.text = viewModel.getTagsPreferences()
                    binding.musicModeButton.setImageResource(R.drawable.ic_round_numbers_24)
                }
                PreferenceProvider.FAVORITES_MODE -> {
                    binding.musicModeTview.text = resources.getText(R.string.favorites_cap)
                    binding.musicModeButton.setImageResource(R.drawable.ic_baseline_favorite_24)
                }
                else -> {
                    binding.musicModeTview.text = resources.getText(R.string.listen_later_cap)
                    binding.musicModeButton.setImageResource(R.drawable.ic_baseline_watch_later_24)
                }
            }
        })
        initForm()
    }

    fun initForm(){
        when(viewModel.interactor.getMusicModeFromPreferences()){
            PreferenceProvider.TAGS_MODE -> {
                binding.musicModeTview.text = viewModel.getTagsPreferences()
                binding.musicModeButton.setImageResource(R.drawable.ic_round_numbers_24)
            }
            PreferenceProvider.FAVORITES_MODE -> {
                binding.musicModeTview.text = resources.getText(R.string.favorites_cap)
                binding.musicModeButton.setImageResource(R.drawable.ic_baseline_favorite_24)
            }
            else -> {
                binding.musicModeTview.text = resources.getText(R.string.listen_later_cap)
                binding.musicModeButton.setImageResource(R.drawable.ic_baseline_watch_later_24)
            }
        }

        binding.playAnimation.apply {
            if((requireActivity() as MainActivity).isWiredHeadsetOn())
                setImageResource(R.drawable.headset_anim)
            else
                setImageResource(R.drawable.speaker_anim)
            playAnimation = drawable as AnimationDrawable
        }

        viewModel.connectedDeviceTypeLiveData.observe(viewLifecycleOwner){
            binding.playAnimation.setImageResource(it)
        }
    }

    private fun initSeekBar(){

        if ((requireActivity() as MainActivity).handyMediaPlayer != null) {
            (requireActivity() as MainActivity).handyMediaPlayer?.let {
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
                    binding.playerTotalTimeText.text = msecToTimeStamp(it)

                }

                it.bufferingLevel.observe(viewLifecycleOwner){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        binding.seekBar.secondaryProgress = it
                    }
                }
                it.progress.observe(viewLifecycleOwner){
                    binding.playerCurrentTimeText.text = msecToTimeStamp(it)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        binding.seekBar.setProgress(it, false)
                    }
                }
            }
        }
    }

    fun msecToTimeStamp(msec: Int): String {
        val formatter = SimpleDateFormat("mm:ss")
        return formatter.format(msec)
    }

    private fun initRecycler(){
        //находим наш RV
        binding.mainRecycler.apply {

            setOnTouchListener { view, motionEvent ->
                return@setOnTouchListener true
            }
            tracksAdapter = TrackListRecyclerAdapter(object : TrackListRecyclerAdapter.OnItemClickListener{
                override fun click(track: JamendoTrackData) {
                    (requireActivity() as MainActivity).launchDetailsFragment(track)
                }
            })
            //Присваиваем адаптер
            adapter = tracksAdapter

            //Присвоим layoutmanager
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

            tracksAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver(){
                override fun onChanged() {
                    super.onChanged()
                    if(tracksAdapter.itemCount < 2) return
                    if ((requireActivity() as MainActivity).getHandyMedialayer() != null) {

                        var pos = (requireActivity() as MainActivity).getHandyMedialayer()?.getCurrTrackPos()?:0
                        if(pos >= 0) binding.mainRecycler.scrollToPosition(pos)

                        val lManager = layoutManager
                        if (lManager is LinearLayoutManager)
                        {
                            if(lManager.findLastCompletelyVisibleItemPosition() >= lManager.itemCount - 1)
                            {
                                //Делаем новый запрос трэков на сервер
                                viewModel.getNextTracks()
                            }
                        }
                    }
                }
            })

            //Применяем декоратор для отступов
            val decorator = TopSpacingItemDecoration(8)
            addItemDecoration(decorator)

            viewModel.tagsPropertyLiveData.observe(viewLifecycleOwner, Observer<String> {
                if(viewModel.getMusicMode() == PreferenceProvider.TAGS_MODE) {
                    if(viewModel.tags.isBlank()) viewModel.tags = it
                    else if(!it.equals(viewModel.tags)) viewModel.updateTracks(it)
                }
            })

            viewModel.tracksLiveData.observe(viewLifecycleOwner){
                if(isAdded) {
                    if (it.size > 0){
                        binding.seekBar.visibility = View.VISIBLE
                        binding.waveForm.visibility = View.VISIBLE
                        binding.playerTotalTimeText.visibility = View.VISIBLE
                        binding.playerCurrentTimeText.visibility = View.VISIBLE
                    } else {
                        binding.seekBar.visibility = View.INVISIBLE
                        binding.waveForm.visibility = View.INVISIBLE
                        binding.playerTotalTimeText.visibility = View.INVISIBLE
                        binding.playerCurrentTimeText.visibility = View.INVISIBLE
                    }
                    tracksDataBase = it
                }
            }

            viewModel.showProgressBar
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe{
                    binding.progressBar.isVisible = it
                }.addTo(autoDisposable)
        }
    }
}
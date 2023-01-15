package ru.ssnexus.yourhandyplayer.view.fragments

import android.graphics.drawable.AnimationDrawable
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import ru.ssnexus.database_module.data.entity.JamendoTrackData
import ru.ssnexus.mymoviesearcher.view.rv_adapters.TopSpacingItemDecoration
import ru.ssnexus.mymoviesearcher.view.rv_adapters.TrackListRecyclerAdapter
import ru.ssnexus.yourhandyplayer.R
import ru.ssnexus.yourhandyplayer.data.preferences.PreferenceProvider
import ru.ssnexus.yourhandyplayer.databinding.FragmentHomeBinding
import ru.ssnexus.yourhandyplayer.mediaplayer.HandyMediaPlayerSingle
import ru.ssnexus.yourhandyplayer.utils.AutoDisposable
import ru.ssnexus.yourhandyplayer.utils.addTo
import ru.ssnexus.yourhandyplayer.view.MainActivity
import ru.ssnexus.yourhandyplayer.viewmodel.HomeFragmentViewModel
import timber.log.Timber
import java.text.SimpleDateFormat


class HomeFragment : Fragment() {

    private var bundle: Bundle? = null
    private lateinit var binding: FragmentHomeBinding
    private lateinit var tracksAdapter: TrackListRecyclerAdapter

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
    private val scope = CoroutineScope(Dispatchers.IO + Job())

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

        Timber.d("onCreateView")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bundle = savedInstanceState

        viewModel.tags = arguments?.get(R.string.parcel_item_tags.toString()).toString()
        Timber.d("onViewCreated")
        initWaveForm()
        initRecycler()
        initSeekBar()
        initButtons()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Timber.d("HomeFragment:onSaveInstanceState Called")
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("OnDestroy")
        scope.cancel()
    }

    fun initWaveForm() {
        binding.waveForm.post {
            HandyMediaPlayerSingle.instance.let {
                var pos = it.getWaveDataPos()

                var strVal : String = ""
                for (i in binding.waveForm.childCount - 1 downTo 0 step 1) {
                    if (pos < 0) binding.waveForm.getChildAt(i).layoutParams.height =
                                    resources.getDimension(R.dimen.wave_height).toInt()
                    else {
                        strVal += it.getWaveData()[pos]
                        strVal += " "
                        binding.waveForm.getChildAt(i).layoutParams.height =
                            getWaveHeight(it.getWaveData()[pos--], binding.waveForm.height,
                                resources.getDimension(R.dimen.wave_height).toInt())
                    }
                }
                Timber.d("Recover wave state " + strVal)
            }
        }

        HandyMediaPlayerSingle.instance.let {
            it.onSetTrackLiveEvent.observe(viewLifecycleOwner) {
                Timber.d("onSetTrackLiveEvent")
                for (i in binding.waveForm.childCount - 1 downTo 0 step 1) {
                    binding.waveForm.getChildAt(i).layoutParams.height =
                        resources.getDimension(R.dimen.wave_height).toInt()
                }
            }
            it.waveLiveData.observe(viewLifecycleOwner){
//                Timber.d("it.waveLiveData.observe" + it)
                for (i in binding.waveForm.childCount - 1 downTo 1 step 1) {
                    binding.waveForm.getChildAt(i - 1).layoutParams.height = binding.waveForm.getChildAt(i).height
                }
                binding.waveForm.getChildAt(binding.waveForm.childCount - 1).layoutParams.height = getWaveHeight(it, binding.waveForm.measuredHeight, resources.getDimension(R.dimen.wave_height).toInt())
            }
//            val waveDataFlowCollect = it.waveDataFlow.asSharedFlow()
//            scope.launch {
//                waveDataFlowCollect.collect{
////                    Timber.d("waveLiveData")
//                    binding.waveForm.getChildAt(--waveNum).layoutParams.height = getWaveHeight(it, binding.waveForm.measuredHeight, resources.getDimension(R.dimen.wave_height).toInt())
//                    if (waveNum <= 0) waveNum = HomeFragmentViewModel.WAVE_STRIPS_CNT
//                }
//            }
        }
    }

    fun getWaveHeight(value: Int, maxHeight: Int, minHeight: Int): Int {

        val dip = value.toFloat()
        val px = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dip,
            resources.displayMetrics
        )
        var dim = px.toInt()

        if(dim < minHeight)
            dim = minHeight
        else if(dim > maxHeight)
            dim = maxHeight
        return dim
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
            HandyMediaPlayerSingle.instance.onNextTrack()
            HandyMediaPlayerSingle.instance.getCurrTrackPos()
                .let { binding.mainRecycler.scrollToPosition(it)
                }
        }
        binding.backwardButton.setOnClickListener {
            HandyMediaPlayerSingle.instance.onPrevTrack()
            HandyMediaPlayerSingle.instance.getCurrTrackPos()
                .let{ binding.mainRecycler.scrollToPosition(it) }
        }
        binding.playButton.setOnClickListener {
            HandyMediaPlayerSingle.instance.onPlay()
        }

        HandyMediaPlayerSingle.instance.let {
            it.playIconState.observe (viewLifecycleOwner){
                if (it) {
                    binding.playButton.setIconResource(R.drawable.ic_baseline_pause_24)
                    (binding.playAnimation.drawable as AnimationDrawable).start()
                } else {
                    binding.playButton.setIconResource(R.drawable.ic_baseline_play_arrow_24)
                    (binding.playAnimation.drawable as AnimationDrawable).stop()
                    (binding.playAnimation.drawable as AnimationDrawable).selectDrawable(0)
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

        binding.playAnimation.post{
            binding.playAnimation.apply {
                if(viewModel.interactor.isWiredHeadsetOn() || viewModel.interactor.isBluetoothHeadsetConnected()) {
                    Timber.d("isBluetoothHeadsetConnected")
                    setImageResource(R.drawable.headset_anim)
                }
                else
                    setImageResource(R.drawable.speaker_anim)
            }
        }

        viewModel.connectedDeviceTypeLiveData.observe(viewLifecycleOwner){
            binding.playAnimation.setImageResource(it)
        }
    }

    private fun initSeekBar() {
        HandyMediaPlayerSingle.instance.let {
            binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        it.getMediaPlayer().seekTo(progress)
                        seekBar?.setProgress(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    //seekBarHint.setVisibility(View.VISIBLE);
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    if(it.isPlaying()) if (seekBar != null) {
                        it.getMediaPlayer().seekTo(seekBar.progress)
                        it.updateWavePosition()
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

    private fun msecToTimeStamp(msec: Int): String {
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
                    var pos = HandyMediaPlayerSingle.instance.getCurrTrackPos()
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
            })

            //Применяем декоратор для отступов
            val decorator = TopSpacingItemDecoration(8)
            addItemDecoration(decorator)

            viewModel.tagsPropertyLiveData.observe(viewLifecycleOwner, Observer<String> {
                if(viewModel.getMusicMode() == PreferenceProvider.TAGS_MODE) {
                    Timber.d("viewModel.tags=" + viewModel.tags)
                    if(!viewModel.tags.isBlank())
                        if(!viewModel.tags.equals("null"))
                            viewModel.updateTracks(it)
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
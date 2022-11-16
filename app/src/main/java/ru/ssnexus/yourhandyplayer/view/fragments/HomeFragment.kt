package ru.ssnexus.yourhandyplayer.view.fragments

import android.os.Build
import android.os.Bundle
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
import ru.ssnexus.database_module.data.entity.JamendoTrackData
import ru.ssnexus.mymoviesearcher.view.rv_adapters.TopSpacingItemDecoration
import ru.ssnexus.mymoviesearcher.view.rv_adapters.TrackListRecyclerAdapter
import ru.ssnexus.yourhandyplayer.R
import ru.ssnexus.yourhandyplayer.databinding.FragmentHomeBinding
import ru.ssnexus.yourhandyplayer.utils.AutoDisposable
import ru.ssnexus.yourhandyplayer.utils.addTo
import ru.ssnexus.yourhandyplayer.view.MainActivity
import ru.ssnexus.yourhandyplayer.viewmodel.HomeFragmentViewModel
import timber.log.Timber


class HomeFragment : Fragment() {

    private var bundle: Bundle? = null
    private lateinit var binding: FragmentHomeBinding
    private lateinit var tracksAdapter: TrackListRecyclerAdapter

    private var tracksDataBase = listOf<JamendoTrackData>()
        //Используем backing field
        set(value) {
            //Если пришло другое значение, то кладем его в переменную
            field = value
            //Обновляем RV адаптер
            tracksAdapter.addItems(field)
        }

    private val autoDisposable = AutoDisposable()

    private val viewModel by lazy {
        ViewModelProvider.NewInstanceFactory().create(HomeFragmentViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        Timber.d("Debug_yhp: onCreate")
        autoDisposable.bindTo(lifecycle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.d("Debug_yhp: onCreateView")
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("Debug_yhp: onViewCreated")
        bundle = savedInstanceState
        initRecycler()
        initSeekBar()
        initButtons()
    }

    override fun onSaveInstanceState(outState: Bundle) {

        Timber.d("Debug_yhp: onSaveInstanceState")
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        Timber.d("Debug_yhp: onResume ")
        (requireActivity() as MainActivity).isHomeFragment(true)
    }

    override fun onStop() {
        Timber.d("Debug_yhp: onStop ")
        super.onStop()
        (requireActivity() as MainActivity).isHomeFragment(true)
    }

    private fun initButtons(){
        binding.tagsBtn.setOnClickListener {
            (requireActivity() as MainActivity).launchFragment(TagsSetFragment())
        }
        binding.playListBtn.setOnClickListener {
            (requireActivity() as MainActivity).launchFragment(PListFragment())
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
            (requireActivity() as MainActivity).handyMediaPlayer!!.let {
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
                        Timber.d("Debug_yhp: onProgressChanged " + progress)
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
                    Timber.d("Duration=" + it)
                    binding.seekBar.max = it
                    binding.seekBar.secondaryProgress = 0
                }
                it.bufferingLevel.observe(viewLifecycleOwner){
                    Timber.d("BufferingLevel=" + it)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        binding.seekBar.secondaryProgress = it
                    }
                }
                it.progress.observe(viewLifecycleOwner){
                    Timber.d("Progress=" + it)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        binding.seekBar.setProgress(it, false)
                    }
                }
            }
        }
    }

    private fun initRecycler(){
        //находим наш RV
        binding.mainRecycler.apply {

            setOnTouchListener { view, motionEvent ->
                return@setOnTouchListener true
            }
            tracksAdapter = TrackListRecyclerAdapter(object : TrackListRecyclerAdapter.OnItemClickListener{
                override fun click(track: JamendoTrackData) {
                }
            })
            tracksAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver(){
                override fun onChanged() {
                    super.onChanged()
                    if ((requireActivity() as MainActivity).getHandyMedialayer() != null) {
                        var pos = (requireActivity() as MainActivity).getHandyMedialayer()?.getCurrTrackPos()?:0
                        if(pos >= 0) binding.mainRecycler.scrollToPosition(pos)
                    }
                }
            })
            //Присваиваем адаптер
            adapter = tracksAdapter

            //Присвоим layoutmanager
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            //Применяем декоратор для отступов
            val decorator = TopSpacingItemDecoration(8)
            addItemDecoration(decorator)

            viewModel.tagsPropertyLifeData.observe(viewLifecycleOwner, Observer<String> {
                binding.tagsTv.text = it
            })

            viewModel.tracksData.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe{tracks_data ->
//                    Timber.d("Data!!!")
                    tracksDataBase = tracks_data
                }
            viewModel.showProgressBar
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe{
                    binding.progressBar.isVisible = it
                }.addTo(autoDisposable)
        }
    }
}
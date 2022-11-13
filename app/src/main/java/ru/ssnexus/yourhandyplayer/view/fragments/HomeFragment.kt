package ru.ssnexus.yourhandyplayer.view.fragments

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.*
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

    var playIconState: BehaviorSubject<Boolean>? = null
    var progress: BehaviorSubject<Int>? = null
    var bufferingLevel: BehaviorSubject<Int>? = null
    var duration: BehaviorSubject<Int>? = null


    private val viewModel by lazy {
        ViewModelProvider.NewInstanceFactory().create(HomeFragmentViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        Timber.d("onCreate")
//        if (savedInstanceState?.getInt(SEEK_PROGRESS) != null) {
//            binding.seekBar.progress = savedInstanceState?.getInt(SEEK_PROGRESS)
//            Timber.d("SeekProgress" + savedInstanceState?.getInt(SEEK_PROGRESS))
//        }
//        if (savedInstanceState?.getInt(SEEK_SECOND_PROGRESS) != null)
//            binding.seekBar.secondaryProgress = savedInstanceState?.getInt(SEEK_SECOND_PROGRESS)

        autoDisposable.bindTo(lifecycle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.d("onCreateView")
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("onViewCreated")
        bundle = savedInstanceState
        initRecycler()
        initSeekBar()
        initButtons()
    }

    override fun onSaveInstanceState(outState: Bundle) {

        Timber.d("onSaveInstanceState")
//        outState.putInt(SEEK_PROGRESS, binding.seekBar.progress)
//        outState.putInt(SEEK_SECOND_PROGRESS, binding.seekBar.secondaryProgress)
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        Timber.d("onResume")
        (requireActivity() as MainActivity).isHomeFragment(true)

//        if(bundle != null){
//            binding.seekBar.progress = bundle?.getInt(SEEK_PROGRESS)!!
//            binding.seekBar.secondaryProgress = bundle?.getInt(SEEK_SECOND_PROGRESS)!!
//        }
    }

    override fun onStop() {
        super.onStop()
        Timber.d("onStop")
        (requireActivity() as MainActivity).isHomeFragment(true)

//        if(bundle == null) bundle = Bundle()
//        bundle?.putInt(SEEK_PROGRESS, binding.seekBar.progress)
//        bundle?.putInt(SEEK_SECOND_PROGRESS, binding.seekBar.secondaryProgress)
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
                playIconState = it.playIconState
                playIconState!!
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe{
                        if (it) {
                            binding.playButton.setIconResource(R.drawable.ic_baseline_pause_24)
                        } else {
                            binding.playButton.setIconResource(R.drawable.ic_baseline_play_arrow_24)

                        }
                    }.addTo(autoDisposable)
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

                progress = it.progress
                progress!!
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe{
                        Timber.d("Progress=" + it)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            binding.seekBar.setProgress(it, false)
                        }
                    }.addTo(autoDisposable)

                bufferingLevel = it.bufferingLevel
                bufferingLevel!!
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe{
                        Timber.d("BufferingLevel=" + it)

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            binding.seekBar.secondaryProgress = it
                        }
                    }.addTo(autoDisposable)
                duration = it.duration
                duration!!
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe{
                        Timber.d("Duration=" + it)
                        binding.seekBar.max = it
                        binding.seekBar.secondaryProgress = 0
                    }.addTo(autoDisposable)
            }
            //  AnimationHelper.performFragmentCircularRevealAnimation(binding.homeFragmentRoot, requireActivity(), 1)

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
                    if ((requireActivity() as MainActivity).getMedialayer() != null) {
                        var pos = (requireActivity() as MainActivity).getMedialayer()!!.getCurrTrackPos()
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

    companion object{
        private const val SEEK_PROGRESS = "SEEK_PROGRESS"
        private const val SEEK_SECOND_PROGRESS = "SEEK_SECOND_PROGRESS"
    }
}
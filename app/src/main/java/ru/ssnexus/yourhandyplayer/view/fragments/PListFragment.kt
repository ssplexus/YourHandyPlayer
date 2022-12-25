package ru.ssnexus.yourhandyplayer.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isEmpty
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import ru.ssnexus.database_module.data.entity.JamendoTrackData
import ru.ssnexus.mymoviesearcher.view.rv_adapters.TopSpacingItemDecoration
import ru.ssnexus.mymoviesearcher.view.rv_adapters.TrackListRecyclerAdapter
import ru.ssnexus.yourhandyplayer.R
import ru.ssnexus.yourhandyplayer.data.preferences.PreferenceProvider
import ru.ssnexus.yourhandyplayer.databinding.FragmentPListBinding
import ru.ssnexus.yourhandyplayer.mediaplayer.HandyMediaPlayer
import ru.ssnexus.yourhandyplayer.utils.AutoDisposable
import ru.ssnexus.yourhandyplayer.utils.addTo
import ru.ssnexus.yourhandyplayer.view.MainActivity
import ru.ssnexus.yourhandyplayer.viewmodel.PListFragmentViewModel
import timber.log.Timber

class PListFragment : Fragment() {

    private lateinit var binding: FragmentPListBinding
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
        ViewModelProvider.NewInstanceFactory().create(PListFragmentViewModel::class.java)
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
        binding = FragmentPListBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализируем RecyclerView
        initRecycler()
        initPlayer()
        (requireActivity() as MainActivity).supportActionBar?.show()
        (requireActivity() as MainActivity).title = "Play list"
    }

    override fun onStop() {
        super.onStop()
        (requireActivity() as MainActivity).let {
            it.isHomeFragment(false)
        }
        bottomNavigationShow(false)
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).let {
            if (it.handyMediaPlayer != null) {
                if (it.handyMediaPlayer?.isPlaying() == true) bottomNavigationShow(true)
            }
            it.isHomeFragment(false)
        }
    }

    private fun initRecycler(){
        binding.mainRecycler.apply {

             tracksAdapter = TrackListRecyclerAdapter(object : TrackListRecyclerAdapter.OnItemClickListener{
                override fun click(track: JamendoTrackData) {
                    (requireActivity() as MainActivity).let {
                        it.handyMediaPlayer?.let { hp ->
                            hp.setTrack(track, false)
                            setBottomNavigationTrack(track)
                        }
                    }
                }
            })

            tracksAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver(){
                override fun onChanged() {
                        super.onChanged()
                        (requireActivity() as MainActivity).let {
                            var pos = it.handyMediaPlayer?.getCurrTrackPos()
                            if (pos != null) {
                                if(pos >= 0) binding.mainRecycler.scrollToPosition(pos)
                            }
                            it.getHandyMedialayer()?.let {hmp->
                                hmp.getCurrTrackData()?.let {
                                        track -> setBottomNavigationTrack(track) }
                            }
                        }
                    }
                }
            )
            //Присваиваем адаптер
            adapter = tracksAdapter
            //Присвоим layoutmanager
            layoutManager = LinearLayoutManager(requireContext())
            //Применяем декоратор для отступов
            val decorator = TopSpacingItemDecoration(8)
            addItemDecoration(decorator)

            viewModel.tracksLiveData.observe(viewLifecycleOwner){
                if(isAdded) tracksDataBase = it
            }

            //Делаем refresh на swipe up
            setOnTouchListener { view, motionEvent ->
                if (motionEvent.action == android.view.MotionEvent.ACTION_UP) {
                    if(isEmpty()) return@setOnTouchListener false
                    if(viewModel.interactor.getMusicModeFromPreferences() != PreferenceProvider.TAGS_MODE) return@setOnTouchListener false
                    val lManager = layoutManager
                    if (lManager is LinearLayoutManager)
                    {
                        if(lManager.findLastCompletelyVisibleItemPosition() >= lManager.itemCount - 5)
                        {
                            //Делаем новый запрос трэков на сервер
                            viewModel.getNextTracks()
                        }
                    }
                }
                return@setOnTouchListener false
            }
        }
        viewModel.showProgressBar
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe{
                binding.progressBar.isVisible = it
            }.addTo(autoDisposable)
    }


    fun initPlayer() {
        (requireActivity() as MainActivity).handyMediaPlayer?.let {
            binding.trackControl.setOnClickListener(it.onClickListener)
            it.playIconState?.observe(viewLifecycleOwner){
                if (it) {
                    binding.trackControl.setImageResource(R.drawable.ic_baseline_stop_24)
                } else {
                    binding.trackControl.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                }
            }
        }
    }

    fun setBottomNavigationTrack(track: JamendoTrackData){
        if ((requireActivity() as MainActivity).handyMediaPlayer?.isPlaying() == true){
            bottomNavigationShow(flag = true)
        }
        else
            bottomNavigationShow(flag = false)

        binding.trackTitle.text = track.name

        Glide.with(binding.bottomNavigation)
            //Загружаем сам ресурс
            .load(track.image)
            //Центруем изображение
            .centerCrop()
            //Указываем ImageView, куда будем загружать изображение
            .into(binding.artAvatar)

    }

    fun bottomNavigationShow(flag : Boolean){
        if(flag) {
            binding.bottomNavigation.visibility = View.VISIBLE
            binding.bottomNavigation.layoutParams.height = resources.getDimension(R.dimen.toolbar_max_height).toInt()
        }else{
            binding.bottomNavigation.visibility = View.INVISIBLE
            binding.bottomNavigation.layoutParams.height = resources.getDimension(R.dimen.toolbar_min_height).toInt()
        }
    }
}

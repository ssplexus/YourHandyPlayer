package ru.ssnexus.yourhandyplayer.view.fragments

import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import ru.ssnexus.yourhandyplayer.App
import ru.ssnexus.yourhandyplayer.databinding.ActivityMainBinding
import ru.ssnexus.yourhandyplayer.databinding.FragmentPListBinding
import ru.ssnexus.yourhandyplayer.di.modules.remote_module.entity.jamendo.JamendoTrack
import ru.ssnexus.yourhandyplayer.utils.AutoDisposable
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
    }

    override fun onStop() {
        super.onStop()
        Timber.d("OnStop")
        (requireActivity() as MainActivity).let {
            it.bottomNavigationShow(false)
            it.isHomeFragment(false)
        }
    }

    override fun onResume() {
        super.onResume()
        Timber.d("OnResume")
        (requireActivity() as MainActivity).let {
            if (it.handyMediaPlayer != null) {
                if (it.handyMediaPlayer!!.isPlaying() == true) it.bottomNavigationShow(true)
//                it.handyMediaPlayer!!.getCurrentTrack()
//                    ?.let { it1 -> it.setBottomNavigationTrack(it1) }
            }
            it.isHomeFragment(false)
        }
    }

    private fun initRecycler(){
        //находим наш RV

        binding.mainRecycler.apply {

             tracksAdapter = TrackListRecyclerAdapter(object : TrackListRecyclerAdapter.OnItemClickListener{
                override fun click(track: JamendoTrackData) {
                    (requireActivity() as MainActivity).let {
                        it.handyMediaPlayer?.let { hp ->
                            it.setBottomNavigationTrack(track)
                            hp.setTrack(track)
                        }
                    }
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
            layoutManager = LinearLayoutManager(requireContext())
            //Применяем декоратор для отступов
            val decorator = TopSpacingItemDecoration(8)
            addItemDecoration(decorator)

            viewModel.tracksData.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe{tracks_data ->
                    Timber.d("Data!!!")
                    tracksDataBase = tracks_data
                }
        }
    }

}
package ru.ssnexus.yourhandyplayer.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import ru.ssnexus.database_module.data.entity.Track
import ru.ssnexus.mymoviesearcher.view.rv_adapters.TopSpacingItemDecoration
import ru.ssnexus.mymoviesearcher.view.rv_adapters.TrackListRecyclerAdapter
import ru.ssnexus.yourhandyplayer.databinding.FragmentPListBinding
import ru.ssnexus.yourhandyplayer.di.modules.remote_module.entity.jamendo.JamendoTrack
import ru.ssnexus.yourhandyplayer.utils.AutoDisposable
import ru.ssnexus.yourhandyplayer.utils.addTo
import ru.ssnexus.yourhandyplayer.viewmodel.PListFragmentViewModel
import timber.log.Timber

class PListFragment : Fragment() {

    companion object {
        fun newInstance() = PListFragment()
    }

    private lateinit var binding: FragmentPListBinding
    private lateinit var tracksAdapter: TrackListRecyclerAdapter

    private val autoDisposable = AutoDisposable()

    private val viewModel by lazy {
        ViewModelProvider.NewInstanceFactory().create(PListFragmentViewModel::class.java)
    }

    private var tracksDataBase = listOf<JamendoTrack>()
        //Используем backing field
        set(value) {
            //Если пришло другое значение, то кладем его в переменную
            field = value
            //Обновляем RV адаптер
            tracksAdapter.addItems(field)
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

    private fun initRecycler(){
        //находим наш RV
        binding.mainRecycler.apply {

            tracksAdapter = TrackListRecyclerAdapter(object : TrackListRecyclerAdapter.OnItemClickListener{
                override fun click(track: JamendoTrack) {

                }
            })
            //Присваиваем адаптер
            adapter = tracksAdapter
            //Присвоим layoutmanager
            layoutManager = LinearLayoutManager(requireContext())
            //Применяем декоратор для отступов
            val decorator = TopSpacingItemDecoration(8)
            addItemDecoration(decorator)

            viewModel.trackListData?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe{tracks_data ->
                    Timber.d("Data!!!")
                    tracksDataBase = tracks_data.results
                }?.addTo(autoDisposable)

            viewModel.getTracksFromApi()
//            viewModel.showProgressBar
//                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
//                .subscribe{
//                    binding.progressBar.isVisible = it
//                }.addTo(autoDisposable)
        }
    }

}
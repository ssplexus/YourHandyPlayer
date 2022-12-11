package ru.ssnexus.mymoviesearcher.view.rv_viewholders

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.*
import ru.ssnexus.database_module.data.entity.JamendoTrackData
import ru.ssnexus.yourhandyplayer.App
import ru.ssnexus.yourhandyplayer.R
import ru.ssnexus.yourhandyplayer.databinding.TrackItemBinding
import ru.ssnexus.yourhandyplayer.di.modules.remote_module.entity.jamendo.JamendoTrack
import ru.ssnexus.yourhandyplayer.domain.Interactor
import timber.log.Timber
import javax.inject.Inject

class TrackViewHolder(val binding: TrackItemBinding) : RecyclerView.ViewHolder(binding.root) {

    //Инициализируем интерактор
    @Inject
    lateinit var interactor: Interactor

    //Привязываем View из layout к переменным
    private val title = binding.title
    private val poster = binding.poster
    private val description = binding.description
    private val fav_btn = binding.favorite

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        App.instance.dagger.inject(this)
    }

    fun bind(track: JamendoTrackData) {

        MainScope().launch {
            scope.async {
                fav_btn.setImageResource(
                    if (interactor.isInFavorites(track)) R.drawable.ic_baseline_favorite_24
                                                    else R.drawable.ic_baseline_favorite_border_24
                )
            }
        }

        fav_btn.setOnClickListener {
            MainScope().launch {
                scope.async {
                    interactor.updateTrackFavState(track)
                    if (!interactor.isInFavorites(track))
                        fav_btn.setImageResource(R.drawable.ic_baseline_favorite_24)
                    else
                        fav_btn.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                }
            }
        }

        //Устанавливаем заголовок
        title.text = track.name

        //Указываем контейнер, в котором будет "жить" наша картинка
        Glide.with(itemView)
            //Загружаем сам ресурс
            .load(track.image)
//            //Центруем изображение
            .centerCrop()
//            //Указываем ImageView, куда будем загружать изображение
            .into(poster)

        //Устанавливаем описание
        description.text = track.album_name + "\n" + track.genres

    }
}
package ru.ssnexus.mymoviesearcher.view.rv_viewholders

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.ssnexus.database_module.data.entity.Track
import ru.ssnexus.yourhandyplayer.databinding.TrackItemBinding
import ru.ssnexus.yourhandyplayer.di.modules.remote_module.entity.jamendo.JamendoTrack

class TrackViewHolder(val binding: TrackItemBinding) : RecyclerView.ViewHolder(binding.root) {
    //Привязываем View из layout к переменным
    private val title = binding.title
    private val poster = binding.poster
    private val description = binding.description

    fun bind(track: JamendoTrack) {
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
        description.text = track.album_name

    }
}
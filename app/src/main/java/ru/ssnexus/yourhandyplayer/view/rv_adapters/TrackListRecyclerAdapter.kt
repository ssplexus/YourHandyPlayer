package ru.ssnexus.mymoviesearcher.view.rv_adapters
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import ru.ssnexus.database_module.data.entity.JamendoTrackData

import ru.ssnexus.mymoviesearcher.view.rv_viewholders.TrackViewHolder
import ru.ssnexus.yourhandyplayer.R
import ru.ssnexus.yourhandyplayer.di.modules.remote_module.entity.jamendo.JamendoTrack
import timber.log.Timber


//в параметр передаем слушатель, чтобы мы потом могли обрабатывать нажатия из класса Activity
class TrackListRecyclerAdapter(private val clickListener: OnItemClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    //Здесь у нас хранится список элементов для RV
    private var items = mutableListOf<JamendoTrackData>()

    fun setItems(items: ArrayList<JamendoTrackData>) {
        this.items = items as MutableList<JamendoTrackData>
    }

    fun getItems() : List<JamendoTrackData>{
        return items
    }

    fun clear()
    {
        items.clear()
    }

    //Этот метод нужно переопределить на возврат количества элементов в списке RV
    override fun getItemCount() = items.size

    //В этом методе мы привязываем наш ViewHolder и передаем туда "надутую" верстку нашего фильма
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val context = LayoutInflater.from(parent.context)
        return TrackViewHolder(DataBindingUtil.inflate(context, R.layout.track_item, parent, false))
    }

    //В этом методе будет привязка полей из объекта Track к View из Track_item.xml
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        //Проверяем какой у нас ViewHolder
        when (holder) {
            is TrackViewHolder -> {
                 var TrackViewHolder : TrackViewHolder = holder

                //Вызываем метод bind(), который мы создали, и передаем туда объект
                //из нашей базы данных с указанием позиции
                TrackViewHolder.bind(items[position])
                //Обрабатываем нажатие на весь элемент целиком(можно сделать на отдельный элемент
                //например, картинку) и вызываем метод нашего листенера, который мы получаем из
                //конструктора адаптера
                TrackViewHolder.binding.itemContainer.setOnClickListener{
                    clickListener.click(items[position])
                }

            }
        }
    }


    //Метод для добавления объектов в наш список
    fun addItems(list: List<JamendoTrackData>) {
        Timber.d("addItems")
        val newList = arrayListOf<JamendoTrackData>()
        //newList.addAll(getItems() + list)
        newList.addAll(list)
        setItems(newList)
        notifyDataSetChanged()

//        val  diff = ItemDiffUtil(getItems(), newList)
//        val difResult = DiffUtil.calculateDiff(diff)
//        setItems(newList)
//        difResult.dispatchUpdatesTo(this)
    }

    fun clearRV(){
        val size: Int = items.size
        items.clear()
        notifyItemRangeRemoved(0, size)
    }

    //Интерфейс для обработки кликов
    interface OnItemClickListener {
        fun click(track: JamendoTrackData)
    }
}
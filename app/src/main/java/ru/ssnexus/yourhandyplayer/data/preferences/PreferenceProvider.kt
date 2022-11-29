package ru.ssnexus.yourhandyplayer.data.preferences

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import timber.log.Timber

class PreferenceProvider(context: Context) {
    //Нам нужен контекст приложения
    private val appContext = context.applicationContext
    //Создаем экземпляр SharedPreferences
    private val preference: SharedPreferences = appContext.getSharedPreferences("settings", Context.MODE_PRIVATE)

    val modePropertyLiveData: MutableLiveData<String> = MutableLiveData()

    init {
        //Логика для первого запуска приложения, чтобы положить наши настройки,
        //Сюда потом можно добавить и другие настройки
        //preference.edit().clear().commit()
        if(preference.getBoolean(KEY_FIRST_LAUNCH, true)) {
            preference.edit { putString(KEY_TAGS, DEFAULT_TAGS) }
            preference.edit { putString(KEY_MODE, DEFAULT_MODE) }
            preference.edit { putBoolean(KEY_FIRST_LAUNCH, false) }
            preference.edit { putLong(KEY_FIRST_LAUNCH_TIME, System.currentTimeMillis())}
        }
    }

    //Сохраняем теги
    fun saveTags(tags: String) {
        preference.edit { putString(KEY_TAGS, tags) }
    }
    //Забираем теги
    fun getDefaultTags(): String {
        return preference.getString(KEY_TAGS, DEFAULT_TAGS) ?: DEFAULT_TAGS
    }

    //Поменять режим
    fun changeMode() {
        if(getMode() == TAGS_MODE) {
            preference.edit { putString(KEY_MODE, FAVORITES_MODE) }
            modePropertyLiveData.postValue(FAVORITES_MODE)
        }
        else{
            preference.edit { putString(KEY_MODE, TAGS_MODE) }
            modePropertyLiveData.postValue(TAGS_MODE)
        }
    }

    //Получить режим
    fun getMode(): String {
        return preference.getString(KEY_MODE, DEFAULT_MODE) ?: DEFAULT_MODE
    }

    //Получить время первого запуска
    fun getFirstLaunchTime():Long{
        return preference.getLong(KEY_FIRST_LAUNCH_TIME,0)
    }

    //Ключи для наших настроек, по ним мы их будем получать
    companion object {
        const val KEY_FIRST_LAUNCH = "first_launch"
        const val KEY_TAGS = "tags"
        const val KEY_MODE = "mode"
        const val DEFAULT_TAGS = "Choose your mood"
        const val DEFAULT_MODE = "tags_mode"
        const val TAGS_MODE = "tags_mode"
        const val FAVORITES_MODE = "fav_mode"
        const val KEY_FIRST_LAUNCH_TIME = "first_launch_time"
    }
}
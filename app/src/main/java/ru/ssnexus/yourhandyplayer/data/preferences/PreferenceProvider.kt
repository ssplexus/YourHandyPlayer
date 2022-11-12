package ru.ssnexus.yourhandyplayer.data.preferences

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.core.content.edit
import timber.log.Timber

class PreferenceProvider(context: Context) {
    //Нам нужен контекст приложения
    private val appContext = context.applicationContext
    //Создаем экземпляр SharedPreferences
    private val preference: SharedPreferences = appContext.getSharedPreferences("settings", Context.MODE_PRIVATE)

    init {
        //Логика для первого запуска приложения, чтобы положить наши настройки,
        //Сюда потом можно добавить и другие настройки
        //preference.edit().clear().commit()
        if(preference.getBoolean(KEY_FIRST_LAUNCH, true)) {
            preference.edit { putString(KEY_TAGS, DEFAULT_TAGS) }
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

    //Получить время первого запуска
    fun getFirstLaunchTime():Long{
        return preference.getLong(KEY_FIRST_LAUNCH_TIME,0)
    }

    //Ключи для наших настроек, по ним мы их будем получать
    companion object {
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_TAGS = "tags"
        private const val DEFAULT_TAGS = "Choose your mood"
        private const val KEY_FIRST_LAUNCH_TIME = "first_launch_time"
    }
}
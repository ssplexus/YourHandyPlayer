package ru.ssnexus.yourhandyplayer.di.modules.remote_module

import dagger.Module
import dagger.Provides
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.ssnexus.yourhandyplayer.BuildConfig
import ru.ssnexus.yourhandyplayer.di.modules.remote_module.entity.ApiConstants
import ru.ssnexus.yourhandyplayer.di.modules.remote_module.entity.ApiConstants.HTTP_CLIENT_TIMEOUT
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class RemoteModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        //Настраиваем таймауты для медленного интернета
        .callTimeout(HTTP_CLIENT_TIMEOUT.toLong(), TimeUnit.SECONDS)
        .readTimeout(HTTP_CLIENT_TIMEOUT.toLong(), TimeUnit.SECONDS)
        //Добавляем логгер
        .addInterceptor(HttpLoggingInterceptor().apply {
            if (BuildConfig.DEBUG) {
                level = HttpLoggingInterceptor.Level.BASIC
            }
        })
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        //Указываем базовый URL из констант
        .baseUrl(ApiConstants.BASE_URL)
        //Добавляем конвертер
        .addConverterFactory(GsonConverterFactory.create())
        //Добавляем поддержку RxJava
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        //Добавляем кастомный клиент
        .client(okHttpClient)
        .build()

//    @Provides
//    @Singleton
//    fun provideSoundCloudApi(retrofit: Retrofit): SoundCloudApi = retrofit.create(SoundCloudApi::class.java)

    @Provides
    @Singleton
    fun provideJamendoApi(retrofit: Retrofit): JamendoApi = retrofit.create(JamendoApi::class.java)
}
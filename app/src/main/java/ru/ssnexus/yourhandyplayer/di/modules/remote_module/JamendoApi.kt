package ru.ssnexus.yourhandyplayer.di.modules.remote_module

import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET
import retrofit2.http.Query
import ru.ssnexus.yourhandyplayer.di.modules.remote_module.entity.ApiConstants
import ru.ssnexus.yourhandyplayer.di.modules.remote_module.entity.jamendo.JamendoResult

interface JamendoApi {
    @GET("tracks")
    fun getTracksByTags(
        @Query("client_id") client_id: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("fuzzytags") tags: String,
        @Query("include") include: String
    ): Observable<JamendoResult>
}
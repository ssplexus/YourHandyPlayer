package ru.ssnexus.yourhandyplayer.di.modules.remote_module.entity

import android.icu.text.CaseMap
import com.google.gson.annotations.SerializedName

data class SoundCloudTrack(
    @SerializedName("title")
    val mTitle: String,
    @SerializedName("id")
    val api_id: Int,
    @SerializedName("stream_url")
    val mStreamURL: String,
    @SerializedName("artwork_url")
    val mArtworkURL: String,
)


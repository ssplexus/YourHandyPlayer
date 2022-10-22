package ru.ssnexus.yourhandyplayer.di.modules.remote_module.entity.jamendo

data class JamendoTrack(
    val album_id: String,
    val album_image: String,
    val album_name: String,
    val artist_id: String,
    val artist_idstr: String,
    val artist_name: String,
    val audio: String,
    val audiodownload: String,
    val audiodownload_allowed: Boolean,
    val duration: Int,
    val id: String,
    val image: String,
    val license_ccurl: String,
    val musicinfo: Musicinfo,
    val name: String,
    val position: Int,
    val prourl: String,
    val releasedate: String,
    val shareurl: String,
    val shorturl: String,
    val waveform: String
)
package ru.ssnexus.yourhandyplayer.di.modules.remote_module.entity.jamendo

data class JamendoResult(
    val headers: Headers,
    val results: List<JamendoTrack>
)
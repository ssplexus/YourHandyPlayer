package ru.ssnexus.yourhandyplayer.di.modules.remote_module.entity.jamendo

data class Headers(
    val code: Int,
    val error_message: String,
    val next: String,
    val results_count: Int,
    val status: String,
    val warnings: String
)
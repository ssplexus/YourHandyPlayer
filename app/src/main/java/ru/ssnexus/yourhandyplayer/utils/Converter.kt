package ru.ssnexus.yourhandyplayer.utils

import ru.ssnexus.database_module.data.entity.JamendoTrackData
import ru.ssnexus.yourhandyplayer.di.modules.remote_module.entity.jamendo.JamendoResult
import ru.ssnexus.yourhandyplayer.di.modules.remote_module.entity.jamendo.JamendoTrack
import java.text.DateFormat
import java.text.SimpleDateFormat

object Converter {
    fun convertApiListToDtoList(jamendoResult: JamendoResult): List<JamendoTrackData> {
        val result = mutableListOf<JamendoTrackData>()
        jamendoResult.results.forEach {
            result.add(convertApiToDto(it))
        }
        return result
    }

    fun convertApiToDto(jamendoTrack: JamendoTrack): JamendoTrackData {
        return JamendoTrackData(
            mid = 0,
            album_id = jamendoTrack.album_id.toInt(),
            album_image = jamendoTrack.album_image,
            album_name = jamendoTrack.name,
            artist_id = jamendoTrack.artist_id.toInt(),
            artist_idstr = jamendoTrack.artist_idstr,
            artist_name = jamendoTrack.artist_name,
            audio = jamendoTrack.audio,
            audiodownload = jamendoTrack.audiodownload,
            audiodownload_allowed = jamendoTrack.audiodownload_allowed,
            duration = jamendoTrack.duration,
            id = jamendoTrack.id.toInt(),
            image = jamendoTrack.image,
            license_ccurl = jamendoTrack.license_ccurl,
            acousticelectric = jamendoTrack.musicinfo.acousticelectric,
            gender = jamendoTrack.musicinfo.gender,
            lang = jamendoTrack.musicinfo.lang,
            speed = jamendoTrack.musicinfo.speed,
            genres = jamendoTrack.musicinfo.tags.genres.toString(),
            instruments = jamendoTrack.musicinfo.tags.instruments.toString(),
            vartags = jamendoTrack.musicinfo.tags.vartags.toString(),
            vocalinstrumental = jamendoTrack.musicinfo.vocalinstrumental,
            name = jamendoTrack.name,
            position = jamendoTrack.position,
            prourl = jamendoTrack.prourl,
            releasedate = jamendoTrack.releasedate,
            shareurl = jamendoTrack.shorturl,
            shorturl = jamendoTrack.shorturl,
            waveform = jamendoTrack.waveform,
            fav_state = -1
        )
    }
}
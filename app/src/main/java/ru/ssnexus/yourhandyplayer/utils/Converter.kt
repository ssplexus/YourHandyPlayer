package ru.ssnexus.yourhandyplayer.utils

import ru.ssnexus.database_module.data.entity.Track
import ru.ssnexus.yourhandyplayer.di.modules.remote_module.entity.SoundCloudTrack

object Converter {
    fun convertApiListToDtoList(list: List<SoundCloudTrack>?): List<Track> {
        val result = mutableListOf<Track>()
        list?.forEach {
            result.add(convertApiToDto(it))
        }
        return result
    }

    fun convertApiToDto(soundCloudTrack: SoundCloudTrack): Track {
        return Track(
                    mTitle = soundCloudTrack.mTitle,
                    api_id = soundCloudTrack.api_id,
                    mStreamURL = soundCloudTrack.mStreamURL,
                    mArtworkURL = soundCloudTrack.mArtworkURL
        )
    }
}
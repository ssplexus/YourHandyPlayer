package ru.ssnexus.database_module.data.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.sql.Date


@Parcelize
@Entity(tableName = "tracks", indices = [Index(value = ["id"], unique = true)])
data class JamendoTrackData(
    //@PrimaryKey(autoGenerate = false) val mid: Int = 0,
    @PrimaryKey(autoGenerate = true) val mid: Int = 0,
    @ColumnInfo(name = "album_id") val album_id: Int,
    @ColumnInfo(name = "album_image") val album_image: String,
    @ColumnInfo(name = "album_name") val album_name: String,
    @ColumnInfo(name = "artist_id") val artist_id: Int,
    @ColumnInfo(name = "artist_idstr") val artist_idstr: String,
    @ColumnInfo(name = "artist_name") val artist_name: String,
    @ColumnInfo(name = "audio") val audio: String,
    @ColumnInfo(name = "audiodownload") val audiodownload: String,
    @ColumnInfo(name = "audiodownload_allowed") val audiodownload_allowed: Boolean,
    @ColumnInfo(name = "duration") val duration: Int,
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "image") val image: String,
    @ColumnInfo(name = "license_ccurl") val license_ccurl: String,
    @ColumnInfo(name = "acousticelectric") val acousticelectric: String,
    @ColumnInfo(name = "gender") val gender: String,
    @ColumnInfo(name = "lang") val lang: String,
    @ColumnInfo(name = "speed") val speed: String,

    @ColumnInfo(name = "genres") val genres: String,
    @ColumnInfo(name = "instruments") val instruments: String,
    @ColumnInfo(name = "vartags") val vartags: String,

    @ColumnInfo(name = "vocalinstrumental") val vocalinstrumental: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "position") val position: Int,
    @ColumnInfo(name = "prourl") val prourl: String,
    @ColumnInfo(name = "releasedate") val releasedate: String,
    @ColumnInfo(name = "shareurl") val shareurl: String,
    @ColumnInfo(name = "shorturl") val shorturl: String,
    @ColumnInfo(name = "waveform") val waveform: String,
    @ColumnInfo(name = "fav_state") var fav_state: Int = -1,
    @ColumnInfo(name = "watch_later_state") var watch_later_state: Int = -1

) : Parcelable


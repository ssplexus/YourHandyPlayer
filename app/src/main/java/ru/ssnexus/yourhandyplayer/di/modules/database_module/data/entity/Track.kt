package ru.ssnexus.database_module.data.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "tracks", indices = [Index(value = ["title"], unique = true)])
data class Track(
    @PrimaryKey(autoGenerate = false) val id: Int = 0,
    @ColumnInfo(name = "title") val mTitle: String,
    @ColumnInfo(name = "api_id") val api_id: Int,
    @ColumnInfo(name = "stream_url") val mStreamURL: String,
    @ColumnInfo(name = "artwork_url") val mArtworkURL: String) : Parcelable

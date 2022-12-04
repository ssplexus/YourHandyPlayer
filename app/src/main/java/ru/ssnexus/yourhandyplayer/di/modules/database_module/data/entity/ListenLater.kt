package ru.ssnexus.yourhandyplayer.di.modules.database_module.data.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "later", indices = [Index(value = ["mid"], unique = true)])
data class ListenLater(
    @PrimaryKey(autoGenerate = true) val mid: Int = 0,
    @ColumnInfo(name = "id") val id: Int,
) : Parcelable
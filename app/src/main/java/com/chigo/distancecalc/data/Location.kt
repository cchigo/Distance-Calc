package com.chigo.distancecalc.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "location_table")
@Parcelize
data class Location (

    @ColumnInfo(name = "longitude")
    var longitude: Double,

    @ColumnInfo(name = "latitude")
    var latitude: Double

): Parcelable
{
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
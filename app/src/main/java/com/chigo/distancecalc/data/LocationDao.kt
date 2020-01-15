package com.chigo.distancecalc.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface LocationDao {
    @Query("SELECT * FROM location_table")
    fun getLocationInfo(): LiveData<List<Location>>

    @Insert
    fun insert(location: Location)

    @Update
    fun update (location: Location)

    @Delete
    fun delete(location: Location)

    @Query("SELECT * FROM location_table")
    fun deleteAllLocations()


}
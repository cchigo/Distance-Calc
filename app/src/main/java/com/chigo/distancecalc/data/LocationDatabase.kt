package com.chigo.distancecalc.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Location::class], version = 1, exportSchema = false)
abstract class LocationDatabase: RoomDatabase() {

    abstract fun LocationDao(): LocationDao

    companion object{
        private var instance:LocationDatabase?=null

        fun getInstance(context: Context): LocationDatabase{
            if(instance==null){

                instance = Room.databaseBuilder(context, LocationDatabase::class.java, "location_database")
                    .allowMainThreadQueries()
                    .build()

                return instance!!
            }

            return instance!!
        }

    }

}
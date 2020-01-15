package com.chigo.distancecalc

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chigo.distancecalc.data.Location
import com.chigo.distancecalc.data.LocationDatabase

class LocationViewModel: ViewModel() {

    private val location: MutableLiveData<List<Location>> by lazy {
        MutableLiveData<List<Location>>().also{
            loadAllLocation()
        }
    }

    private fun loadAllLocation(){

    }

    fun getLocation(context:Context, location: Location): LiveData<List<Location>> {
        return LocationDatabase.getInstance(context).LocationDao().getLocationInfo()
    }

    fun setLocation(location: Location, context: Context){
        return LocationDatabase.getInstance(context).LocationDao().insert(location)
    }

    fun updateLocation(location: Location, context: Context){
        return LocationDatabase.getInstance(context).LocationDao().update(location)
    }



}
package com.chigo.distancecalc

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProviders
import com.chigo.distancecalc.data.Location
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.SphericalUtil
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), OnMapReadyCallback, View.OnClickListener {



    val PERMISSION_ID = 42
    private var googleMap: GoogleMap? = null
    private lateinit var locationViewModel: LocationViewModel



    lateinit var mMap: GoogleMap
    lateinit var googleApiClient: GoogleApiClient
    lateinit var mFusedLocationClient: FusedLocationProviderClient


    private var longitude:Double = 0.0
    private var latitude:Double = 0.0

    private var fromLongitude:Double = 0.0
    private var fromLatitude:Double = 0.0

    private var toLongitude:Double = 0.0
    private var toLatitude:Double = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //sets start and stop button visibility
        start.setOnClickListener{
            it.visibility = View.GONE
            stop.visibility = View.VISIBLE

        }

        stop.setOnClickListener {
            start.visibility = View.VISIBLE
            it.visibility = View.GONE

            distance_textView.setText("Distance covered is: ${calculateDistance()}")

        }
        //initialize fuzed location api
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()

        //reference viewmodel
        locationViewModel = ViewModelProviders.of(this).get(LocationViewModel::class.java)


        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }
    //onCreate Ends here

    //Getting current location
    private fun getCurrentLocation(): ArrayList<Double> { //Creating a location object
        val location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
        if (location != null) { //Getting longitude and latitude
//            longitude = location.longitude
//            latitude = location.latitude
            //moving the map to location

            val newLocation = Location(
                longitude = location.longitude,
                latitude = location.latitude
            )
            locationViewModel.getLocation(newLocation)
            Toast.makeText(this, "location found", Toast.LENGTH_LONG).show()
        }

        return arrayListOf(longitude, latitude)
    }
    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {

                mFusedLocationClient.lastLocation.addOnCompleteListener(this) {task ->
//                    var location: Location = task.result
                    var location = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        latitude_textView.text = location.latitude.toString()
                        longitude_textView.text = location.longitude.toString()
                    }
                }
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
           Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation = locationResult.lastLocation

            latitude_textView.text = mLastLocation.toString()
            longitude_textView.text = mLastLocation.toString()
        }
    }






    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }
    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_ID
        )
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Granted. Start getting the location information
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap!!

        val latLng = LatLng(-7.5, 4.00)
        mMap.addMarker(MarkerOptions().position(latLng).draggable(true))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
  }

//    override fun onMove(googleMap: GoogleMap?){
//        mFusedLocationClient.lastLocation.addOnCompleteListener(this) {task ->
//            //                    var location: Location = task.result
//            var location = task.result
//            if (location == null) {
//                requestNewLocationData()
//            } else {
//                latitude_textView.text = location.latitude.toString()
//                longitude_textView.text = location.longitude.toString()
//            }
//
//        }
//
//    }

    override fun onClick(v: View?) {
        if(v == start){
            fromLongitude = getCurrentLocation()[0]
            fromLatitude = getCurrentLocation()[1]


            Toast.makeText(this,"From set",Toast.LENGTH_SHORT).show()

        }
        if(v == stop){
            toLongitude = getCurrentLocation()[0]
            toLatitude = getCurrentLocation()[1]
            Toast.makeText(this,"To set",Toast.LENGTH_SHORT).show()
            calculateDistance()
        }

        if(v == buttonCalcDistance){
            //This method will show the distance

            calculateDistance()
        }

    }

    //calculates the distance between the start and stop cordinates
    private fun calculateDistance(): Double {
        //get both coordinates
        var from = LatLng(fromLatitude, fromLongitude)
        var to = LatLng(toLatitude, toLongitude)

        //calculate the distance
        var distance = SphericalUtil.computeDistanceBetween(from, to)

        //display the distance
       Toast.makeText(this, "$distance Meters",Toast.LENGTH_SHORT).show()

        return distance


    }


}

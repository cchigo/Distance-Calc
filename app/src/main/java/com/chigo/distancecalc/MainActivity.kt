package com.chigo.distancecalc

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProviders
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.chigo.distancecalc.data.Location
import com.chigo.distancecalc.data.LocationDatabase
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject


class MainActivity : AppCompatActivity(), OnMapReadyCallback{



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

        //initialize fuzed location api
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()

        //reference viewmodel
        locationViewModel = ViewModelProviders.of(this).get(LocationViewModel::class.java)


        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment


        //sets start and stop button visibility
        start.setOnClickListener{
            findViewById<Button>(R.id.start).visibility = View.GONE
            findViewById<Button>(R.id.stop).visibility = View.VISIBLE
            val newLocation = Location(
                fromLatitude,
                fromLongitude
            )
            //saving present location to room database
            LocationDatabase.getInstance(it.context).LocationDao().insert(newLocation)
            locationViewModel.setLocation(newLocation, this)
            Toast.makeText(this, "location found", Toast.LENGTH_LONG).show()
            Toast.makeText(this,"From set",Toast.LENGTH_SHORT).show()

        }
//
        stop.setOnClickListener {
           findViewById<Button>(R.id.start).visibility = View.VISIBLE
            findViewById<Button>(R.id.stop).visibility = View.GONE
            val newLocation = Location(
                toLatitude,
                toLongitude
            )
            //saving destination to romm database
            LocationDatabase.getInstance(it.context).LocationDao().insert(newLocation)
            locationViewModel.setLocation(newLocation, this)
            Toast.makeText(this, "location found", Toast.LENGTH_LONG).show()
            Toast.makeText(this,"To set",Toast.LENGTH_SHORT).show()
            calculateDistance()

        }
        buttonCalcDistance.setOnClickListener {
            calculateDistance()

        }


    }
    //onCreate Ends here

    //Getting current location
//    private fun getCurrentLocation(): ArrayList<Double> { //Creating a location object
//        val location = LocationRequest()
//        if (location != null) { //Getting longitude and latitude
////            longitude = location.longitude
////            latitude = location.latitude
//            //moving the map to location
//
//            val newLocation = Location(
//                longitude = location.longitude,
//                latitude = location.latitude
//            )
//            locationViewModel.setLocation(newLocation, this)
//            Toast.makeText(this, "location found", Toast.LENGTH_LONG).show()
//        }
//
//        return arrayListOf(longitude, latitude)
//    }


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


            latitude_textView.text = mLastLocation.latitude.toString()
            longitude_textView.text = mLastLocation.longitude.toString()

            fromLatitude = mLastLocation.latitude
            fromLongitude = mLastLocation.longitude
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

//        val latLng = LatLng(-7.5, 4.00)
//        mMap.addMarker(MarkerOptions().position(latLng).draggable(true))
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))

        val latLngOrigin = LatLng(10.3181466, 123.9029382)
        val latLngDestination = LatLng(10.311795,123.915864)


        val path: MutableList<List<LatLng>> = ArrayList()
        val urlDirections = "https://maps.googleapis.com/maps/api/directions/json?origin=10.3181466,123.9029382&destination=10.311795,123.915864&key=<AIzaSyBylAlQpXXhe1hhYfLyh9mEchn9loD5GkI>"
        val directionsRequest = object : StringRequest(Request.Method.GET, urlDirections, Response.Listener<String> {
                response ->
            val jsonResponse = JSONObject(response)
            // Get routes
            val routes = jsonResponse.getJSONArray("routes")
            val legs = routes.getJSONObject(0).getJSONArray("legs")
            val steps = legs.getJSONObject(0).getJSONArray("steps")
            for (i in 0 until steps.length()) {
                val points = steps.getJSONObject(i).getJSONObject("polyline").getString("points")
                path.add(PolyUtil.decode(points))
            }
            for (i in 0 until path.size) {
                this.googleMap!!.addPolyline(PolylineOptions().addAll(path[i]).color(Color.RED))
            }
        }, Response.ErrorListener {
                _ ->
        }){}
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(directionsRequest)
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

//    override fun onClick(v: View?) {
//        if(v == start){
//            findViewById<Button>(R.id.start).visibility = View.GONE
//            findViewById<Button>(R.id.stop).visibility = View.VISIBLE
//            fromLongitude = getCurrentLocation()[0]
//            fromLatitude = getCurrentLocation()[1]
//
//            Toast.makeText(this,"From set",Toast.LENGTH_SHORT).show()
//
//        }
//        if(v == stop){
//
//            findViewById<Button>(R.id.start).visibility = View.VISIBLE
//            findViewById<Button>(R.id.stop).visibility = View.GONE
//            toLongitude = getCurrentLocation()[0]
//            toLatitude = getCurrentLocation()[1]
//            Toast.makeText(this,"To set",Toast.LENGTH_SHORT).show()
//            calculateDistance()
//        }
//
//        if(v == buttonCalcDistance){
//            //This method will show the distance
//
//            calculateDistance()
//        }
//
//    }

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

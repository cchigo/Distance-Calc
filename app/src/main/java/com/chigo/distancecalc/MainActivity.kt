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
import androidx.lifecycle.Observer
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
    var googleMap: GoogleMap ? = null
    private lateinit var locationViewModel: LocationViewModel
    var startPosition: List<Location> = arrayListOf()



    //lateinit var mMap: GoogleMap
  //  lateinit var googleApiClient: GoogleApiClient
    lateinit var mFusedLocationClient: FusedLocationProviderClient


    private var longitude:Double = 0.0
    private var latitude:Double = 0.0

    private var fromLongitude:Double = 0.0
    private var fromLatitude:Double = 0.0

    private var toLongitude:Double = 0.0
    private var toLatitude:Double = 0.0
    lateinit var mapFragment: SupportMapFragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //initialize fuzed location api
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        //reference viewmodel
        locationViewModel = ViewModelProviders.of(this).get(LocationViewModel::class.java)


        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment


        //sets start and stop button visibility
        start.setOnClickListener{
            if (checkPermissions()) {
                if (isLocationEnabled()){

                    getLastLocation()
                    findViewById<Button>(R.id.start).visibility = View.GONE
                    findViewById<Button>(R.id.stop).visibility = View.VISIBLE
                    val newLocation = Location(
                        fromLatitude,
                        fromLongitude
                    )
                    //getting present location to room database
                    //LocationDatabase.getInstance(it.context).LocationDao().insert(newLocation)
                    locationViewModel.setLocation(newLocation, this)
                    Toast.makeText(this, "location found", Toast.LENGTH_LONG).show()
                    Toast.makeText(this,"From set",Toast.LENGTH_SHORT).show()

                    //saving present location
                    locationViewModel.getLocation(this).observe(this, Observer<List<Location>> { location ->

                        location?.let {
                            startPosition = location
                        }
                    })
                }else {
                    Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }
            }else {
                requestPermissions()
            }

        }
//
        stop.setOnClickListener {

            getLastLocation()
           findViewById<Button>(R.id.start).visibility = View.VISIBLE
            findViewById<Button>(R.id.stop).visibility = View.GONE
            val newLocation = Location(
                toLatitude,
                toLongitude
            )
            //saving destination to romm database
            //LocationDatabase.getInstance(it.context).LocationDao().insert(newLocation)
            locationViewModel.setLocation(newLocation, this)
            Toast.makeText(this, "location found", Toast.LENGTH_LONG).show()
            Toast.makeText(this,"To set",Toast.LENGTH_SHORT).show()
            //calculateDistance()

        }

        buttonCalcDistance.setOnClickListener {
           calculateDistance()
//            mapFragment.getMapAsync(this)
            getMap(startPosition[0].latitude, startPosition[0].longitude, fromLatitude, fromLongitude)


        }


    }
    //onCreate Ends here



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

    fun getMap(startLat:Double, startLong:Double, stopLat:Double, stopLong:Double){
        val path: MutableList<List<LatLng>> = ArrayList()
        val urlDirections = "https://maps.googleapis.com/maps/api/directions/json?origin=${startLat},${startLong}&destination=${stopLat},${stopLong}&key=AIzaSyBylAlQpXXhe1hhYfLyh9mEchn9loD5GkI"
        val directionsRequest = object : StringRequest(Request.Method.GET, urlDirections, Response.Listener<String> {
                response ->
            val jsonResponse = JSONObject(response)
            // Get routes
            val routes = jsonResponse.getJSONArray("routes")
            val legs = routes.getJSONObject(0).getJSONArray("legs")
            val steps = legs.getJSONObject(0).getJSONArray("steps")

            val distance = steps.getJSONObject(0).getJSONObject("distance").getString("text")

            Toast.makeText(this, "$distance Meters",Toast.LENGTH_SHORT).show()


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

    override fun onMapReady(googleMap: GoogleMap?) {
        this.googleMap = googleMap


        val latLngOrigin = LatLng(3.5245918, 6.4383454)
        val latLngDestination = LatLng(fromLatitude,fromLongitude)
        this.googleMap!!.addMarker(MarkerOptions().position(latLngOrigin).title("Start"))
        this.googleMap!!.addMarker(MarkerOptions().position(latLngDestination).title("Stop"))
        this.googleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOrigin, 14.5f))


        val path: MutableList<List<LatLng>> = ArrayList()
        val urlDirections = "https://maps.googleapis.com/maps/api/directions/json?origin=${startPosition[0].latitude},${startPosition[0].longitude}&destination=${fromLatitude},${fromLongitude}&key=AIzaSyBylAlQpXXhe1hhYfLyh9mEchn9loD5GkI"
        val directionsRequest = object : StringRequest(Request.Method.GET, urlDirections, Response.Listener<String> {
                response ->
            val jsonResponse = JSONObject(response)
            // Get routes
            val routes = jsonResponse.getJSONArray("routes")
            val legs = routes.getJSONObject(0).getJSONArray("legs")
            val steps = legs.getJSONObject(0).getJSONArray("steps")

            val distance = steps.getJSONObject(0).getJSONObject("distance").getString("text")

            Toast.makeText(this, "$distance Meters",Toast.LENGTH_SHORT).show()


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


    //calculates the distance between the start and stop coordinates
    private fun calculateDistance(): Double {
        //get both coordinates
        var from = LatLng(fromLatitude, fromLongitude)
        var to = LatLng(toLatitude, toLongitude)

        //calculate the distance
        var distance = SphericalUtil.computeDistanceBetween(from, to)

        //display the distance
       Toast.makeText(this, "$distance Meters",Toast.LENGTH_SHORT).show()

        return distance

        mapFragment.getMapAsync(this)
    }







}

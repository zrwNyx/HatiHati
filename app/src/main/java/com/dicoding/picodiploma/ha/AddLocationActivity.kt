package com.dicoding.picodiploma.ha

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import com.dicoding.picodiploma.ha.databinding.ActivityAddLocationBinding
import com.dicoding.picodiploma.ha.databinding.ActivityFirebaseMltestBinding
import com.dicoding.picodiploma.ha.test.MapsActivity2
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import java.util.*

class AddLocationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityAddLocationBinding
    private lateinit var mMap: GoogleMap
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var placesClient: PlacesClient
    private val apiKey ="AIzaSyA0L5HH6pok52kuy9NNJZVGM0vKb7VG0Zg"
    private lateinit var lastLocation: Location
    private lateinit var move: LatLng
    var lat : Double = 0.0
    var lon : Double = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(applicationContext)

        if(!Places.isInitialized()){
            Places.initialize(applicationContext,apiKey)
        }

        placesClient = Places.createClient(this)
        val autoCompleteFragment = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment?

        autoCompleteFragment?.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME,Place.Field.LAT_LNG))
        autoCompleteFragment?.view?.setBackgroundColor(Color.WHITE)
        autoCompleteFragment?.setHint("Cari Tempat")
        autoCompleteFragment?.setOnPlaceSelectedListener(object  : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                Log.i("Searched Place", "Place: ${place.latLng?.latitude.toString()}  ${place.latLng?.longitude.toString()}")
                move = place.latLng
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(move,15.0f))

            }

            override fun onError(p0: Status) {
                Log.i("Error", "An error occured : + ${p0.status}")
            }
        })

        binding.myloc.setOnClickListener{
            setUpMap()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setUpMap()
        mMap.uiSettings.isMyLocationButtonEnabled = false

        /*mMap.setOnMapClickListener(GoogleMap.OnMapClickListener {
            val intent = Intent(applicationContext, AddReportActivity::class.java)
            intent.putExtra("LAT", it.latitude)
            intent.putExtra("LON", it.longitude)
            startActivity(intent)
            finish()
        })*/

        mMap.setOnCameraIdleListener {
            lat = mMap.cameraPosition.target.latitude
            lon = mMap.cameraPosition.target.longitude
        }

        binding.add.setOnClickListener{
            val intent = Intent(applicationContext, AddReportActivity::class.java)
            intent.putExtra("LAT", lat)
            intent.putExtra("LON", lon)
            startActivity(intent)
            finish()
        }

    }


    private fun setUpMap(){
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
              LOCATION_REQUEST_CODE
            )
            return
        }
        mMap.isMyLocationEnabled = true
        fusedLocationProviderClient.lastLocation.addOnSuccessListener(this) {
            if(it != null){
                lastLocation = it
                val currentLatLong = LatLng(it.latitude, it.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong,15f))
            }
        }
    }

    companion object{
        private const val LOCATION_REQUEST_CODE = 1
    }
}
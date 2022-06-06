package com.dicoding.picodiploma.ha.test

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.dicoding.picodiploma.ha.Model.Report
import com.dicoding.picodiploma.ha.R
import com.dicoding.picodiploma.ha.databinding.ActivityMaps2Binding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class MapsActivity2 : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMaps2Binding
    private lateinit var reportArrayList : ArrayList<Report>
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private val apiKey ="AIzaSyA0L5HH6pok52kuy9NNJZVGM0vKb7VG0Zg"
    private lateinit var map: GoogleMap
    private lateinit var fkip: LatLng
    private lateinit var monas: LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMaps2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(applicationContext)
        fkip = LatLng(-6.3037978, 106.8693713)
        monas = LatLng(-6.1890511, 106.8251573)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true

        reportArrayList = arrayListOf<Report>()
        setUpMap()

        val markerFkip = MarkerOptions()
            .position(fkip)
            .title("FKIP")
        val markerMonas = MarkerOptions()
            .position(monas)
            .title("Monas")

        mMap.addMarker(markerFkip)
        mMap.addMarker(markerMonas)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(monas, 11.6f))

        val fromFKIP = fkip.latitude.toString() + "," + fkip.longitude.toString()
        val toMonas = monas.latitude.toString() + "," + monas.longitude.toString()

        val apiServices = RetrofitClient.apiServices(this)
        apiServices.getDirection(fromFKIP, toMonas, apiKey)
            .enqueue(object : Callback<DirectionResponses> {
                override fun onResponse(call: Call<DirectionResponses>, response: Response<DirectionResponses>) {
                    drawPolyline(response)
                    Log.i("status", "bisa ngab")
                }

                override fun onFailure(call: Call<DirectionResponses>, t: Throwable) {
                    Log.i("status", "g bisa gimana dong")
                }
            })



    }

    private fun drawPolyline(response: Response<DirectionResponses>) {
        val shape = response.body()?.routes?.get(0)?.overviewPolyline?.points
        val polyline = PolylineOptions()
            .addAll(PolyUtil.decode(shape))
            .width(8f)
            .color(Color.RED)
        mMap.addPolyline(polyline)
    }


    private interface ApiServices {
        @GET("maps/api/directions/json")
        fun getDirection(@Query("origin") origin: String,
                         @Query("destination") destination: String,
                         @Query("key") apiKey: String): Call<DirectionResponses>
    }

    private object RetrofitClient {
        fun apiServices(context: Context): ApiServices {
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://maps.googleapis.com")
                .build()

            return retrofit.create<ApiServices>(ApiServices::class.java)
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
                LOCATION_REQUEST_CODE)
            return
        }
        mMap.isMyLocationEnabled = true
        fusedLocationProviderClient.lastLocation.addOnSuccessListener(this) {
            if(it != null){
                lastLocation = it
                val currentLatLong = LatLng(it.latitude, it.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong,12f))
            }
        }
    }
    companion object{
        private const val LOCATION_REQUEST_CODE = 1
    }
}
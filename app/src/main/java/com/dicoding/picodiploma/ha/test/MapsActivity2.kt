package com.dicoding.picodiploma.ha.test

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.dicoding.picodiploma.ha.Model.MyItem
import com.dicoding.picodiploma.ha.Model.Report
import com.dicoding.picodiploma.ha.R
import com.dicoding.picodiploma.ha.databinding.ActivityMaps2Binding
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
import com.google.firebase.ml.modeldownloader.DownloadType
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader
import com.google.maps.android.clustering.ClusterManager
import org.tensorflow.lite.Interpreter
import java.io.IOException
import java.lang.StringBuilder
import java.math.RoundingMode
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList

class MapsActivity2 : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMaps2Binding
    private lateinit var database : DatabaseReference
    private lateinit var reportArrayList : ArrayList<Report>
    private lateinit var clusterManager : ClusterManager<MyItem>
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var placesClient: PlacesClient
    private val apiKey ="AIzaSyA0L5HH6pok52kuy9NNJZVGM0vKb7VG0Zg"
    var move = LatLng(0.0,0.0)
    private lateinit var geocoder: Geocoder
    private lateinit var interpreter: Interpreter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMaps2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(applicationContext)
        val conditions = CustomModelDownloadConditions.Builder().build()
        FirebaseModelDownloader.getInstance().getModel("Prediction", DownloadType.LOCAL_MODEL,conditions)
            .addOnSuccessListener {
                val modelFile = it.file
                if(modelFile != null){
                    Toast.makeText(applicationContext,"Model Downloaded", Toast.LENGTH_SHORT).show()
                    interpreter = Interpreter(modelFile)
                }
            }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true

        database = FirebaseDatabase.getInstance("https://hatihati-22fa9-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Report")
        reportArrayList = arrayListOf<Report>()
        setUpMap()


        mMap.setOnMapClickListener{
            val b =getLocationName(it.latitude, it.longitude)
            val jam = Calendar.getInstance().time.hours
            val c = getPrediction(jam.toFloat(), it.longitude.toFloat(),it.latitude.toFloat())
            Toast.makeText(applicationContext, c,Toast.LENGTH_SHORT).show()
        }



    }


    private fun getPrediction(Jam : Float , Lon: Float, Lat: Float) : String{

        var predict = ""
        val input = ByteBuffer.allocateDirect(3*4).order(ByteOrder.nativeOrder())
        input.putFloat(Jam)
        input.putFloat(Lon)
        input.putFloat(Lat)

        val bufferSize = 4 * java.lang.Float.SIZE / java.lang.Byte.SIZE
        val modelOutput = ByteBuffer.allocate(bufferSize).order(ByteOrder.nativeOrder())
        val actualOutput = modelOutput.asFloatBuffer()

        interpreter.run(input,modelOutput)
        modelOutput.rewind()
        val b = actualOutput.get(1)*100
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.DOWN
        predict = "${df.format(b)} %"

        return predict

    }

    private fun setUpClusterer(){
        clusterManager = ClusterManager(applicationContext,mMap)
        clusterManager.renderer = MarkerClusterRenderer(applicationContext,mMap,clusterManager,R.drawable.sexoffensemark)
        mMap.setOnCameraIdleListener(clusterManager)
        addItems()
    }
    private fun addItems(){
        for(i in 0 until reportArrayList.size) {
            val reportMark = reportArrayList.get(i)
            val item = MyItem(reportMark.Y , reportMark.X,reportMark.Category,reportMark.Dates)
            clusterManager.addItem(item)

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

    private fun getLocationName(lat : Double, lng : Double) : String{
        lateinit var lokasi : String
        geocoder = Geocoder(applicationContext, Locale.getDefault())
        try {
            val addressList : List<Address> = geocoder.getFromLocation(lat,lng,1)
            if(addressList != null && addressList.isNotEmpty()){
                val address = addressList.get(0)
                val sb = StringBuilder()

                for(i in 0 until address.maxAddressLineIndex){
                    sb.append(address.getAddressLine(i)).append("\n")
                }
                if(address.premises != null){
                    sb.append(address.premises).append(", ")
                }
                sb.append(address.subAdminArea).append("\n")
                sb.append(address.locality).append(", ")
                lokasi = sb.toString()

            }
        }catch (e: IOException) {
            Toast.makeText(applicationContext,"Unable connect to Geocoder",Toast.LENGTH_LONG).show()
        }

        return lokasi

    }
}
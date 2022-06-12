package com.dicoding.picodiploma.ha

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dicoding.picodiploma.ha.Auth.SignIn
import com.dicoding.picodiploma.ha.ChatBot.ChatActivity
import com.dicoding.picodiploma.ha.Model.Report
import com.dicoding.picodiploma.ha.databinding.ActivityMapsBinding
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
import com.google.firebase.ml.modeldownloader.DownloadType
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader
import com.google.maps.android.SphericalUtil
import org.tensorflow.lite.Interpreter
import java.io.IOException
import java.lang.StringBuilder
import java.math.RoundingMode
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.collections.ArrayList

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var database : Query
    private lateinit var reportArrayList : ArrayList<Report>
    private lateinit var firebaseAuth : FirebaseAuth
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var placesClient: PlacesClient
    private lateinit var geocoder : Geocoder
    private var check = 0
    private lateinit var interpreter: Interpreter
    private val apiKey ="AIzaSyBF-XzsPttrItObpD3l3EzNEzKTjw4hkLw"
    private val CHANNEL_ID  = "channel_id_example_01"
    private val notificationId =101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(applicationContext)
        firebaseAuth = FirebaseAuth.getInstance()


        if(!Places.isInitialized()){
            Places.initialize(applicationContext,apiKey)
        }

        placesClient = Places.createClient(this)
        val autoCompleteFragment = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment?

        autoCompleteFragment?.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
        autoCompleteFragment?.view?.setBackgroundColor(Color.WHITE)
        autoCompleteFragment?.setHint("Cari Tempat")
        autoCompleteFragment?.setOnPlaceSelectedListener(object  : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                Log.i("Searched Place", "Place: ${place.latLng?.latitude.toString()}  ${place.latLng?.longitude.toString()}")
                val move = place.latLng
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(move!!,15.0f))

            }

            override fun onError(p0: Status) {
                Log.i("Error", "An error occured :  ${p0.status}")
            }
        })


       binding.add.setOnClickListener{
            val user = firebaseAuth.currentUser
            if(user!!.isEmailVerified) {
                val test = Intent(applicationContext, ChatActivity::class.java)
                startActivity(test)
            }else{
                Toast.makeText(applicationContext,"Mohon Verifikasi Email Terlebih Dahulu dan Sign in Ulang", Toast.LENGTH_SHORT).show()
            }
        }




    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Hati-Hati"


        val conditions = CustomModelDownloadConditions.Builder().build()
        FirebaseModelDownloader.getInstance().getModel("Prediction", DownloadType.LOCAL_MODEL,conditions)
            .addOnSuccessListener {
                val modelFile = it.file
                if(modelFile != null){
                    interpreter = Interpreter(modelFile)
                    setUpCurrentLocation()
                }
            }





        database = FirebaseDatabase.getInstance("https://hatihati-22fa9-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("Report").limitToLast(400)
        reportArrayList = arrayListOf<Report>()

        database.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(i in snapshot.children){
                    val report = i.getValue(Report::class.java)
                    reportArrayList.add(report!!)

                }
              markerClicked()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })


    }





   override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater =menuInflater
        inflater.inflate(R.menu.navigation_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.logout->{
                firebaseAuth.signOut()
                val logIntent = Intent(this, SignIn::class.java)
                logIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                this.startActivity(logIntent)

            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
        return true
    }



    //buat munculin marker nya pas diklik ngab
    @Suppress("DEPRECATION")
    @SuppressLint("SetTextI18n")
    private fun markerClicked(){
        mMap.setOnMapClickListener {
            mMap.clear()
            check = 0
            val circPos = LatLng(it.latitude, it.longitude)
            val Lokasi = getLocationName(it.latitude,it.longitude)
            val currentTime = "${Calendar.getInstance().time.hours}:${Calendar.getInstance().time.minutes}"
            val jam = Calendar.getInstance().time.hours
            val prediksi = getPrediction(jam.toFloat(), it.longitude.toFloat(), it.latitude.toFloat())
            createCircle(it.latitude,it.longitude, prediksi)
            showFilteredMarker(circPos)
            binding.rectText.text = "\n" +
                    "     Lokasi : $Lokasi   \n" +
                    "     Jam : $currentTime  \n" +
                    "     Kemungkinan Kriminalitas : $prediksi % \n"
            if(check == 0){
                Toast.makeText(applicationContext,"Tidak ada Data di Area Sekitar",Toast.LENGTH_SHORT).show()
            }
        }

    }


    //ngefilter marker yang ada diradius doang
    private fun showFilteredMarker(circPos : LatLng){
        var x : Int
        for(i in 0 until reportArrayList.size){
            val reportMark = reportArrayList.get(i)
            val location = LatLng(reportMark.Y,reportMark.X)
            val description = reportMark.Category
            val date = reportMark.Dates
            when{
                description == "ASSAULT" -> x = R.drawable.assaultmark
                description == "VEHICLE THEFT" -> x = R.drawable.vectheft
                else -> x = R.drawable.sexoffensemark
            }

            if(SphericalUtil.computeDistanceBetween(circPos, location) < 1000){
                mMap.addMarker(MarkerOptions().title(description).position(location).snippet(date).icon(BitmapDescriptorFactory.fromResource(x)))
                check += 1
            }
        }
    }

    //buat bikin circle kalo di klik
    @Suppress("DEPRECATION")
    private fun createCircle(x : Double, y :Double, Prediksi : Float) {
        val circPos = LatLng(x,y)
        val red : Int
        val green : Int
        val blue : Int
        mMap.addMarker(MarkerOptions().position(circPos).icon(BitmapDescriptorFromVector(applicationContext,R.drawable.ic_baseline_place_24)))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(circPos,15.0f))

       when{
            Prediksi < 10 ->{
                red = 22;green =84;blue =0
            }
            Prediksi > 10 && Prediksi < 20 ->{
                red =38;green = 147;blue = 0
            }
            Prediksi > 20 && Prediksi < 30 ->{
                red =94;green = 195;blue = 58
            }
            Prediksi > 30 && Prediksi < 40 ->{
                red =163;green = 197;blue = 46

            }
            Prediksi > 40 && Prediksi < 50 ->{
                red =214;green = 207;blue = 13
            }
            Prediksi > 50 && Prediksi < 60 ->{
                red =196;green = 167;blue = 46
            }
            Prediksi > 60 && Prediksi < 70 ->{
                red =196;green = 134;blue = 14
            }
            Prediksi > 70 && Prediksi < 80 ->{
                red =178;green = 100;blue = 7
            }
            Prediksi > 80 && Prediksi < 90 ->{
                red =178;green = 35;blue = 7
            }
            Prediksi > 90 && Prediksi < 100 ->{
                red =148;green = 39;blue = 6
            }else->{
                red =0;green =0;blue =0
            }
        }




        val circle =CircleOptions().center(circPos).radius(1000.0).strokeColor(Color.TRANSPARENT).fillColor(Color.argb(40,red,green,blue))
        mMap.addCircle(circle)
    }



    //pas awal awal munculin lokasi lo skrg
    private fun setUpCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_CODE
            )
            return
        }
        mMap.isMyLocationEnabled = true
        fusedLocationProviderClient.lastLocation.addOnSuccessListener(this) {
            if(it != null){
                lastLocation = it
                val currentLatLong = LatLng(it.latitude, it.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong,15.0f))
                createNotification(getLocationName(it.latitude, it.longitude),getPrediction(Calendar.getInstance().time.hours.toFloat(),it.longitude.toFloat(),it.latitude.toFloat()))
            }
        }
    }

    // dapetin nama lokasi
    private fun getLocationName(lat : Double, lng : Double) : String{
        var lokasi = "null"
        geocoder = Geocoder(applicationContext, Locale.getDefault())
        try {
            val addressList : List<Address> = geocoder.getFromLocation(lat,lng,1)
            if(addressList.isNotEmpty()){
                val address = addressList.get(0)
                val sb = StringBuilder()

                for(i in 0 until address.maxAddressLineIndex){
                    sb.append(address.getAddressLine(i)).append("\n")
                }
                if(address.premises != null){
                    sb.append(address.premises).append(", ")
                }
                sb.append(address.subLocality).append(",")
                sb.append(address.subAdminArea).append("")
                lokasi = sb.toString()

            }
        }catch (e: IOException) {
            Toast.makeText(applicationContext,"Unable connect to Geocoder",Toast.LENGTH_LONG).show()
        }

        return lokasi

    }

    private fun BitmapDescriptorFromVector(context : Context, vectorResId : Int) : BitmapDescriptor{
        val vectorDrawable = ContextCompat.getDrawable(context,vectorResId)
        vectorDrawable?.setBounds(0,0,vectorDrawable.intrinsicWidth,vectorDrawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(vectorDrawable!!.intrinsicWidth,vectorDrawable.intrinsicHeight,Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun getPrediction(Jam : Float , Lon: Float, Lat: Float) : Float{

        val predict: Float
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
        val df = DecimalFormat("#,##", DecimalFormatSymbols.getInstance(Locale.US))
        df.roundingMode = RoundingMode.DOWN
        predict = df.format(b).toFloat()

        return predict

    }

    private fun createNotification(Lokasi : String, Prediksi : Float){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name = "Notification Title"
            val descriptionText = "Notification Description"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID,name,importance).apply {
                description = descriptionText
            }
            val notificationManager : NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

        }
        val intent = Intent(this,MapsActivity::class.java)
        val pendingIntent : PendingIntent = PendingIntent.getActivity(this,0, intent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT )
        val dialIntent = Intent(Intent.ACTION_DIAL)
        dialIntent.setData(Uri.parse("tel:110"))
        val actionIntent = PendingIntent.getActivity(this, 0 , dialIntent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT )


        val builder = NotificationCompat
            .Builder(this, CHANNEL_ID)
            .setContentTitle("Anda Sedang Berada di $Lokasi")
            .setContentText("Kemungkinan Kriminalitas = $Prediksi %")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setSmallIcon(R.drawable.log)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_baseline_arrow_back_24,"Panic Button",actionIntent)


        val notification : Notification =  builder.build()
        notification.flags = Notification.FLAG_ONGOING_EVENT
        with(NotificationManagerCompat.from(this)){
            notify(notificationId, notification)
        }
    }

    companion object{
        private const val REQUEST_LOCATION_CODE = 1
    }

}

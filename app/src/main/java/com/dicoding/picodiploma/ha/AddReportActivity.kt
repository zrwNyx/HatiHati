package com.dicoding.picodiploma.ha

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.dicoding.picodiploma.ha.Model.Report
import com.dicoding.picodiploma.ha.databinding.ActivityAddReportBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.IOException
import java.lang.StringBuilder
import java.util.*

class AddReportActivity : AppCompatActivity() {
    private lateinit var binding : ActivityAddReportBinding
    private lateinit var geocoder : Geocoder
    var Deskripsi = "null"
    var checkCounter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        binding.otherEditText.visibility = View.INVISIBLE
        buttonCheck()

        val inLatitude = intent.getDoubleExtra("LAT",0.0)
        val inLongitude = intent.getDoubleExtra("LON",0.0)

        if(inLatitude != 0.0 && inLongitude != 0.0){
            binding.actualLat.text = inLatitude.toString()
            binding.actualLon.text = inLongitude.toString()
        }
        val b = getLocationName(inLatitude,inLongitude)
        binding.actualLoc.text = b

    }

    @Suppress("DEPRECATION")
    private fun buttonCheck(){
        binding.btnAssault.setOnClickListener{
            binding.btnAssault.setBackgroundColor(resources.getColor(R.color.cyan))
            binding.btnVectheft.setBackgroundColor(Color.TRANSPARENT)
            binding.btnSexoffense.setBackgroundColor(Color.TRANSPARENT)
            binding.btnOther.setBackgroundColor(Color.TRANSPARENT)
            binding.otherEditText.visibility = View.INVISIBLE
            Deskripsi = "ASSAULT"
            checkCounter =0
        }
        binding.btnVectheft.setOnClickListener{
            binding.btnVectheft.setBackgroundColor(resources.getColor(R.color.cyan))
            binding.btnAssault.setBackgroundColor(Color.TRANSPARENT)
            binding.btnSexoffense.setBackgroundColor(Color.TRANSPARENT)
            binding.btnOther.setBackgroundColor(Color.TRANSPARENT)
            binding.otherEditText.visibility = View.INVISIBLE
            Deskripsi = "VEHICLE THEFT"
            checkCounter =0
        }
        binding.btnSexoffense.setOnClickListener{
            binding.btnSexoffense.setBackgroundColor(resources.getColor(R.color.cyan))
            binding.btnVectheft.setBackgroundColor(Color.TRANSPARENT)
            binding.btnAssault.setBackgroundColor(Color.TRANSPARENT)
            binding.btnOther.setBackgroundColor(Color.TRANSPARENT)
            binding.otherEditText.visibility = View.INVISIBLE
            Deskripsi = "SEX OFFENSES FORCIBLE"
            checkCounter =0
        }
        binding.btnOther.setOnClickListener{
            binding.btnOther.setBackgroundColor(resources.getColor(R.color.cyan))
            binding.btnVectheft.setBackgroundColor(Color.TRANSPARENT)
            binding.btnAssault.setBackgroundColor(Color.TRANSPARENT)
            binding.btnSexoffense.setBackgroundColor(Color.TRANSPARENT)
            binding.otherEditText.visibility = View.VISIBLE
            checkCounter = 1
        }
        binding.addLocation.setOnClickListener{
            val intent = Intent(applicationContext, AddLocationActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.addReport.setOnClickListener{
            val inLatitude = intent.getDoubleExtra("LAT",0.0)
            val inLongitude = intent.getDoubleExtra("LON",0.0)
           if(checkCounter == 1){
                Deskripsi = binding.otherEditText.text.toString()
            }
            if(Deskripsi == "null" || Deskripsi == "" ||binding.actualLat.text.isEmpty()|| binding.actualLon.text.isEmpty() ){
                Toast.makeText(applicationContext, "Lengkapi Data", Toast.LENGTH_SHORT).show()
            }
            else{
                val Category = Deskripsi
                val Dates = Calendar.getInstance().time.toString()
                val Hours = Calendar.getInstance().time.hours
                val Label = 1
                val X = inLongitude
                val Y = inLatitude
                val Report = Report(Category, Dates, Hours,Label,X,Y)
                val ref : DatabaseReference = FirebaseDatabase.getInstance("https://hatihati-22fa9-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Report")
                ref.push().setValue(Report).addOnCompleteListener {
                    if(it.isSuccessful){
                        Toast.makeText(applicationContext, "Berhasil Menambah Laporan", Toast.LENGTH_SHORT).show()
                        val intent = Intent(applicationContext, MapsActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }else{
                        Toast.makeText(applicationContext, "Gagal Menambah Laporan", Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }
    }

    private fun getLocationName(lat : Double, lng : Double) : String{
        var lokasi = ""
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
                sb.append(address.subAdminArea).append(",")
                sb.append(address.locality).append("")
                lokasi = sb.toString()

            }
        }catch (e: IOException) {
            Toast.makeText(applicationContext,"Unable connect to Geocoder",Toast.LENGTH_LONG).show()
        }

        return lokasi

    }
}
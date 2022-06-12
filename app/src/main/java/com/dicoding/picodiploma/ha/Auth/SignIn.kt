package com.dicoding.picodiploma.ha.Auth

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.dicoding.picodiploma.ha.MapsActivity
import com.dicoding.picodiploma.ha.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth

class SignIn : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        firebaseAuth = FirebaseAuth.getInstance()
        askForPerms()

        binding.btnSignup.setOnClickListener {
            val startSignUp = Intent(this, SignUp::class.java)
            startActivity(startSignUp)
        }

        binding.btnSignin.setOnClickListener {
            val email = binding.emailTxt.text.toString()
            val password = binding.passTxt.text.toString()

            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                if (it.isSuccessful) {
                    val startMap = Intent(this, MapsActivity::class.java)
                    startActivity(startMap)
                    Toast.makeText(applicationContext, "User Berhasil Login", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(applicationContext, "User Tidak Terdaftar", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val startMap = Intent(this, MapsActivity::class.java)
            startActivity(startMap)
            finish()

        }
    }

    private fun askForPerms() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_CODE
            )
            return
        }
    }

    companion object {
        private const val REQUEST_LOCATION_CODE = 1
    }
}
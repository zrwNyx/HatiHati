package com.dicoding.picodiploma.ha.Auth

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.dicoding.picodiploma.ha.MapsActivity
import com.dicoding.picodiploma.ha.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth

class SignIn : AppCompatActivity() {

    private lateinit var binding : ActivitySignInBinding
    private lateinit var firebaseAuth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.btnSignup.setOnClickListener{
            val startSignUp = Intent(this, SignUp::class.java)
            startActivity(startSignUp)
        }

        firebaseAuth = FirebaseAuth.getInstance()
        binding.btnSignin.setOnClickListener{
            val email = binding.emailTxt.text.toString()
            val password = binding.passTxt.text.toString()

            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener{
                if(it.isSuccessful){
                    val startMap = Intent(this, MapsActivity::class.java)
                    startActivity(startMap)
                    Toast.makeText(applicationContext, "User Berhasil Login", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(applicationContext,"User Tidak Terdaftar", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if(currentUser != null){
            val startMap = Intent(this, MapsActivity::class.java)
            startActivity(startMap)
            finish()

        }
    }
}
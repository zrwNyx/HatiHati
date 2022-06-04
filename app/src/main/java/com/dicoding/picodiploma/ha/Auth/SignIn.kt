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
    lateinit var sharedPreferences : SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        sharedPreferences = getSharedPreferences("LOGIN", Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = sharedPreferences.edit()
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
                    editor.putString("Email", email)
                    editor.putString("Password", password)
                    editor.apply()
                }else{
                    Toast.makeText(applicationContext,"User Tidak Terdaftar", Toast.LENGTH_SHORT).show()
                }
            }
        }
        val check = sharedPreferences.getString("Email", null)
        val checkPass = sharedPreferences.getString("Password", null)
         if(check == null && checkPass == null) {
             Toast.makeText(applicationContext, "Silahkan Login", Toast.LENGTH_LONG).show()
         }else{
           //firebaseAuth.signInWithEmailAndPassword(check, checkPass)
            val startMap = Intent(this, MapsActivity::class.java)
            startActivity(startMap)
            finish()
        }
    }
}
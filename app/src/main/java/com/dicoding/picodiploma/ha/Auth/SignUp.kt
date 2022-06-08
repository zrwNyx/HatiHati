package com.dicoding.picodiploma.ha.Auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.dicoding.picodiploma.ha.Model.User
import com.dicoding.picodiploma.ha.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class SignUp : AppCompatActivity() {

    private lateinit var binding : ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        firebaseAuth = FirebaseAuth.getInstance()
        if(binding.passTxt.text!!.length < 6 ){
            binding.passText.error = "Password harus memiliki 6 karakter"
        }


        binding.btnSignup.setOnClickListener{
            val name = binding.nameTxt.text.toString()
            val email = binding.emailTxt.text.toString().trim()
            val password = binding.passTxt.text.toString().trim()
            val User = User(name,email,password)

            if(password.length >= 6 && name.isNotEmpty() && email.isNotEmpty()) {
                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val user = firebaseAuth.currentUser
                        val ref: DatabaseReference = FirebaseDatabase.getInstance("https://hatihati-22fa9-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users")
                        ref.child(user!!.uid).setValue(User)


                        user.sendEmailVerification()
                            .addOnSuccessListener{
                                firebaseAuth.signOut()
                                Toast.makeText(applicationContext, "Email Verifikasi telah dikirim",Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, SignIn::class.java)
                                startActivity(intent)
                            }
                            .addOnFailureListener{
                                Toast.makeText(applicationContext, "${it.message}",Toast.LENGTH_SHORT).show()
                            }

                    } else {
                        Toast.makeText(applicationContext, it.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }else{
                Toast.makeText(applicationContext,"Lengkapi Data" , Toast.LENGTH_SHORT).show()
            }

        }

    }
}
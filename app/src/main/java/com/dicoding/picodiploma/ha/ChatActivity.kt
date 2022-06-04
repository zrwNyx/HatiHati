package com.dicoding.picodiploma.ha

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.picodiploma.ha.databinding.ActivityChatBinding
import com.dicoding.picodiploma.ha.databinding.ActivitySignInBinding

class ChatActivity:AppCompatActivity() {

    private lateinit var binding : ActivityChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
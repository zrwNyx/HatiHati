package com.dicoding.picodiploma.ha.test

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.dicoding.picodiploma.ha.R
import com.dicoding.picodiploma.ha.databinding.ActivityFirebaseMltestBinding
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
import com.google.firebase.ml.modeldownloader.DownloadType
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.ByteChannel

class FirebaseMLTestActivity : AppCompatActivity() {

    private lateinit var  binding : ActivityFirebaseMltestBinding
    private lateinit var interpreter: Interpreter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFirebaseMltestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val conditions = CustomModelDownloadConditions.Builder().build()

        FirebaseModelDownloader.getInstance().getModel("CrimePred", DownloadType.LOCAL_MODEL,conditions)
            .addOnSuccessListener {
                Toast.makeText(applicationContext,"Model Downloaded", Toast.LENGTH_SHORT).show()
                val modelFile = it.file
                if(modelFile != null){
                    interpreter = Interpreter(modelFile)
                }
            }

        binding.button.setOnClickListener{
            val input = ByteBuffer.allocateDirect(12).order(ByteOrder.nativeOrder())
            input.putInt(2)
            input.putFloat(106.815f)
            input.putFloat(-6.16388f)
            val bufferSize = 4 * java.lang.Float.SIZE / java.lang.Byte.SIZE
            val modelOutput = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder())
            interpreter.run(input,modelOutput)

            val prediction = modelOutput.asFloatBuffer()

            binding.pred.text = prediction.get(1).toString()
        }
    }
}
package com.example.dreamer.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.lifecycleScope
import com.example.dreamer.R
import com.google.ai.client.generativeai.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.Calendar

class DreamResponseActivity : AppCompatActivity() {
    private val client = OkHttpClient()
    private lateinit var responseTextView: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var progressBar: ProgressBar
    val apiKey1="AIzaSyDTyvFXY0ZT_8_g88WmOsX_BLnSVJizbgU"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dream_response)
        responseTextView = findViewById(R.id.response_text_view)
        progressBar = findViewById(R.id.progress_bar)
        supportActionBar?.apply {
            // Enable the back button
            setDisplayHomeAsUpEnabled(true)
            // Set the title
            title = "Dreamer"
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Get dream content and emotion from intent
        val dreamContent = intent.getStringExtra("dreamContent") ?: ""
        val dreamEmotion = intent.getStringExtra("emotion") ?: ""
        val first="I am giving DreamContent->"

        val generativeModel = GenerativeModel(
            // The Gemini 1.5 models are versatile and work with both text-only and multimodal prompts
            modelName = "gemini-1.5-flash",
            // Access your API key as a Build Configuration variable (see "Set up your API key" above)
            apiKey = apiKey1
        )
        val ans="I am providing 1)-Dream Content-> "+dreamContent+" 2)-Emotion-> "+dreamContent+" now give me the possible meaning of this in 2 lines only direct write 2 lines about it"
        print(ans)
        lifecycleScope.launch { // Launch a coroutine using lifecycleScope
            val prompt =ans
            val response = generativeModel.generateContent(prompt)
            runOnUiThread {
                responseTextView.text = response.text

                progressBar.visibility = View.GONE
            }
            submitDreamLog(dreamContent, dreamEmotion,response.text.toString())


        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar back button click
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun submitDreamLog(dreamContent: String, emotion: String,response: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val dreamData = hashMapOf(
                "dreamContent" to dreamContent,
                "emotion" to emotion,
                "prompt" to response,
                "timestamp" to Calendar.getInstance().time.toString()
            )

            val databaseReference = database.reference
            databaseReference.child("users").child(userId).child("dreams").push().setValue(dreamData)
                .addOnFailureListener { e ->
                    Toast.makeText( this,"Error logging dream: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

}

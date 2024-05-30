package com.example.dreamer.ui

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.dreamer.R

class HistoryResponse : AppCompatActivity() {
    private lateinit var promptTextView: TextView
    private lateinit var contentTextView: TextView
    private lateinit var emotionTextView: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_response)

        supportActionBar?.apply {
            // Enable the back button
            setDisplayHomeAsUpEnabled(true)
            // Set the title
            title = "Dreamer"
        }

        promptTextView = findViewById(R.id.response_text_view1)
        contentTextView = findViewById(R.id.content1)

        // Retrieve the prompt from the intent extras
        val prompt = intent.getStringExtra("prompt")
        val content = intent.getStringExtra("dreamContent")
        val emotion = intent.getStringExtra("dreamEmotion")

        // Set the prompt to the TextView
        promptTextView.text = prompt
        contentTextView.text=content

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
}

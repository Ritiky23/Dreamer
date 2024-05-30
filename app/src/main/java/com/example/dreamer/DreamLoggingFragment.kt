package com.example.dreamer.ui
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

import com.example.dreamer.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class DreamLoggingFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dreamlogging, container, false)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        setHasOptionsMenu(false)
        val dreamContentEditText = view.findViewById<EditText>(R.id.dream_content_edit_text)
        val emotionEditText = view.findViewById<EditText>(R.id.emotion_edit_text)
        val submitButton = view.findViewById<Button>(R.id.submit_button)

        submitButton.setOnClickListener {
            val dreamContent = dreamContentEditText.text.toString().trim()
            val emotion = emotionEditText.text.toString().trim()

            if (dreamContent.isNotEmpty() && emotion.isNotEmpty()) {
                if (isInternetAvailable()) {
                    submitDreamLog(dreamContent, emotion)
                } else {
                    Toast.makeText(requireContext(), "Check your internet connection", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Please enter both dream content and emotion", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun submitDreamLog(dreamContent: String, emotion: String) {
        val intent = Intent(requireContext(), DreamResponseActivity::class.java)
        intent.putExtra("dreamContent", dreamContent)
        intent.putExtra("emotion", emotion)
        startActivity(intent)
    }
}

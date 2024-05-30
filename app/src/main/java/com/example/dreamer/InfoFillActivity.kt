package com.example.dreamer

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dreamer.ui.DreamLoggingActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class InfoFillActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_fill)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val nameEditText = findViewById<EditText>(R.id.name_edit_text)
        val submitButton = findViewById<Button>(R.id.name_submit_button)
        val phoneNumber=intent.getStringExtra("phoneNumber")
        submitButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            if (name.isNotEmpty()) {
                if (phoneNumber != null) {
                    saveUserNameToFirestore(name, phoneNumber)
                }
            } else {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveUserNameToFirestore(name: String,phoneNumber:String) {
        val userId = auth.currentUser?.uid ?: return
        val userMap = hashMapOf("name" to name,
            "phoneNumber" to phoneNumber,
        "profile" to "")

        firestore.collection("users").document(userId)
            .set(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Name saved successfully", Toast.LENGTH_SHORT).show()
                // Navigate to the main activity
                val intent = Intent(this, DreamLoggingActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving name: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

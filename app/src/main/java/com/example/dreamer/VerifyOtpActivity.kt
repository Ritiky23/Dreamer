package com.example.dreamer.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dreamer.InfoFillActivity
import com.example.dreamer.R
import com.example.dreamer.User
import com.example.dreamer.ui.DreamLoggingActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class VerifyOtpActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var verificationId: String
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_otp)
        progressBar = findViewById(R.id.progress_bar) // Initialize progress bar

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        verificationId = intent.getStringExtra("verificationId") ?: ""
        val otpEditText = findViewById<EditText>(R.id.otp_edit_text)
        val verifyOtpButton = findViewById<Button>(R.id.verify_otp_button)
        val phoneNumber = intent.getStringExtra("phoneNumber")

        verifyOtpButton.setOnClickListener {
            val otpCode = otpEditText.text.toString().trim()
            if (otpCode.isNotEmpty()) {
                val credential = PhoneAuthProvider.getCredential(verificationId, otpCode)
                if (phoneNumber != null) {
                    progressBar.visibility = ProgressBar.VISIBLE // Show progress bar
                    signInWithPhoneAuthCredential(credential, phoneNumber)
                }
            } else {
                Toast.makeText(this, "Please enter the OTP", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential, phoneNumber: String) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                progressBar.visibility = ProgressBar.GONE // Hide progress bar
                if (task.isSuccessful) {
                    // Check if the user's name is already present in Firestore
                    checkIfNameExistsInFirestore(phoneNumber)
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkIfNameExistsInFirestore(phoneNumber: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val userRef = firestore.collection("users").document(uid)
            userRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val user = documentSnapshot.toObject(User::class.java)
                        if (user != null && user.phoneNumber == phoneNumber) {
                            // User exists in Firestore and phone number matches, navigate to DreamLoggingActivity
                            val intent = Intent(this, DreamLoggingActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            // User doesn't exist in Firestore or phone number doesn't match, navigate to InfoFillActivity
                            val intent = Intent(this, InfoFillActivity::class.java)
                            intent.putExtra("phoneNumber", phoneNumber)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        // User document doesn't exist in Firestore, navigate to InfoFillActivity
                        val intent = Intent(this, InfoFillActivity::class.java)
                        intent.putExtra("phoneNumber", phoneNumber)
                        startActivity(intent)
                        finish()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to check user data: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Minimize the app when the back button is pressed
        moveTaskToBack(true)
    }
}

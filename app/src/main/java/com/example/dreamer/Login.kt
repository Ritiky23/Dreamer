package com.example.dreamer.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dreamer.R
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var verificationId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        if (auth.currentUser != null) {
            navigateToMainActivity()
            return
        }
        val countryCodeSpinner: Spinner = findViewById(R.id.country_code_spinner)
        val countryCodes = resources.getStringArray(R.array.country_codes)
        val adapter = ArrayAdapter(this, com.example.dreamer.R.layout.spinner_item, countryCodes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        countryCodeSpinner.adapter = adapter

        // Set default selection to +91
        val defaultSelection = countryCodes.indexOf("+91")
        if (defaultSelection != -1) {
            countryCodeSpinner.setSelection(defaultSelection)
        }

        val phoneNumberEditText = findViewById<EditText>(R.id.phone_number)
        val sendOtpButton = findViewById<Button>(R.id.send_otp_button)

        sendOtpButton.setOnClickListener {
            val selectedCountryCode = countryCodeSpinner.selectedItem.toString()
            val phoneNumber = phoneNumberEditText.text.toString().trim()
            val finalNumber=selectedCountryCode+phoneNumber
            if (phoneNumber.isNotEmpty()) {
                sendVerificationCode(finalNumber)
            } else {
                Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendVerificationCode(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneAuthCredential(credential,phoneNumber)
                }
                override fun onVerificationFailed(e: FirebaseException) {
                    Toast.makeText(this@LoginActivity, "Verification failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    super.onCodeSent(verificationId, token)
                    this@LoginActivity.verificationId = verificationId
                    // Navigate to OTP verification screen
                    val intent = Intent(this@LoginActivity, VerifyOtpActivity::class.java)
                    intent.putExtra("verificationId", verificationId)
                    intent.putExtra("phoneNumber", phoneNumber)
                    startActivity(intent)
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential,phoneNumber: String) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    saveUserInfoToFirestore(phoneNumber)
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                    navigateToMainActivity()
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserInfoToFirestore(phoneNumber: String) {
        val user = auth.currentUser
        user?.let {
            val userId = user.uid
            val name = "" // You may prompt the user to enter their name in another activity
            val profileImageUrl = "" // You may store the profile image URL if available

            val userMap = hashMapOf(
                "phoneNumber" to phoneNumber
            )
            firestore.collection("users").document(userId)
                .set(userMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "User info saved successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error saving user info: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
    private fun navigateToMainActivity() {
        val intent = Intent(this, DreamLoggingActivity::class.java)
        startActivity(intent)
        finish() // Finish the login activity so the user can't go back to it using the back button
    }
}

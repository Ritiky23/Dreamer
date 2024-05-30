package com.example.dreamer.ui
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.dreamer.R
import com.example.dreamer.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.io.File
import java.io.FileOutputStream

class ProfileFragment : Fragment() {

    private val REQUEST_IMAGE_PICK = 2
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var imageView: ImageView
    private lateinit var profileName: TextView
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageReference: FirebaseStorage
    private lateinit var selectedImg: Uri
    private var localProfileImageUrl: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storageReference = FirebaseStorage.getInstance()
        val imageButton: ImageButton = view.findViewById(R.id.profile_btn)
        val logoutBtn: ImageButton = view.findViewById(R.id.logout_btn)
        imageView = view.findViewById(R.id.profile_image)
        profileName = view.findViewById(R.id.profile_name)

        loadProfileData()
        logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }
        imageButton.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_IMAGE_PICK)
        }
        return view
    }

    private fun loadProfileData() {
        val uid = firebaseAuth.currentUser?.uid
        if (uid != null) {
            val userRef = firestore.collection("users").document(uid)
            userRef.addSnapshotListener { snapshot, exception ->
                if (exception != null || snapshot == null) {
                    // Handle error
                    return@addSnapshotListener
                }
                val user = snapshot.toObject(User::class.java)
                profileName.text = user?.name ?: "Default Name"
                saveProfileNameToLocal(user?.name ?: "Default Name")

                val profilePictureUrl = user?.profile
                Log.e("ProfileFragment", "Failed to update profile image URL in Firestore: ${profilePictureUrl} ")
                if (profilePictureUrl != null && profilePictureUrl.isNotEmpty()) {
                    // Check for internet connectivity
                    if (isInternetAvailable(requireContext())) {
                        // If internet is available, load the profile picture from Firebase
                        Picasso.get().load(profilePictureUrl).into(imageView)
                        saveProfileImageToLocal(profilePictureUrl)
                        localProfileImageUrl = profilePictureUrl
                    } else {
                        // If internet is not available, load the profile picture locally
                        loadLocalProfileImage()
                    }
                } else {
                    imageView.setImageResource(R.drawable.profile2) // Replace with your default drawable
                }
            }
        }
    }
    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun uploadImageToFirebase() {
        // Check for internet connectivity
        if (isInternetAvailable(requireContext())) {
            val uid = firebaseAuth.currentUser?.uid
            val fileName = "profile.jpg" // Use a consistent file name
            val reference = uid?.let { storageReference.reference.child("users").child(uid).child("profile").child(fileName) }
            if (reference != null) {
                reference.putFile(selectedImg)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            reference.downloadUrl.addOnSuccessListener { downloadUrl ->
                                updateProfileImageInFirestore(downloadUrl.toString())
                                saveProfileImageToLocal(downloadUrl.toString())
                                localProfileImageUrl = downloadUrl.toString()
                            }.addOnFailureListener { e ->
                                Log.e("ProfileFragment", "Failed to get download URL: ", e)
                            }
                        } else {
                            Log.e("ProfileFragment", "Image upload failed: ${task.exception?.message}")
                        }
                    }.addOnFailureListener { e ->
                        Log.e("ProfileFragment", "Upload failed: ", e)
                    }
            }
        } else {
            // Show a toast message indicating the user needs to check their internet connection
            Toast.makeText(requireContext(), "Please check your internet connection", Toast.LENGTH_SHORT).show()
        }
    }


    private fun updateProfileImageInFirestore(imageUrl: String) {
        val uid = firebaseAuth.currentUser?.uid ?: return
        val userRef = firestore.collection("users").document(uid)
        userRef.update("profile", imageUrl)
            .addOnSuccessListener {
                Log.d("ProfileFragment", "Profile image URL updated in Firestore")
            }.addOnFailureListener { e ->
                Log.e("ProfileFragment", "Failed to update profile image URL in Firestore: ", e)
            }
    }
    private fun saveProfileNameToLocal(name: String) {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString("profileName", name)
            apply()
        }
    }
    private fun loadLocalProfileImage() {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        profileName.text = sharedPref?.getString("profileName", "Default Name")
        val directory = File(activity?.getExternalFilesDir(null), "profile_images")
        val file = File(directory, "profile.jpg")
        if (file.exists()) {
            val uri = Uri.fromFile(file)
            imageView.setImageURI(uri)
            localProfileImageUrl = uri.toString()
        } else {
            imageView.setImageResource(R.drawable.profile2) // Replace with your default drawable
        }
    }
    private fun saveProfileImageToLocal(imageUrl: String) {
        Picasso.get().load(imageUrl).into(object : com.squareup.picasso.Target {
            override fun onBitmapLoaded(bitmap: android.graphics.Bitmap?, from: com.squareup.picasso.Picasso.LoadedFrom?) {
                bitmap?.let {
                    try {
                        val directory = File(activity?.getExternalFilesDir(null), "profile_images")
                        if (!directory.exists()) {
                            directory.mkdirs()
                        }
                        val file = File(directory, "profile.jpg")
                        val out = FileOutputStream(file)
                        it.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                        out.flush()
                        out.close()
                    } catch (e: Exception) {
                        Log.e("ProfileFragment", "Failed to save image locally", e)
                    }
                }
            }
            override fun onBitmapFailed(e: java.lang.Exception?, errorDrawable: android.graphics.drawable.Drawable?) {
                Log.e("ProfileFragment", "Failed to load image", e)
            }
            override fun onPrepareLoad(placeHolderDrawable: android.graphics.drawable.Drawable?) {}
        })
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            // Check for internet connectivity
            if (isInternetAvailable(requireContext())) {
                selectedImg = data.data!!
                imageView.setImageURI(selectedImg)
                uploadImageToFirebase()
            } else {
                // Show a toast message indicating the user needs to check their internet connection
                Toast.makeText(requireContext(), "Please check your internet connection", Toast.LENGTH_SHORT).show()
            }
        }
    }

}

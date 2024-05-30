package com.example.dreamer.ui

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dreamer.CustomAdapter
import com.example.dreamer.DreamResponse
import com.example.dreamer.MyApp
import com.example.dreamer.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.HashSet

class HistoryFragment : Fragment() {
    private lateinit var responseAdapter: CustomAdapter
    private lateinit var responseList: MutableList<DreamResponse>
    private lateinit var uniqueResponses: HashSet<String>
    private lateinit var emptyTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        responseList = mutableListOf()
        uniqueResponses = HashSet()
        responseAdapter = CustomAdapter(responseList) { position, dreamResponse ->
            val intent = Intent(context, HistoryResponse::class.java)
            intent.putExtra("prompt", dreamResponse.prompt)
            intent.putExtra("dreamContent", dreamResponse.dreamContent)
            intent.putExtra("dreamEmotion", dreamResponse.emotion)
            startActivity(intent)
        }
        emptyTextView = view.findViewById(R.id.emptytext)
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerview)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = responseAdapter
        }
        if (isInternetAvailable(requireContext())) {
            fetchResponses()
        } else {
            loadCachedResponses()
        }
        return view
    }
    private fun isInternetAvailable(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    private fun loadCachedResponses() {
        CoroutineScope(Dispatchers.IO).launch {
            val cachedResponses = MyApp.database.dreamResponseDao().getAllResponses()
            withContext(Dispatchers.Main) {
                for (response in cachedResponses) {
                    if (!uniqueResponses.contains(response.sorttime)) {
                        responseList.add(response)
                        uniqueResponses.add(response.sorttime)
                    }
                }
                responseAdapter.notifyDataSetChanged()
                updateEmptyTextVisibility()
            }
        }
    }

    private fun fetchResponses() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val databaseReference = FirebaseDatabase.getInstance().reference
                .child("users").child(userId).child("dreams")

            databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Clear the existing list to avoid duplicates
                    responseList.clear()
                    uniqueResponses.clear()

                    // Iterate through all children (dreams) in the snapshot
                    for (childSnapshot in snapshot.children) {
                        val dreamContent = childSnapshot.child("dreamContent").value.toString()
                        val emotion = childSnapshot.child("emotion").value.toString()
                        val prompt = childSnapshot.child("prompt").value.toString()
                        val timestamp1 = childSnapshot.child("timestamp").value.toString()
                        val timestamp = parseTimestampSort(timestamp1)
                        val dateTime = parseTimestamp(timestamp1)

                        // Use Firebase's unique key as an additional check
                        val uniqueKey = childSnapshot.key ?: continue

                        // Check for duplicates
                        if (!uniqueResponses.contains(uniqueKey)) {
                            val response = DreamResponse(dreamContent, emotion, prompt, dateTime, timestamp)
                            responseList.add(response)
                            uniqueResponses.add(uniqueKey)
                        }
                    }

                    // Sort and update the UI
                    sortResponseListByDateTime()
                    cacheResponses(responseList)
                    responseAdapter.notifyDataSetChanged()
                    updateEmptyTextVisibility()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }


    private fun cacheResponses(responses: List<DreamResponse>) {
        CoroutineScope(Dispatchers.IO).launch {
            MyApp.database.dreamResponseDao().deleteAll()
            MyApp.database.dreamResponseDao().insertAll(*responses.toTypedArray())
        }
    }

    private fun parseTimestampSort(timestamp: String): String {
        val sdf = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.getDefault())
        val date = sdf.parse(timestamp)
        val outputFormat = SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault()) // More precise format
        return outputFormat.format(date)
    }

    private fun parseTimestamp(timestamp: String): String {
        val sdf = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.getDefault())
        val date = sdf.parse(timestamp)
        val outputFormat = SimpleDateFormat("dd MMM HH:mm a", Locale.getDefault())
        return outputFormat.format(date)
    }

    private fun sortResponseListByDateTime() {
        responseList.sortByDescending { it.sorttime }
        responseAdapter.notifyDataSetChanged()
    }
    private fun updateEmptyTextVisibility() {
        if (responseList.isEmpty()) {
            emptyTextView.visibility = View.VISIBLE
        } else {
            emptyTextView.visibility = View.GONE
        }
    }
}

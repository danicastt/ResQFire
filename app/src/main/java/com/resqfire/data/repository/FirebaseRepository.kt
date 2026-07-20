package com.resqfire.data.repository

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.resqfire.data.model.FireEvent
import com.resqfire.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.math.*

class FirebaseRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val IMGBB_API_KEY = "85037aeff47044726d13976a8a3cb4ca"

    val currentUserId get() = auth.currentUser?.uid

    suspend fun uploadPhoto(uri: Uri, context: Context): String = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes() ?: return@withContext ""
            inputStream.close()
            val base64 = Base64.encodeToString(bytes, Base64.DEFAULT)
            val encodedImage = URLEncoder.encode(base64, "UTF-8")
            val postData = "image=$encodedImage".toByteArray()

            val url = URL("https://api.imgbb.com/1/upload?key=$IMGBB_API_KEY")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            connection.setRequestProperty("Content-Length", postData.size.toString())

            connection.outputStream.use { it.write(postData) }

            val responseCode = connection.responseCode
            Log.d("UploadPhoto", "Response code: $responseCode")

            if (responseCode !in 200..299) {
                val errorBody = connection.errorStream?.bufferedReader()?.readText()
                Log.e("UploadPhoto", "Upload failed: $responseCode, body: $errorBody")
                return@withContext ""
            }

            val response = connection.inputStream.bufferedReader().readText()
            Log.d("UploadPhoto", "Response body: $response")

            val json = JSONObject(response)
            json.getJSONObject("data").getString("url")
        } catch (e: Exception) {
            Log.e("UploadPhoto", "Exception during upload", e)
            ""
        }
    }

    suspend fun register(email: String, password: String): String {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        return result.user?.uid ?: throw Exception("Registration failed")
    }

    suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    fun logout() = auth.signOut()

    suspend fun saveUser(user: User) {
        db.collection("users").document(user.uid).set(user).await()
    }

    suspend fun getUser(uid: String): User? {
        return db.collection("users").document(uid).get().await().toObject(User::class.java)
    }

    suspend fun updateUserLocation(lat: Double, lng: Double) {
        val uid = currentUserId ?: return
        val ref = db.collection("users").document(uid)
        val doc = ref.get().await()
        if (doc.exists()) {
            ref.update(mapOf(
                "latitude" to lat,
                "longitude" to lng,
                "lastSeen" to com.google.firebase.Timestamp.now()
            )).await()
        } else {
            ref.set(mapOf(
                "latitude" to lat,
                "longitude" to lng,
                "lastSeen" to com.google.firebase.Timestamp.now()
            )).await()
        }
    }

    fun getUsersFlow(): Flow<List<User>> = callbackFlow {
        val listener = db.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                trySend(snapshot?.toObjects(User::class.java) ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    suspend fun addPoints(uid: String, points: Int) {
        val ref = db.collection("users").document(uid)
        db.runTransaction { transaction ->
            val current = transaction.get(ref).getLong("points")?.toInt() ?: 0
            transaction.update(ref, "points", current + points)
        }.await()
    }

    suspend fun addEvent(event: FireEvent): String {
        val ref = db.collection("events").document()
        ref.set(event.copy(id = ref.id)).await()
        currentUserId?.let { addPoints(it, 10) }
        return ref.id
    }

    fun getEventsFlow(): Flow<List<FireEvent>> = callbackFlow {
        val listener = db.collection("events")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                trySend(snapshot?.toObjects(FireEvent::class.java) ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    suspend fun joinAsVolunteer(eventId: String) {
        val uid = currentUserId ?: return
        val ref = db.collection("events").document(eventId)
        db.runTransaction { transaction ->
            val snap = transaction.get(ref)
            @Suppress("UNCHECKED_CAST")
            val volunteers = (snap.get("volunteers") as? List<String>)?.toMutableList() ?: mutableListOf()
            if (!volunteers.contains(uid)) {
                volunteers.add(uid)
                transaction.update(ref, "volunteers", volunteers)
            }
        }.await()
        addPoints(uid, 5)
    }

    fun getLeaderboardFlow(): Flow<List<User>> = callbackFlow {
        val listener = db.collection("users")
            .orderBy("points", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                trySend(snapshot?.toObjects(User::class.java) ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    fun distanceMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val r = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}
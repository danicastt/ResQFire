package com.resqfire.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint

data class User(
    @DocumentId val uid: String = "",
    val username: String = "",
    val fullName: String = "",
    val phone: String = "",
    val photoUrl: String = "",
    val points: Int = 0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val lastSeen: Timestamp? = null
)

enum class EventType(val displayName: String, val emoji: String) {
    SMOKE("Dim", "💨"),
    SMALL_FIRE("Mali požar", "🔥"),
    LARGE_FIRE("Veliki požar", "🔥🔥")
}

data class FireEvent(
    @DocumentId val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val type: String = EventType.SMOKE.name,
    val description: String = "",
    val comment: String = "",
    val photoUrl: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Timestamp? = null,
    val volunteers: List<String> = emptyList(),
    val active: Boolean = true
) {
    fun getEventType(): EventType = try {
        EventType.valueOf(type)
    } catch (e: Exception) {
        EventType.SMOKE
    }
}

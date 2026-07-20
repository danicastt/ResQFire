package com.resqfire.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.resqfire.data.model.EventType
import com.resqfire.data.model.FireEvent
import com.resqfire.data.model.User
import com.resqfire.data.repository.FirebaseRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MainUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUser: User? = null,
    val events: List<FireEvent> = emptyList(),
    val users: List<User> = emptyList(),
    val leaderboard: List<User> = emptyList(),
    val userLat: Double = 0.0,
    val userLng: Double = 0.0
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val repo = FirebaseRepository()

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(repo.currentUserId != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _nearbyAlert = MutableStateFlow<FireEvent?>(null)
    val nearbyAlert: StateFlow<FireEvent?> = _nearbyAlert.asStateFlow()

    init {
        if (repo.currentUserId != null) loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            repo.currentUserId?.let { uid ->
                val user = repo.getUser(uid)
                _uiState.update { it.copy(currentUser = user) }
            }
        }
        viewModelScope.launch {
            repo.getEventsFlow().collect { events ->
                _uiState.update { it.copy(events = events) }
                checkNearbyEvents()
            }
        }
        viewModelScope.launch {
            repo.getUsersFlow().collect { users ->
                _uiState.update { it.copy(users = users) }
            }
        }
        viewModelScope.launch {
            repo.getLeaderboardFlow().collect { lb ->
                _uiState.update { it.copy(leaderboard = lb) }
            }
        }
    }

    fun updateLocation(lat: Double, lng: Double) {
        _uiState.update { it.copy(userLat = lat, userLng = lng) }
        viewModelScope.launch { repo.updateUserLocation(lat, lng) }
        checkNearbyEvents()
    }

    private fun checkNearbyEvents() {
        val lat = _uiState.value.userLat
        val lng = _uiState.value.userLng
        if (lat == 0.0 && lng == 0.0) return
        val nearby = _uiState.value.events.firstOrNull { event ->
            repo.distanceMeters(lat, lng, event.latitude, event.longitude) < 1000
        }
        _nearbyAlert.value = nearby
    }

    fun dismissAlert() { _nearbyAlert.value = null }

    suspend fun register(
        email: String, password: String, username: String,
        fullName: String, phone: String, photoUri: Uri?
    ): Boolean {
        _uiState.update { it.copy(isLoading = true, error = null) }
        return try {
            val uid = repo.register(email, password)
            val photoUrl = photoUri?.let { repo.uploadPhoto(it, getApplication()) } ?: ""
            val user = User(uid = uid, username = username, fullName = fullName,
                phone = phone, photoUrl = photoUrl, points = 0)
            repo.saveUser(user)
            _uiState.update { it.copy(currentUser = user, isLoading = false) }
            _isLoggedIn.value = true
            loadData()
            true
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message, isLoading = false) }
            false
        }
    }

    suspend fun login(email: String, password: String): Boolean {
        _uiState.update { it.copy(isLoading = true, error = null) }
        return try {
            repo.login(email, password)
            val uid = repo.currentUserId ?: return false
            val user = repo.getUser(uid)
            _uiState.update { it.copy(currentUser = user, isLoading = false, error = null) }
            _isLoggedIn.value = true
            loadData()
            true
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message, isLoading = false) }
            false
        }
    }

    fun logout() {
        repo.logout()
        _isLoggedIn.value = false
        _nearbyAlert.value = null
        _uiState.value = MainUiState()
    }

    suspend fun addEvent(
        type: EventType, description: String, comment: String, photoUri: Uri?
    ): Boolean {
        return try {
            val lat = _uiState.value.userLat
            val lng = _uiState.value.userLng
            val user = _uiState.value.currentUser
            val photoUrl = photoUri?.let { repo.uploadPhoto(it, getApplication()) } ?: ""
            val event = FireEvent(
                authorId = repo.currentUserId ?: "",
                authorName = user?.fullName ?: "",
                type = type.name,
                description = description,
                comment = comment,
                photoUrl = photoUrl,
                latitude = lat,
                longitude = lng,
                timestamp = Timestamp.now()
            )
            repo.addEvent(event)
            true
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message) }
            false
        }
    }

    suspend fun joinVolunteer(eventId: String) {
        try { repo.joinAsVolunteer(eventId) } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message) }
        }
    }

    fun getFilteredEvents(
        typeFilter: EventType? = null,
        radiusKm: Double? = null,
        authorFilter: String? = null
    ): List<FireEvent> {
        val lat = _uiState.value.userLat
        val lng = _uiState.value.userLng
        return _uiState.value.events.filter { event ->
            val typeOk = typeFilter == null || event.type == typeFilter.name
            val authorOk = authorFilter == null || event.authorName.contains(authorFilter, ignoreCase = true)
            val radiusOk = radiusKm == null || lat == 0.0 || lng == 0.0 ||
                    repo.distanceMeters(lat, lng, event.latitude, event.longitude) <= radiusKm * 1000
            typeOk && authorOk && radiusOk
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}

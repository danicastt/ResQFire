package com.resqfire.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.resqfire.data.model.EventType
import com.resqfire.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    viewModel: MainViewModel,
    eventId: String,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val event = uiState.events.find { it.id == eventId }
    val currentUid = viewModel.repo.currentUserId
    val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    var isVolunteer by remember(event) {
        mutableStateOf(event?.volunteers?.contains(currentUid) == true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(event?.getEventType()?.displayName ?: "Detalji") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Nazad")
                    }
                }
            )
        }
    ) { padding ->
        if (event == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Type badge
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(event.getEventType().emoji, fontSize = 40.sp)
                    Column {
                        Text(event.getEventType().displayName, fontSize = 22.sp,
                            fontWeight = FontWeight.Bold)
                        Text("Prijavio: ${event.authorName}", fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(0.6f))
                        val date = event.timestamp?.toDate()?.let { sdf.format(it) } ?: "—"
                        Text(date, fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(0.5f))
                    }
                }

                // Photo
                if (event.photoUrl.isNotBlank()) {
                    AsyncImage(
                        model = event.photoUrl,
                        contentDescription = "Fotografija događaja",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }

                // Description
                Card(colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Opis", fontWeight = FontWeight.Bold, fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(event.description)
                    }
                }

                if (event.comment.isNotBlank()) {
                    Card(colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Komentar", fontWeight = FontWeight.Bold, fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(event.comment)
                        }
                    }
                }

                // Map
                Text("📍 Lokacija na mapi", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                val eventLatLng = LatLng(event.latitude, event.longitude)
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(eventLatLng, 15f)
                }
                val markerColor = when (event.getEventType()) {
                    EventType.SMOKE -> BitmapDescriptorFactory.HUE_YELLOW
                    EventType.SMALL_FIRE -> BitmapDescriptorFactory.HUE_ORANGE
                    EventType.LARGE_FIRE -> BitmapDescriptorFactory.HUE_RED
                }
                GoogleMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = false),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = true,
                        scrollGesturesEnabled = true
                    )
                ) {
                    Marker(
                        state = MarkerState(position = eventLatLng),
                        title = event.getEventType().displayName,
                        snippet = event.description,
                        icon = BitmapDescriptorFactory.defaultMarker(markerColor)
                    )
                }

                // Volunteers
                Card(colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("🙋 Volonteri: ${event.volunteers.size}",
                            fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                // Volunteer button
                if (!isVolunteer) {
                    Button(
                        onClick = {
                            scope.launch {
                                viewModel.joinVolunteer(eventId)
                                isVolunteer = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Filled.VolunteerActivism, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Prijavim se kao volonter (+5 poena)", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Card(colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondary.copy(0.2f))) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.Center) {
                            Text("✅ Prijavljen/a si kao volonter",
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

package com.resqfire.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.resqfire.data.model.EventType
import com.resqfire.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MainViewModel,
    onNavigateToAdd: () -> Unit,
    onNavigateToList: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onEventClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val nearbyAlert by viewModel.nearbyAlert.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(43.32, 21.89), 13f)
    }

    LaunchedEffect(Unit) {
        while (true) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        viewModel.updateLocation(it.latitude, it.longitude)
                        scope.launch {
                            cameraPositionState.animate(
                                com.google.android.gms.maps.CameraUpdateFactory.newLatLng(
                                    LatLng(it.latitude, it.longitude)
                                )
                            )
                        }
                    }
                }
            } catch (_: Exception) {}
            delay(15000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🔥 ResQFire") },
                actions = {
                    IconButton(onClick = onNavigateToLeaderboard) {
                        Icon(Icons.Filled.Leaderboard, contentDescription = "Rang lista")
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Filled.Person, contentDescription = "Profil")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FloatingActionButton(
                    onClick = onNavigateToList,
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(Icons.Filled.List, contentDescription = "Lista")
                }
                FloatingActionButton(
                    onClick = onNavigateToAdd,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Dodaj")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = true)
            ) {
                uiState.events.forEach { event ->
                    val color = when (event.getEventType()) {
                        EventType.SMOKE -> BitmapDescriptorFactory.HUE_YELLOW
                        EventType.SMALL_FIRE -> BitmapDescriptorFactory.HUE_ORANGE
                        EventType.LARGE_FIRE -> BitmapDescriptorFactory.HUE_RED
                    }
                    Marker(
                        state = MarkerState(position = LatLng(event.latitude, event.longitude)),
                        title = event.getEventType().displayName,
                        snippet = event.description,
                        icon = BitmapDescriptorFactory.defaultMarker(color),
                        onClick = { onEventClick(event.id); true }
                    )
                }
            }

            nearbyAlert?.let { event ->
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Warning, contentDescription = null,
                            tint = Color.White, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("⚠️ UPOZORENJE!", color = Color.White, fontSize = 14.sp)
                            Text("${event.getEventType().displayName} u blizini!",
                                color = Color.White, fontSize = 13.sp)
                        }
                        IconButton(onClick = { viewModel.dismissAlert() }) {
                            Icon(Icons.Filled.Close, contentDescription = "Zatvori", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}
package com.resqfire.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Logout
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
import com.resqfire.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val user = uiState.currentUser
    val myEvents = uiState.events.filter { it.authorId == viewModel.repo.currentUserId }
    val myVolunteering = uiState.events.filter {
        it.volunteers.contains(viewModel.repo.currentUserId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Nazad")
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Filled.Logout, contentDescription = "Odjavi se",
                            tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar
            if (user?.photoUrl?.isNotBlank() == true) {
                AsyncImage(
                    model = user.photoUrl,
                    contentDescription = "Profilna fotografija",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(100.dp).clip(CircleShape)
                )
            } else {
                Surface(shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(0.3f),
                    modifier = Modifier.size(100.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(user?.fullName?.take(1)?.uppercase() ?: "?",
                            fontSize = 40.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text(user?.fullName ?: "—", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("@${user?.username ?: "—"}", fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(0.6f))
            Text("📞 ${user?.phone ?: "—"}", fontSize = 14.sp)

            // Points
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(0.15f))
            ) {
                Column(modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${user?.points ?: 0}", fontSize = 48.sp, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary)
                    Text("ukupnih poena", fontSize = 14.sp)
                }
            }

            // Stats
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(modifier = Modifier.weight(1f),
                    label = "Prijavljenih događaja",
                    value = myEvents.size.toString(), emoji = "🔥")
                StatCard(modifier = Modifier.weight(1f),
                    label = "Volonterskih akcija",
                    value = myVolunteering.size.toString(), emoji = "🙋")
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier, label: String, value: String, emoji: String) {
    Card(modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 24.sp)
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
            Text(label, fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
        }
    }
}

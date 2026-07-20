package com.resqfire.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.resqfire.data.model.User
import com.resqfire.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUid = viewModel.repo.currentUserId

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🏆 Rang lista") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Nazad")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(uiState.leaderboard) { index, user ->
                LeaderboardItem(
                    rank = index + 1,
                    user = user,
                    isCurrentUser = user.uid == currentUid
                )
            }
        }
    }
}

@Composable
fun LeaderboardItem(rank: Int, user: User, isCurrentUser: Boolean) {
    val medal = when (rank) {
        1 -> "🥇"
        2 -> "🥈"
        3 -> "🥉"
        else -> "#$rank"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(medal, fontSize = if (rank <= 3) 28.sp else 18.sp,
                modifier = Modifier.width(40.dp))

            if (user.photoUrl.isNotBlank()) {
                AsyncImage(
                    model = user.photoUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(44.dp).clip(CircleShape)
                )
            } else {
                Box(modifier = Modifier.size(44.dp).clip(CircleShape)
                    .then(Modifier.padding(0.dp)),
                    contentAlignment = Alignment.Center) {
                    Surface(shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(0.3f),
                        modifier = Modifier.size(44.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(user.fullName.take(1).uppercase(), fontSize = 20.sp,
                                fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("@${user.username}", fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("${user.points}", fontSize = 22.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary)
                Text("poena", fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
            }
        }
    }
}

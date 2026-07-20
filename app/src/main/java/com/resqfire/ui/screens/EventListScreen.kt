package com.resqfire.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.resqfire.data.model.EventType
import com.resqfire.data.model.FireEvent
import com.resqfire.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(
    viewModel: MainViewModel,
    onEventClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedType by remember { mutableStateOf<EventType?>(null) }
    var radiusKm by remember { mutableStateOf<Double?>(null) }
    var searchText by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    var radiusInput by remember { mutableStateOf("") }

    val filtered = remember(uiState.events, selectedType, radiusKm, searchText) {
        viewModel.getFilteredEvents(selectedType, radiusKm)
            .filter { it.description.contains(searchText, ignoreCase = true) ||
                    it.authorName.contains(searchText, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Događaji (${filtered.size})") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Nazad")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(Icons.Filled.FilterList, contentDescription = "Filteri")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Search
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Pretraži po opisu ili autoru") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )

            // Filters panel
            if (showFilters) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Tip događaja:", fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = selectedType == null,
                                onClick = { selectedType = null },
                                label = { Text("Svi") }
                            )
                            EventType.values().forEach { type ->
                                FilterChip(
                                    selected = selectedType == type,
                                    onClick = { selectedType = if (selectedType == type) null else type },
                                    label = { Text(type.emoji) }
                                )
                            }
                        }

                        Text("Radijus pretrage:", fontWeight = FontWeight.SemiBold)
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = radiusInput,
                                onValueChange = { radiusInput = it },
                                label = { Text("km") },
                                modifier = Modifier.width(100.dp),
                                singleLine = true
                            )
                            Button(onClick = {
                                radiusKm = radiusInput.toDoubleOrNull()
                            }) { Text("Primeni") }
                            if (radiusKm != null) {
                                TextButton(onClick = { radiusKm = null; radiusInput = "" }) {
                                    Text("Resetuj")
                                }
                            }
                        }
                    }
                }
            }

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nema događaja", color = MaterialTheme.colorScheme.onBackground.copy(0.5f))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered) { event ->
                        EventCard(event = event, onClick = { onEventClick(event.id) })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventCard(event: FireEvent, onClick: () -> Unit) {
    val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    val date = event.timestamp?.toDate()?.let { sdf.format(it) } ?: "—"

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = event.getEventType().emoji, fontSize = 36.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(event.getEventType().displayName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(event.description.take(60) + if (event.description.length > 60) "..." else "",
                    fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.7f))
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("👤 ${event.authorName}", fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                    Text("🕐 $date", fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                }
                if (event.volunteers.isNotEmpty()) {
                    Text("🙋 ${event.volunteers.size} volonter(a)", fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}

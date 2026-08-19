package com.monevix.app.ui.worker

import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.monevix.app.MonevixApplication
import com.monevix.app.data.worker.WorkerEntity
import com.monevix.app.navigation.MonevixDestinations

private enum class WorkerFilter(val label: String) {
    ACTIVE("Activos"),
    INACTIVE("Inactivos"),
    ALL("Todos")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerListScreen(navController: NavHostController) {
    val app = LocalContext.current.applicationContext as MonevixApplication
    val viewModel: WorkerViewModel = viewModel(
        factory = WorkerViewModelFactory(app.workerRepository)
    )
    val workers by viewModel.workers.collectAsStateWithLifecycle()
    var filter by remember { mutableStateOf(WorkerFilter.ACTIVE) }

    val filteredWorkers = when (filter) {
        WorkerFilter.ACTIVE -> workers.filter { it.isActive }
        WorkerFilter.INACTIVE -> workers.filter { !it.isActive }
        WorkerFilter.ALL -> workers
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personal") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(MonevixDestinations.workerForm()) }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Añadir trabajador")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            FilterChipsRow(current = filter, onSelect = { filter = it })

            if (filteredWorkers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay trabajadores para mostrar.\nToca + para crear el primero.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredWorkers, key = { it.id }) { worker ->
                        WorkerCard(
                            worker = worker,
                            onEdit = {
                                navController.navigate(MonevixDestinations.workerForm(worker.id))
                            },
                            onHistory = {
                                navController.navigate(MonevixDestinations.workerHistory(worker.id))
                            },
                            onToggleActive = { active ->
                                viewModel.setActive(worker, active)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipsRow(current: WorkerFilter, onSelect: (WorkerFilter) -> Unit) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WorkerFilter.entries.forEach { option ->
            FilterChip(
                selected = current == option,
                onClick = { onSelect(option) },
                label = { Text(option.label) }
            )
        }
    }
}

@Composable
private fun WorkerCard(
    worker: WorkerEntity,
    onEdit: () -> Unit,
    onHistory: () -> Unit,
    onToggleActive: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (worker.isActive)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = worker.name,
                    style = MaterialTheme.typography.titleMedium
                )
                EstadoBadge(isActive = worker.isActive)
            }

            if (!worker.documentId.isNullOrBlank()) {
                Text(
                    text = "Documento: ${worker.documentId}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (!worker.phone.isNullOrBlank()) {
                Text(
                    text = "Teléfono: ${worker.phone}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            androidx.compose.foundation.layout.Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onHistory) {
                    Icon(Icons.Filled.History, contentDescription = null)
                    Text(" Historial")
                }
                TextButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, contentDescription = null)
                    Text(" Editar")
                }
                TextButton(onClick = { onToggleActive(!worker.isActive) }) {
                    Text(if (worker.isActive) "Desactivar" else "Activar")
                }
            }
        }
    }
}

@Composable
private fun EstadoBadge(isActive: Boolean) {
    val (bg, fg, label) = if (isActive) {
        Triple(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, "Activo")
    } else {
        Triple(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer, "Inactivo")
    }
    Box(
        modifier = Modifier
            .padding(start = 8.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = fg)
    }
}

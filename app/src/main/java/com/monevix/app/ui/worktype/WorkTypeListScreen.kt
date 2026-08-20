package com.monevix.app.ui.worktype

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.monevix.app.MonevixApplication
import com.monevix.app.data.worktype.WorkTypeEntity
import com.monevix.app.navigation.MonevixDestinations
import com.monevix.app.util.CurrencyFormatter

private enum class WorkTypeFilter(val label: String) {
    ACTIVE("Activos"),
    INACTIVE("Inactivos"),
    ALL("Todos")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkTypeListScreen(navController: NavHostController) {
    val app = LocalContext.current.applicationContext as MonevixApplication
    val viewModel: WorkTypeViewModel = viewModel(
        factory = WorkTypeViewModelFactory(app.workTypeRepository)
    )
    val workTypes by viewModel.workTypes.collectAsStateWithLifecycle()
    var filter by remember { mutableStateOf(WorkTypeFilter.ACTIVE) }

    val filteredWorkTypes = when (filter) {
        WorkTypeFilter.ACTIVE -> workTypes.filter { it.isActive }
        WorkTypeFilter.INACTIVE -> workTypes.filter { !it.isActive }
        WorkTypeFilter.ALL -> workTypes
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trabajos") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(MonevixDestinations.workTypeForm()) }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Añadir trabajo")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            FilterChipsRow(current = filter, onSelect = { filter = it })

            if (filteredWorkTypes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay trabajos para mostrar.\nToca + para crear el primero.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredWorkTypes, key = { it.id }) { workType ->
                        WorkTypeCard(
                            workType = workType,
                            onEdit = {
                                navController.navigate(MonevixDestinations.workTypeForm(workType.id))
                            },
                            onToggleActive = { active ->
                                viewModel.setActive(workType, active)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipsRow(current: WorkTypeFilter, onSelect: (WorkTypeFilter) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WorkTypeFilter.entries.forEach { option ->
            FilterChip(
                selected = current == option,
                onClick = { onSelect(option) },
                label = { Text(option.label) }
            )
        }
    }
}

@Composable
private fun WorkTypeCard(
    workType: WorkTypeEntity,
    onEdit: () -> Unit,
    onToggleActive: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (workType.isActive)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CodeBadge(code = workType.code)
                    Text(
                        text = workType.name,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                EstadoBadge(isActive = workType.isActive)
            }

            Text(
                text = "Valor por unidad: ${CurrencyFormatter.format(workType.unitPrice)}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, contentDescription = null)
                    Text(" Editar")
                }
                TextButton(onClick = { onToggleActive(!workType.isActive) }) {
                    Text(if (workType.isActive) "Desactivar" else "Activar")
                }
            }
        }
    }
}

@Composable
private fun CodeBadge(code: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = code,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
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
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = fg)
    }
}

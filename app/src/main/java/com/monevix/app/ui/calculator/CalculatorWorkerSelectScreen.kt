package com.monevix.app.ui.calculator

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.monevix.app.MonevixApplication
import com.monevix.app.navigation.MonevixDestinations

/**
 * Paso 1 del flujo de Calculadora: "Seleccionar trabajador".
 * Solo muestra trabajadores activos, ya que no tiene sentido calcular un
 * pago para alguien desactivado.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorWorkerSelectScreen(navController: NavHostController) {
    val app = LocalContext.current.applicationContext as MonevixApplication
    val activeWorkers by app.workerRepository.observeActive()
        .collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calculadora") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (activeWorkers.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay trabajadores activos.\nCrea uno en Personal antes de continuar.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp)
                )
            }
            return@Scaffold
        }

        Column(modifier = Modifier.padding(innerPadding)) {
            Text(
                text = "1. Selecciona el trabajador",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
            ) {
                items(activeWorkers, key = { it.id }) { worker ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clickable {
                                navController.navigate(MonevixDestinations.calculatorDetail(worker.id))
                            }
                    ) {
                        ListItem(
                            headlineContent = { Text(worker.name) },
                            trailingContent = {
                                Icon(Icons.Filled.ChevronRight, contentDescription = null)
                            }
                        )
                    }
                }
            }
        }
    }
}

package com.monevix.app.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.monevix.app.MonevixApplication
import com.monevix.app.data.settlement.WorkerMonthlySummary
import com.monevix.app.util.CurrencyFormatter
import com.monevix.app.util.PdfExportUtil

/**
 * FASE 6: Resumen mensual. Calcula automáticamente, por trabajador, el
 * total pagado, el número de liquidaciones y la cantidad de trabajos
 * realizados dentro del mes seleccionado, además del total general.
 *
 * El PDF de este resumen se puede descargar en cualquier momento con el
 * botón "Descargar PDF", no solo a fin de mes: el recordatorio de fin de
 * mes únicamente avisa que el mes terminó, para que entres a revisarlo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlySummaryScreen(navController: NavHostController) {
    val app = LocalContext.current.applicationContext as MonevixApplication
    val context = LocalContext.current
    val vm: MonthlySummaryViewModel = viewModel(
        factory = MonthlySummaryViewModelFactory(app.settlementRepository)
    )
    val periodLabel by vm.periodLabel.collectAsStateWithLifecycle()
    val rows by vm.rows.collectAsStateWithLifecycle()
    val totalGeneral = rows.sumOf { it.totalPaid }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resumen mensual") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { vm.previousMonth() }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Mes anterior")
                }
                Text(periodLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                IconButton(onClick = { vm.nextMonth() }, enabled = vm.canGoNext) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Mes siguiente")
                }
            }

            Spacer(Modifier.height(12.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Total general del mes", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        CurrencyFormatter.format(totalGeneral),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (rows.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.QueryStats, contentDescription = null)
                        Text("No hay liquidaciones en este mes.", Modifier.padding(top = 12.dp))
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(rows, key = { it.workerId }) { row -> WorkerSummaryCard(row) }
                }
            }

            Button(
                onClick = {
                    val uri = PdfExportUtil.exportMonthlySummary(context, periodLabel, rows, totalGeneral)
                    PdfExportUtil.sharePdf(context, uri)
                },
                enabled = rows.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Icon(Icons.Filled.PictureAsPdf, contentDescription = null)
                Text("  Descargar PDF", modifier = Modifier.padding(start = 4.dp))
            }
        }
    }
}

@Composable
private fun WorkerSummaryCard(row: WorkerMonthlySummary) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(row.workerName, fontWeight = FontWeight.Bold)
                Text(
                    "${row.settlementCount} liquidaciones · ${row.totalQuantity} unidades",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                CurrencyFormatter.format(row.totalPaid),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

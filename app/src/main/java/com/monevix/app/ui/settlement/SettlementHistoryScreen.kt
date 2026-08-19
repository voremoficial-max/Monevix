package com.monevix.app.ui.settlement

import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.monevix.app.MonevixApplication
import com.monevix.app.data.settlement.SettlementEntity
import com.monevix.app.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettlementHistoryScreen(navController: NavHostController, initialWorkerId: Long? = null) {
    val app = LocalContext.current.applicationContext as MonevixApplication
    val vm: SettlementViewModel = viewModel(factory = SettlementViewModelFactory(app.settlementRepository, app.workerRepository))
    val settlements by vm.settlements.collectAsStateWithLifecycle()
    val workers by vm.workers.collectAsStateWithLifecycle()
    var selectedSettlement by remember { mutableStateOf<SettlementEntity?>(null) }
    var workerMenu by remember { mutableStateOf(false) }
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }
    LaunchedEffect(initialWorkerId) { if (initialWorkerId != null) vm.setWorker(initialWorkerId) }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Liquidaciones e historial") },
            navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Filled.ArrowBack, "Volver") } },
            actions = { Icon(Icons.Filled.FilterAlt, contentDescription = "Filtros", modifier = Modifier.padding(end = 16.dp)) }
        )
    }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().padding(16.dp)) {
            Box {
                OutlinedButton(onClick = { workerMenu = true }, modifier = Modifier.fillMaxWidth()) {
                    val selected = workers.firstOrNull { it.id == vm.selectedWorkerId.collectAsStateWithLifecycle().value }
                    Text(selected?.name ?: "Todos los trabajadores")
                }
                DropdownMenu(expanded = workerMenu, onDismissRequest = { workerMenu = false }) {
                    DropdownMenuItem(text = { Text("Todos los trabajadores") }, onClick = { vm.setWorker(null); workerMenu = false })
                    workers.forEach { worker ->
                        DropdownMenuItem(text = { Text(worker.name) }, onClick = { vm.setWorker(worker.id); workerMenu = false })
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(top=8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { showFromPicker = true }, modifier = Modifier.weight(1f)) { Text("Desde") }
                OutlinedButton(onClick = { showToPicker = true }, modifier = Modifier.weight(1f)) { Text("Hasta") }
            }
            if (settlements.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.ReceiptLong, null)
                        Text("No hay liquidaciones en este periodo.", Modifier.padding(top=12.dp))
                    }
                }
            } else {
                LazyColumn(Modifier.fillMaxSize().padding(top=12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(settlements, key={it.id}) { settlement ->
                        SettlementCard(settlement) { selectedSettlement = settlement }
                    }
                }
            }
        }
    }

    if (showFromPicker) {
        DatePickerDialog(
            onDismissRequest = { showFromPicker = false },
            confirmButton = {
                TextButton(onClick = { /* state handled below */ }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick={showFromPicker=false}) { Text("Cancelar") } }
        ) {
            val state = rememberDatePickerState(initialSelectedDateMillis = vm.fromMillis.collectAsStateWithLifecycle().value)
            DatePicker(state)
            LaunchedEffect(state.selectedDateMillis) {
                state.selectedDateMillis?.let { vm.setDateRange(it, vm.toMillis.value) }
            }
        }
    }
    if (showToPicker) {
        DatePickerDialog(
            onDismissRequest = { showToPicker = false },
            confirmButton = { TextButton(onClick={showToPicker=false}) { Text("Aceptar") } },
            dismissButton = { TextButton(onClick={showToPicker=false}) { Text("Cancelar") } }
        ) {
            val state = rememberDatePickerState(initialSelectedDateMillis = vm.toMillis.collectAsStateWithLifecycle().value)
            DatePicker(state)
            LaunchedEffect(state.selectedDateMillis) {
                state.selectedDateMillis?.let { 
                    val c=Calendar.getInstance().apply { timeInMillis=it; set(Calendar.HOUR_OF_DAY,23); set(Calendar.MINUTE,59); set(Calendar.SECOND,59); set(Calendar.MILLISECOND,999) }
                    vm.setDateRange(vm.fromMillis.value, c.timeInMillis)
                }
            }
        }
    }

    selectedSettlement?.let { SettlementDetailDialog(it, vm, onDismiss={selectedSettlement=null}) }
}

@Composable
private fun SettlementCard(settlement: SettlementEntity, onClick: () -> Unit) {
    val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es","CO")).format(Date(settlement.dateMillis))
    Card(Modifier.fillMaxWidth().clickable(onClick=onClick)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement=Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text(settlement.workerName, fontWeight=FontWeight.Bold)
                Text("$date · ${settlement.periodLabel}", style=MaterialTheme.typography.bodySmall)
            }
            Text(CurrencyFormatter.format(settlement.total), fontWeight=FontWeight.Bold, color=MaterialTheme.colorScheme.primary)
        }
    }
}

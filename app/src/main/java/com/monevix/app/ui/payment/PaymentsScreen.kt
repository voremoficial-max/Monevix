package com.monevix.app.ui.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.monevix.app.MonevixApplication
import com.monevix.app.data.payment.PaymentEntity
import com.monevix.app.data.workentry.WorkEntryEntity
import com.monevix.app.util.CurrencyFormatter
import com.monevix.app.util.PdfExportUtil
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val app = context.applicationContext as MonevixApplication
    val scope = rememberCoroutineScope()
    val pendingEntries by app.workEntryRepository.observePending().collectAsStateWithLifecycle(initialValue = emptyList())
    val payments by app.paymentRepository.observeAll().collectAsStateWithLifecycle(initialValue = emptyList())
    val preferences = remember { context.getSharedPreferences("monevix_preferences", android.content.Context.MODE_PRIVATE) }
    val companyName = preferences.getString("company_name", "").orEmpty()
    val periodType = preferences.getString("payment_frequency", "WEEKLY").orEmpty()

    var payingWorkerId by remember { mutableStateOf<Long?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedPayment by remember { mutableStateOf<PaymentEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pagos") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Filled.ArrowBack, null) } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Text("Pendiente por pagar", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
            val groups = pendingEntries.groupBy { it.workerId to it.workerName }
                .entries.sortedBy { it.key.second.lowercase(Locale.getDefault()) }
            if (groups.isEmpty()) {
                item { Text("No hay trabajos pendientes de pago.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                items(groups, key = { it.key.first }) { group ->
                    PendingWorkerCard(group.key.second, group.value) { payingWorkerId = group.key.first }
                }
            }
            item {
                Spacer(Modifier.height(8.dp))
                Text("Historial de pagos", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
            if (payments.isEmpty()) {
                item { Text("Todavía no hay pagos registrados.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                items(payments, key = { it.id }) { payment -> PaymentHistoryCard(payment) { selectedPayment = payment } }
            }
        }
    }

    payingWorkerId?.let { workerId ->
        val group = pendingEntries.filter { it.workerId == workerId }
        val workerName = group.firstOrNull()?.workerName.orEmpty()
        AlertDialog(
            onDismissRequest = { payingWorkerId = null },
            title = { Text("Confirmar pago") },
            text = { Text("Pagar ${CurrencyFormatter.format(group.sumOf { it.subtotal })} a $workerName y marcar sus trabajos pendientes como pagados.") },
            confirmButton = {
                Button(onClick = {
                    if (group.isEmpty()) { payingWorkerId = null; return@Button }
                    scope.launch {
                        runCatching {
                            val now = System.currentTimeMillis()
                            val label = paymentPeriodLabel(now, periodType)
                            val id = app.paymentRepository.payWorker(workerId, workerName, companyName, periodType, label, now)
                            val payment = PaymentEntity(id, workerId, workerName, companyName, now, periodType, label, group.sumOf { it.subtotal })
                            val items = app.paymentRepository.getItems(id)
                            val uri = PdfExportUtil.exportPayment(context, payment, items)
                            PdfExportUtil.sharePdf(context, uri)
                        }.onSuccess { payingWorkerId = null }
                            .onFailure { error = it.message ?: "No se pudo registrar el pago." }
                    }
                }) { Text("Pagar") }
            },
            dismissButton = { TextButton(onClick = { payingWorkerId = null }) { Text("Cancelar") } }
        )
    }

    selectedPayment?.let { PaymentDetailDialog(it, app) { selectedPayment = null } }
    error?.let { AlertDialog(onDismissRequest = { error = null }, title = { Text("No se pudo registrar") }, text = { Text(it) }, confirmButton = { TextButton(onClick = { error = null }) { Text("Aceptar") } }) }
}

@Composable
private fun PendingWorkerCard(workerName: String, entries: List<WorkEntryEntity>, onPay: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(workerName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("${entries.sumOf { it.quantity }} unidades · ${entries.size} registros")
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("es", "CO"))
            entries.groupBy { dateFormat.format(Date(it.dateMillis)) }.forEach { (date, dayEntries) ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(date)
                    Text(CurrencyFormatter.format(dayEntries.sumOf { it.subtotal }), fontWeight = FontWeight.SemiBold)
                }
            }
            HorizontalDivider()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Acumulado", style = MaterialTheme.typography.titleMedium)
                Text(CurrencyFormatter.format(entries.sumOf { it.subtotal }), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Button(onClick = onPay, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.CheckCircle, null)
                Spacer(Modifier.width(8.dp))
                Text("Pagar y generar PDF")
            }
        }
    }
}

@Composable
private fun PaymentHistoryCard(payment: PaymentEntity, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        ListItem(
            leadingContent = { Icon(Icons.Filled.Payments, null) },
            headlineContent = { Text(payment.workerName, fontWeight = FontWeight.Bold) },
            supportingContent = { Text("${payment.periodLabel} · ${formatDateTime(payment.paidAtMillis)}") },
            trailingContent = { Text(CurrencyFormatter.format(payment.total), fontWeight = FontWeight.Bold) }
        )
    }
}

@Composable
private fun PaymentDetailDialog(payment: PaymentEntity, app: MonevixApplication, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val items by app.paymentRepository.observeItems(payment.id).collectAsStateWithLifecycle(initialValue = emptyList())
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale("es", "CO")) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pago realizado") },
        text = {
            Column {
                Text(payment.companyName.ifBlank { "Monevix" }, style = MaterialTheme.typography.labelLarge)
                Text(payment.workerName, style = MaterialTheme.typography.titleMedium)
                Text("${payment.periodLabel} · ${formatDateTime(payment.paidAtMillis)}", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(10.dp))
                LazyColumn(Modifier.heightIn(max = 300.dp)) {
                    items(items) { item ->
                        Column(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                            Text("${dateFormat.format(Date(item.dateMillis))} · ${item.code} ${item.name}")
                            Text("${item.quantity} × ${CurrencyFormatter.format(item.unitPrice)} = ${CurrencyFormatter.format(item.subtotal)}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Text("Total: ${CurrencyFormatter.format(payment.total)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } },
        dismissButton = {
            TextButton(onClick = {
                val uri = PdfExportUtil.exportPayment(context, payment, items)
                PdfExportUtil.sharePdf(context, uri)
            }) {
                Icon(Icons.Filled.PictureAsPdf, null)
                Spacer(Modifier.width(6.dp))
                Text("PDF")
            }
        }
    )
}

private fun formatDateTime(millis: Long): String = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CO")).format(Date(millis))

private fun paymentPeriodLabel(now: Long, periodType: String): String {
    val c = java.util.Calendar.getInstance().apply { timeInMillis = now }
    return when (periodType) {
        "MONTHLY" -> SimpleDateFormat("MMMM yyyy", Locale("es", "CO")).format(Date(now)).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "CO")) else it.toString() }
        "BIWEEKLY" -> {
            val month = SimpleDateFormat("MMMM yyyy", Locale("es", "CO")).format(Date(now)).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "CO")) else it.toString() }
            "${if (c.get(java.util.Calendar.DAY_OF_MONTH) <= 15) "1ª quincena" else "2ª quincena"} de $month"
        }
        else -> "Semana ${c.get(java.util.Calendar.WEEK_OF_YEAR)} de ${c.get(java.util.Calendar.YEAR)}"
    }
}

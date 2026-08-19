package com.monevix.app.ui.settlement

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monevix.app.data.settlement.SettlementEntity
import com.monevix.app.util.CurrencyFormatter
import com.monevix.app.util.PdfExportUtil
import java.text.SimpleDateFormat
import java.util.*

/**
 * El botón "Descargar PDF" genera la liquidación en PDF en el momento en que
 * el usuario lo pida, sin esperar a fin de mes.
 */
@Composable
fun SettlementDetailDialog(
    settlement: SettlementEntity,
    viewModel: SettlementViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val items by viewModel.observeItems(settlement.id).collectAsStateWithLifecycle(emptyList())
    val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es","CO")).format(Date(settlement.dateMillis))
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detalle de liquidación") },
        text = {
            Column {
                Text(settlement.companyName.ifBlank { "Monevix" }, style = MaterialTheme.typography.labelLarge)
                Text(settlement.workerName, style = MaterialTheme.typography.titleMedium)
                Text("$date · ${settlement.periodLabel}", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(12.dp))
                LazyColumn(Modifier.heightIn(max=280.dp)) {
                    items(items) { item ->
                        Column(Modifier.fillMaxWidth().padding(vertical=6.dp)) {
                            Text("${item.code} — ${item.name}")
                            Text(
                                "${item.quantity} × ${CurrencyFormatter.format(item.unitPrice)} = ${CurrencyFormatter.format(item.subtotal)}",
                                style=MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                HorizontalDivider(Modifier.padding(vertical=8.dp))
                Text("Total: ${CurrencyFormatter.format(settlement.total)}", style=MaterialTheme.typography.titleMedium)
            }
        },
        confirmButton = { TextButton(onClick=onDismiss) { Text("Cerrar") } },
        dismissButton = {
            TextButton(onClick = {
                val uri = PdfExportUtil.exportSettlement(context, settlement, items)
                PdfExportUtil.sharePdf(context, uri)
            }) {
                Icon(Icons.Filled.PictureAsPdf, contentDescription = null)
                Text("  Descargar PDF")
            }
        }
    )
}

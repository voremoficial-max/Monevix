package com.monevix.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.monevix.app.data.settlement.WorkerMonthlySummary
import com.monevix.app.data.settlement.WorkerMonthlyTrend
import com.monevix.app.navigation.MonevixDestinations
import com.monevix.app.reminder.NotificationHelper
import com.monevix.app.reminder.ReminderScheduler
import com.monevix.app.ui.calculator.CalculatorDetailScreen
import com.monevix.app.ui.calculator.CalculatorWorkerSelectScreen
import com.monevix.app.ui.settlement.SettlementHistoryScreen
import com.monevix.app.ui.payment.PaymentsScreen
import com.monevix.app.ui.summary.MonthlySummaryScreen
import com.monevix.app.ui.theme.MonevixTheme
import com.monevix.app.ui.worker.WorkerFormScreen
import com.monevix.app.ui.worker.WorkerHistoryScreen
import com.monevix.app.ui.worker.WorkerListScreen
import com.monevix.app.ui.worktype.WorkTypeFormScreen
import com.monevix.app.ui.worktype.WorkTypeListScreen
import com.monevix.app.util.DataBackupUtil
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        ReminderScheduler.scheduleNext(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.createChannel(this)
        setupMonthEndReminder()
        setContent {
            MonevixTheme {
                MonevixApp()
            }
        }
    }

    private fun setupMonthEndReminder() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (granted) {
                ReminderScheduler.scheduleNext(this)
            } else {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            ReminderScheduler.scheduleNext(this)
        }
    }
}

private data class SideMenuItem(
    val label: String,
    val icon: ImageVector,
    val destination: String
)

private val sideMenuItems = listOf(
    SideMenuItem("Calculadora", Icons.Filled.Calculate, MonevixDestinations.CALCULATOR_WORKER_SELECT),
    SideMenuItem("Crear personal", Icons.Filled.Groups, MonevixDestinations.WORKER_LIST),
    SideMenuItem("Crear trabajos", Icons.Filled.Work, MonevixDestinations.WORK_TYPE_LIST),
    SideMenuItem("Pagos", Icons.Filled.Payments, MonevixDestinations.PAYMENTS),
    SideMenuItem("Liquidaciones", Icons.Filled.ReceiptLong, MonevixDestinations.SETTLEMENT_HISTORY),
    SideMenuItem("Resumen mensual", Icons.Filled.QueryStats, MonevixDestinations.MONTHLY_SUMMARY),
    SideMenuItem("Ajustes", Icons.Filled.Settings, MonevixDestinations.SETTINGS)
)

@Composable
fun MonevixApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val preferences = remember {
        context.getSharedPreferences("monevix_preferences", android.content.Context.MODE_PRIVATE)
    }
    var userName by remember { mutableStateOf(preferences.getString("user_name", null).orEmpty()) }
    var showNameDialog by remember { mutableStateOf(userName.isBlank()) }

    if (showNameDialog) {
        NameDialog(
            onSave = { name ->
                val cleanName = name.trim()
                preferences.edit().putString("user_name", cleanName).apply()
                userName = cleanName
                showNameDialog = false
            }
        )
    }

    NavHost(
        navController = navController,
        startDestination = MonevixDestinations.PANEL,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(MonevixDestinations.PANEL) {
            MonevixShell(navController, userName) { openPanel ->
                DashboardScreen(userName, openPanel)
            }
        }
        composable(MonevixDestinations.WORKER_LIST) {
            WorkerListScreen(navController)
        }
        composable(MonevixDestinations.WORKER_FORM) {
            WorkerFormScreen(navController, workerId = null)
        }
        composable(MonevixDestinations.WORKER_FORM_WITH_ID) { entry ->
            WorkerFormScreen(
                navController,
                workerId = entry.arguments?.getString("workerId")?.toLongOrNull()
            )
        }
        composable(MonevixDestinations.WORKER_HISTORY) { entry ->
            WorkerHistoryScreen(
                navController,
                workerId = entry.arguments?.getString("workerId")?.toLongOrNull() ?: 0L
            )
        }
        composable(MonevixDestinations.WORK_TYPE_LIST) {
            WorkTypeListScreen(navController)
        }
        composable(MonevixDestinations.WORK_TYPE_FORM) {
            WorkTypeFormScreen(navController, workTypeId = null)
        }
        composable(MonevixDestinations.WORK_TYPE_FORM_WITH_ID) { entry ->
            WorkTypeFormScreen(
                navController,
                workTypeId = entry.arguments?.getString("workTypeId")?.toLongOrNull()
            )
        }
        composable(MonevixDestinations.CALCULATOR_WORKER_SELECT) {
            CalculatorWorkerSelectScreen(navController)
        }
        composable(MonevixDestinations.CALCULATOR_DETAIL) { entry ->
            CalculatorDetailScreen(
                navController,
                workerId = entry.arguments?.getString("workerId")?.toLongOrNull() ?: 0L
            )
        }
        composable(MonevixDestinations.PAYMENTS) {
            PaymentsScreen(navController)
        }
        composable(MonevixDestinations.SETTLEMENT_HISTORY) {
            SettlementHistoryScreen(navController)
        }
        composable(MonevixDestinations.MONTHLY_SUMMARY) {
            MonthlySummaryScreen(navController)
        }
        composable(MonevixDestinations.SETTINGS) {
            SettingsScreen(
                navController = navController,
                userName = userName,
                onUserNameChanged = { newName ->
                    preferences.edit().putString("user_name", newName.trim()).apply()
                    userName = newName.trim()
                }
            )
        }
    }
}

@Composable
private fun MonevixShell(
    navController: NavHostController,
    userName: String,
    content: @Composable (openPanel: () -> Unit) -> Unit
) {
    var panelOpen by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp)
        ) {
            content { panelOpen = true }
        }

        if (panelOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable { panelOpen = false }
            )
            SideBar(
                navController = navController,
                userName = userName,
                onClose = { panelOpen = false },
                modifier = Modifier.align(Alignment.CenterStart)
            )
        }
    }
}

@Composable
private fun SideBar(
    navController: NavHostController,
    userName: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(255.dp)
            .fillMaxHeight()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 14.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LogoMark(Modifier.size(48.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Monevix",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        "Panel administrativo",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                androidx.compose.material3.IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, contentDescription = "Cerrar panel")
                }
            }

            Spacer(Modifier.height(18.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(12.dp))

            NavigationDrawerItem(
                label = { Text("Inicio", fontWeight = FontWeight.SemiBold) },
                selected = true,
                onClick = {
                    navController.navigate(MonevixDestinations.PANEL) {
                        popUpTo(MonevixDestinations.PANEL) { inclusive = false }
                        launchSingleTop = true
                    }
                    onClose()
                },
                icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.padding(vertical = 3.dp)
            )

            Spacer(Modifier.height(8.dp))

            sideMenuItems.forEach { item ->
                NavigationDrawerItem(
                    label = { Text(item.label) },
                    selected = false,
                    onClick = {
                        navController.navigate(item.destination) {
                            launchSingleTop = true
                        }
                        onClose()
                    },
                    icon = { Icon(item.icon, contentDescription = null) },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = MaterialTheme.colorScheme.surface,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(vertical = 3.dp)
                )
            }

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Person, contentDescription = null)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Usuario", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = userName.ifBlank { "Usuario" },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardScreen(userName: String, openPanel: () -> Unit) {
    val app = LocalContext.current.applicationContext as MonevixApplication
    val viewModel: com.monevix.app.ui.summary.MonthlySummaryViewModel = viewModel(
        factory = com.monevix.app.ui.summary.MonthlySummaryViewModelFactory(app.settlementRepository)
    )
    val rows by viewModel.rows.collectAsStateWithLifecycle()
    val trend by app.settlementRepository.observeMonthlyTrend().collectAsStateWithLifecycle(initialValue = emptyList())
    val totalMonth = rows.sumOf { it.totalPaid }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.IconButton(
                        onClick = openPanel,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(
                            Icons.Filled.Menu,
                            contentDescription = "Abrir panel",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (userName.isNotBlank()) "Bienvenido, $userName" else "Bienvenido",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            "Aquí tienes una vista rápida de la actividad de Monevix.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                LogoMark(Modifier.size(72.dp))
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(Modifier.padding(22.dp)) {
                    Text("Resumen del mes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        formatMoney(totalMonth),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("Total pagado en el mes actual", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            MonthlyTrendChartCard(
                title = "Trabajos por trabajador",
                subtitle = "Los 3 trabajadores con más trabajos en los últimos 5 meses",
                trend = trend,
                value = { it.totalQuantity.toLong() },
                yAxisLabel = "Trabajos",
                valueFormatter = { "$it" }
            )
        }

        item {
            MonthlyTrendChartCard(
                title = "Ganancias por trabajador",
                subtitle = "Los 3 trabajadores con mayores ganancias en los últimos 5 meses",
                trend = trend,
                value = { it.totalPaid },
                yAxisLabel = "Ganancias",
                valueFormatter = ::formatMoney
            )
        }
    }
}

private data class ChartSeries(
    val workerId: Long,
    val workerName: String,
    val color: Color,
    val values: List<Long>
)

@Composable
private fun MonthlyTrendChartCard(
    title: String,
    subtitle: String,
    trend: List<WorkerMonthlyTrend>,
    value: (WorkerMonthlyTrend) -> Long,
    yAxisLabel: String,
    valueFormatter: (Long) -> String
) {
    val months = remember {
        val formatter = SimpleDateFormat("yyyy-MM", Locale.US)
        val calendar = Calendar.getInstance()
        (0 until 5).map {
            val key = formatter.format(calendar.time)
            calendar.add(Calendar.MONTH, -1)
            key
        }.reversed()
    }
    val monthLabels = remember(months) {
        months.map { key ->
            val parsed = SimpleDateFormat("yyyy-MM", Locale.US).parse(key)
            if (parsed == null) key else SimpleDateFormat("MMM", Locale("es", "CO")).format(parsed)
                .replaceFirstChar { it.titlecase(Locale("es", "CO")) }
        }
    }

    val byWorker = trend.groupBy { it.workerId }
    val topWorkerIds = byWorker.values
        .sortedByDescending { workerRows -> workerRows.sumOf { value(it) } }
        .take(3)
        .map { it.first().workerId }

    val palette = listOf(
        Color(0xFF66D17A),
        Color(0xFF5DA9FF),
        Color(0xFFFFB84D)
    )

    val series = topWorkerIds.mapIndexedNotNull { index, workerId ->
        val workerRows = byWorker[workerId].orEmpty()
        val workerName = workerRows.firstOrNull()?.workerName ?: return@mapIndexedNotNull null
        val values = months.map { month ->
            workerRows.firstOrNull { it.monthKey == month }?.let(value) ?: 0L
        }
        ChartSeries(workerId, workerName, palette[index], values)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))

            if (series.isEmpty()) {
                Text("Aún no hay datos para mostrar.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 18.dp))
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        yAxisLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(64.dp)
                    )
                    Column(Modifier.weight(1f)) {
                        MultiLineChart(
                            series = series,
                            monthLabels = monthLabels,
                            formatter = valueFormatter
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            monthLabels.forEach { label ->
                                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                series.forEachIndexed { index, item ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                        Box(Modifier.size(10.dp).clip(RoundedCornerShape(50)).background(item.color))
                        Spacer(Modifier.width(8.dp))
                        Text("${index + 1}. ${item.workerName}", fontWeight = FontWeight.Medium)
                        Spacer(Modifier.weight(1f))
                        Text(valueFormatter(item.values.lastOrNull() ?: 0L), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun MultiLineChart(
    series: List<ChartSeries>,
    monthLabels: List<String>,
    formatter: (Long) -> String
) {
    val maxValue = series.flatMap { it.values }.maxOrNull()?.coerceAtLeast(1L) ?: 1L
    val chartSurfaceColor = MaterialTheme.colorScheme.surface
    val labelPaint = remember {
        android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 28f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        val left = 8f
        val right = size.width - 8f
        val top = 18f
        val bottom = size.height - 10f
        val width = (right - left).coerceAtLeast(1f)
        val height = (bottom - top).coerceAtLeast(1f)
        val stepX = if (monthLabels.size <= 1) 0f else width / (monthLabels.size - 1)

        for (i in 0..4) {
            val y = top + height * i / 4f
            drawLine(
                color = Color.Gray.copy(alpha = 0.20f),
                start = androidx.compose.ui.geometry.Offset(left, y),
                end = androidx.compose.ui.geometry.Offset(right, y),
                strokeWidth = 1f
            )
        }

        drawLine(Color.Gray.copy(alpha = 0.45f), androidx.compose.ui.geometry.Offset(left, top), androidx.compose.ui.geometry.Offset(left, bottom), 2f)
        drawLine(Color.Gray.copy(alpha = 0.45f), androidx.compose.ui.geometry.Offset(left, bottom), androidx.compose.ui.geometry.Offset(right, bottom), 2f)

        drawIntoCanvas { canvas ->
            labelPaint.color = android.graphics.Color.LTGRAY
            labelPaint.textSize = 25f
            labelPaint.textAlign = android.graphics.Paint.Align.RIGHT
            for (i in 0..4) {
                val value = maxValue * (4 - i) / 4
                val y = top + height * i / 4f
                canvas.nativeCanvas.drawText(formatter(value), left - 6f, y + 8f, labelPaint)
            }
        }

        series.forEach { item ->
            val points = item.values.mapIndexed { index, value ->
                val x = left + stepX * index
                val y = bottom - (value.toFloat() / maxValue.toFloat()).coerceIn(0f, 1f) * height
                androidx.compose.ui.geometry.Offset(x, y)
            }
            points.zipWithNext().forEach { (a, b) ->
                drawLine(item.color, a, b, 5f)
            }
            points.forEach { point ->
                drawCircle(item.color, 6f, point)
                drawCircle(chartSurfaceColor, 2.5f, point)
            }
        }
    }
}


@Composable
private fun SettingsScreen(
    navController: NavHostController,
    userName: String,
    onUserNameChanged: (String) -> Unit
) {
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences("monevix_preferences", android.content.Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()
    var editedName by remember(userName) { mutableStateOf(userName) }
    var editedCompany by remember { mutableStateOf(preferences.getString("company_name", "").orEmpty()) }
    var paymentFrequency by remember { mutableStateOf(preferences.getString("payment_frequency", "WEEKLY").orEmpty()) }
    var status by remember { mutableStateOf<String?>(null) }
    var pendingImport by remember { mutableStateOf<String?>(null) }

    val exportLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching {
                    val json = DataBackupUtil.exportJson(context, userName)
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        output.writer(Charsets.UTF_8).use { it.write(json) }
                    } ?: error("No se pudo abrir el archivo de destino.")
                }.onSuccess {
                    status = "Datos exportados correctamente."
                }.onFailure {
                    status = "No se pudo exportar: ${it.message}"
                }
            }
        }
    }

    val importLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        input.reader(Charsets.UTF_8).readText()
                    } ?: error("No se pudo leer el archivo seleccionado.")
                }.onSuccess { pendingImport = it }
                    .onFailure { status = "No se pudo leer el respaldo: ${it.message}" }
            }
        }
    }

    if (pendingImport != null) {
        AlertDialog(
            onDismissRequest = { pendingImport = null },
            title = { Text("Importar datos") },
            text = {
                Text("La importación reemplazará los datos actuales de Monevix por los del respaldo. ¿Quieres continuar?")
            },
            confirmButton = {
                Button(onClick = {
                    val json = pendingImport ?: return@Button
                    pendingImport = null
                    scope.launch {
                        runCatching { DataBackupUtil.importJson(context, json) }
                            .onSuccess { importedName ->
                                if (importedName.isNotBlank()) {
                                    editedName = importedName
                                    onUserNameChanged(importedName)
                                }
                                status = "Datos importados correctamente."
                            }
                            .onFailure { status = "No se pudo importar: ${it.message}" }
                    }
                }) { Text("Importar") }
            },
            dismissButton = {
                TextButton(onClick = { pendingImport = null }) { Text("Cancelar") }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.material3.IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.width(8.dp))
            Column {
                Text("Ajustes", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                Text("Personaliza Monevix y protege tus datos.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Usuario administrador", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Nombre") },
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) }
                )
                Button(
                    onClick = {
                        val clean = editedName.trim()
                        if (clean.isNotEmpty()) {
                            onUserNameChanged(clean)
                            editedName = clean
                            status = "Nombre actualizado."
                        }
                    },
                    enabled = editedName.trim().isNotEmpty()
                ) { Text("Guardar nombre") }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Empresa y pagos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = editedCompany,
                    onValueChange = { editedCompany = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Nombre de la empresa") },
                    leadingIcon = { Icon(Icons.Filled.Groups, contentDescription = null) }
                )
                Text("Frecuencia de pago", style = MaterialTheme.typography.labelLarge)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { paymentFrequency = "WEEKLY" }) { Text("Semanal") }
                    OutlinedButton(onClick = { paymentFrequency = "BIWEEKLY" }) { Text("Quincenal") }
                    OutlinedButton(onClick = { paymentFrequency = "MONTHLY" }) { Text("Mensual") }
                }
                Text(
                    when (paymentFrequency) {
                        "BIWEEKLY" -> "Seleccionado: quincenal"
                        "MONTHLY" -> "Seleccionado: mensual"
                        else -> "Seleccionado: semanal"
                    },
                    color = MaterialTheme.colorScheme.primary
                )
                Button(onClick = {
                    preferences.edit()
                        .putString("company_name", editedCompany.trim())
                        .putString("payment_frequency", paymentFrequency)
                        .apply()
                    status = "Configuración guardada correctamente."
                }) { Text("Guardar configuración") }
                status?.let { message ->
                    Text(
                        message,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Copia de seguridad", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Exporta trabajadores, trabajos, liquidaciones y el nombre de usuario para llevarlos a otro celular.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = {
                        val safeName = userName.ifBlank { "monevix" }.replace(" ", "_")
                        exportLauncher.launch("monevix_$safeName.json")
                    }) { Text("Exportar datos") }
                    TextButton(onClick = {
                        importLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                    }) { Text("Importar datos") }
                }
            }
        }

            Spacer(Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(14.dp))
                Text(
                    "Creado by Vorem",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Versión 1.1 • Todos los derechos reservados © 2026 Vorem",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LogoMark(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "M",
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
private fun NameDialog(onSave: (String) -> Unit) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { },
        title = {
            Text("Bienvenido a Monevix", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text("Para personalizar el inicio, dime cómo quieres que te llamemos.")
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    label = { Text("Tu nombre") },
                    leadingIcon = {
                        Icon(Icons.Filled.Person, contentDescription = null)
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name) },
                enabled = name.trim().isNotEmpty()
            ) {
                Text("Continuar")
            }
        }
    )
}

private fun formatMoney(value: Long): String =
    "$" + "%,d".format(java.util.Locale("es", "CO"), value)

@Preview(showBackground = true)
@Composable
fun PanelPrincipalPreview() {
    MonevixTheme {
        DashboardScreen("Carlos", {})
    }
}

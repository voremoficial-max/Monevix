package com.monevix.app.ui.worker

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.monevix.app.ui.settlement.SettlementHistoryScreen

@Composable
fun WorkerHistoryScreen(navController: NavHostController, workerId: Long) {
    SettlementHistoryScreen(navController = navController, initialWorkerId = workerId)
}

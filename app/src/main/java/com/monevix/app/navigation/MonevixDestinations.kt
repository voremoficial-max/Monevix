package com.monevix.app.navigation

/**
 * Rutas de navegación de Monevix. Centralizarlas aquí evita strings sueltos
 * repetidos por las pantallas y facilita agregar nuevas rutas en las
 * siguientes fases (Trabajos, Calculadora, Liquidaciones, Historial, etc.).
 */
object MonevixDestinations {
    const val PANEL = "panel"
    const val WORKER_LIST = "worker_list"
    const val WORKER_FORM = "worker_form"
    const val WORKER_FORM_WITH_ID = "worker_form/{workerId}"
    const val WORKER_HISTORY = "worker_history/{workerId}"

    const val WORK_TYPE_LIST = "work_type_list"
    const val WORK_TYPE_FORM = "work_type_form"
    const val WORK_TYPE_FORM_WITH_ID = "work_type_form/{workTypeId}"

    const val CALCULATOR_WORKER_SELECT = "calculator"
    const val CALCULATOR_DETAIL = "calculator_detail/{workerId}"
    const val PAYMENTS = "payments"
    const val SETTLEMENT_HISTORY = "settlement_history"
    const val MONTHLY_SUMMARY = "monthly_summary"
    const val SETTINGS = "settings"

    fun workerForm(workerId: Long? = null): String =
        if (workerId == null) WORKER_FORM else "worker_form/$workerId"

    fun workerHistory(workerId: Long): String = "worker_history/$workerId"

    fun workTypeForm(workTypeId: Long? = null): String =
        if (workTypeId == null) WORK_TYPE_FORM else "work_type_form/$workTypeId"

    fun calculatorDetail(workerId: Long): String = "calculator_detail/$workerId"
}

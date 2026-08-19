package com.monevix.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.monevix.app.data.settlement.SettlementEntity
import com.monevix.app.data.settlement.SettlementItemEntity
import com.monevix.app.data.settlement.WorkerMonthlySummary
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Genera y comparte reportes en PDF usando únicamente APIs nativas de
 * Android (android.graphics.pdf.PdfDocument), sin dependencias externas.
 *
 * El PDF se puede generar en cualquier momento -no solo a fin de mes-: el
 * botón "Descargar PDF" está disponible en el detalle de cada liquidación y
 * en el resumen mensual, cuando el usuario lo pida. El recordatorio de fin
 * de mes (ver [ReminderScheduler]) es independiente: solo avisa que ya
 * terminó el mes, no genera el PDF automáticamente.
 *
 * El archivo se guarda en el caché privado de la app y se entrega mediante
 * [FileProvider] a través de un Intent para compartir/guardar (el usuario
 * elige dónde: Descargas, Drive, WhatsApp, imprimir, etc.), lo cual evita
 * pedir permisos de almacenamiento en cualquier versión de Android.
 */
object PdfExportUtil {

    private const val PAGE_WIDTH = 595 // A4 a 72dpi aprox.
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 40f

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CO"))

    fun exportSettlement(context: Context, settlement: SettlementEntity, items: List<SettlementItemEntity>): Uri {
        val document = PdfDocument()
        val page = document.startPage(
            PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        )
        val canvas = page.canvas
        var y = MARGIN

        y = drawTitle(canvas, "Liquidación de pago", y)
        y = drawSubtitle(canvas, "Monevix", y)
        y += 16f

        y = drawLine(canvas, "Trabajador: ${settlement.workerName}", y, bold = true)
        y = drawLine(canvas, "Fecha: ${dateFormat.format(Date(settlement.dateMillis))}", y)
        y = drawLine(canvas, "Periodo: ${settlement.periodLabel}", y)
        y += 12f
        y = drawDivider(canvas, y)
        y += 12f

        y = drawLine(canvas, "Trabajos realizados", y, bold = true)
        y += 6f
        items.forEach { item ->
            y = drawLine(canvas, "${item.code} — ${item.name}", y)
            y = drawLine(
                canvas,
                "  ${item.quantity} x ${CurrencyFormatter.format(item.unitPrice)} = ${CurrencyFormatter.format(item.subtotal)}",
                y
            )
        }
        y += 8f
        y = drawDivider(canvas, y)
        y += 16f
        drawLine(canvas, "TOTAL: ${CurrencyFormatter.format(settlement.total)}", y, bold = true, size = 16f)

        document.finishPage(page)
        val fileName = "liquidacion_${settlement.workerName.sanitize()}_${settlement.id}.pdf"
        return document.saveAndGetUri(context, fileName)
    }

    fun exportMonthlySummary(
        context: Context,
        periodLabel: String,
        rows: List<WorkerMonthlySummary>,
        totalGeneral: Long
    ): Uri {
        val document = PdfDocument()
        val page = document.startPage(
            PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        )
        val canvas = page.canvas
        var y = MARGIN

        y = drawTitle(canvas, "Resumen mensual", y)
        y = drawSubtitle(canvas, periodLabel, y)
        y += 16f
        y = drawDivider(canvas, y)
        y += 12f

        if (rows.isEmpty()) {
            drawLine(canvas, "No hay liquidaciones registradas en este mes.", y)
        } else {
            rows.forEach { row ->
                y = drawLine(canvas, row.workerName, y, bold = true)
                y = drawLine(
                    canvas,
                    "  ${row.settlementCount} liquidaciones · ${row.totalQuantity} unidades · ${CurrencyFormatter.format(row.totalPaid)}",
                    y
                )
            }
            y += 8f
            y = drawDivider(canvas, y)
            y += 16f
            drawLine(canvas, "TOTAL GENERAL: ${CurrencyFormatter.format(totalGeneral)}", y, bold = true, size = 16f)
        }

        document.finishPage(page)
        val fileName = "resumen_mensual_${periodLabel.sanitize()}.pdf"
        return document.saveAndGetUri(context, fileName)
    }

    /** Abre el selector del sistema para guardar/compartir el PDF ya generado. */
    fun sharePdf(context: Context, uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Descargar / compartir PDF"))
    }

    private fun PdfDocument.saveAndGetUri(context: Context, fileName: String): Uri {
        val dir = File(context.cacheDir, "pdfs").apply { mkdirs() }
        val file = File(dir, fileName)
        FileOutputStream(file).use { writeTo(it) }
        close()
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private fun String.sanitize(): String = replace(Regex("[^A-Za-z0-9_-]"), "_")

    private fun drawTitle(canvas: Canvas, text: String, y: Float): Float {
        val paint = Paint().apply {
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            color = android.graphics.Color.BLACK
        }
        canvas.drawText(text, MARGIN, y + paint.textSize, paint)
        return y + paint.textSize + 6f
    }

    private fun drawSubtitle(canvas: Canvas, text: String, y: Float): Float {
        val paint = Paint().apply {
            textSize = 13f
            color = android.graphics.Color.DKGRAY
        }
        canvas.drawText(text, MARGIN, y + paint.textSize, paint)
        return y + paint.textSize + 4f
    }

    private fun drawDivider(canvas: Canvas, y: Float): Float {
        val paint = Paint().apply { color = android.graphics.Color.LTGRAY; strokeWidth = 1f }
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, paint)
        return y
    }

    private fun drawLine(canvas: Canvas, text: String, y: Float, bold: Boolean = false, size: Float = 12f): Float {
        val paint = Paint().apply {
            textSize = size
            typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            color = android.graphics.Color.BLACK
        }
        canvas.drawText(text, MARGIN, y + paint.textSize, paint)
        return y + paint.textSize + 8f
    }
}

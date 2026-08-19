package com.monevix.app.util

import android.content.Context
import androidx.room.withTransaction
import com.monevix.app.data.AppDatabase
import com.monevix.app.data.settlement.SettlementEntity
import com.monevix.app.data.settlement.SettlementItemEntity
import com.monevix.app.data.worker.WorkerEntity
import com.monevix.app.data.worktype.WorkTypeEntity
import com.monevix.app.data.workentry.WorkEntryEntity
import com.monevix.app.data.payment.PaymentEntity
import com.monevix.app.data.payment.PaymentItemEntity
import org.json.JSONArray
import org.json.JSONObject

/** Exporta e importa los datos de Monevix en un JSON portable. */
object DataBackupUtil {
    private const val VERSION = 2

    suspend fun exportJson(context: Context, userName: String): String {
        val db = AppDatabase.getInstance(context)
        val root = JSONObject().apply {
            put("format", "monevix-backup")
            put("version", VERSION)
            put("userName", userName)
            put("companyName", context.getSharedPreferences("monevix_preferences", Context.MODE_PRIVATE).getString("company_name", "").orEmpty())
            put("paymentFrequency", context.getSharedPreferences("monevix_preferences", Context.MODE_PRIVATE).getString("payment_frequency", "WEEKLY").orEmpty())
            put("workers", JSONArray().apply {
                db.workerDao().getAll().forEach { put(workerToJson(it)) }
            })
            put("workTypes", JSONArray().apply {
                db.workTypeDao().getAll().forEach { put(workTypeToJson(it)) }
            })
            put("settlements", JSONArray().apply {
                db.settlementDao().getAll().forEach { put(settlementToJson(it)) }
            })
            put("settlementItems", JSONArray().apply {
                db.settlementDao().getAllItems().forEach { put(itemToJson(it)) }
            })
            put("workEntries", JSONArray().apply { db.workEntryDao().getAll().forEach { put(workEntryToJson(it)) } })
            put("payments", JSONArray().apply { db.paymentDao().getAll().forEach { put(paymentToJson(it)) } })
            put("paymentItems", JSONArray().apply { db.paymentDao().getAllItems().forEach { put(paymentItemToJson(it)) } })
        }
        return root.toString(2)
    }

    suspend fun importJson(context: Context, json: String): String {
        val root = JSONObject(json)
        require(root.optString("format") == "monevix-backup") { "El archivo no es un respaldo válido de Monevix." }
        require(root.optInt("version", 0) in 1..VERSION) { "La versión del respaldo no es compatible." }

        val workers = root.optJSONArray("workers").toEntities { workerFromJson(it) }
        val workTypes = root.optJSONArray("workTypes").toEntities { workTypeFromJson(it) }
        val settlements = root.optJSONArray("settlements").toEntities { settlementFromJson(it) }
        val items = root.optJSONArray("settlementItems").toEntities { itemFromJson(it) }
        val workEntries = root.optJSONArray("workEntries").toEntities { workEntryFromJson(it) }
        val payments = root.optJSONArray("payments").toEntities { paymentFromJson(it) }
        val paymentItems = root.optJSONArray("paymentItems").toEntities { paymentItemFromJson(it) }

        val db = AppDatabase.getInstance(context)
        db.withTransaction {
            db.paymentDao().deleteAllItems()
            db.workEntryDao().deleteAll()
            db.paymentDao().deleteAll()
            db.settlementDao().deleteAllItems()
            db.settlementDao().deleteAll()
            db.workTypeDao().deleteAll()
            db.workerDao().deleteAll()
            if (workers.isNotEmpty()) db.workerDao().insertAll(workers)
            if (workTypes.isNotEmpty()) db.workTypeDao().insertAll(workTypes)
            if (settlements.isNotEmpty()) db.settlementDao().insertAll(settlements)
            if (items.isNotEmpty()) db.settlementDao().insertItems(items)
            if (payments.isNotEmpty()) db.paymentDao().insertAll(payments)
            if (paymentItems.isNotEmpty()) db.paymentDao().insertItems(paymentItems)
            if (workEntries.isNotEmpty()) db.workEntryDao().insertAll(workEntries)
        }

        val prefs = context.getSharedPreferences("monevix_preferences", Context.MODE_PRIVATE)
        prefs.edit().putString("company_name", root.optString("companyName", "").trim()).putString("payment_frequency", root.optString("paymentFrequency", "WEEKLY")).apply()
        return root.optString("userName").trim()
    }

    private fun workerToJson(w: WorkerEntity) = JSONObject().apply {
        put("id", w.id); put("name", w.name); put("documentId", w.documentId ?: JSONObject.NULL)
        put("phone", w.phone ?: JSONObject.NULL); put("isActive", w.isActive); put("createdAt", w.createdAt)
    }
    private fun workTypeToJson(w: WorkTypeEntity) = JSONObject().apply {
        put("id", w.id); put("code", w.code); put("name", w.name); put("unitPrice", w.unitPrice)
        put("isActive", w.isActive); put("createdAt", w.createdAt)
    }
    private fun settlementToJson(s: SettlementEntity) = JSONObject().apply {
        put("id", s.id); put("workerId", s.workerId); put("workerName", s.workerName)
        put("companyName", s.companyName)
        put("dateMillis", s.dateMillis); put("periodLabel", s.periodLabel); put("total", s.total)
    }
    private fun itemToJson(i: SettlementItemEntity) = JSONObject().apply {
        put("id", i.id); put("settlementId", i.settlementId); put("workTypeId", i.workTypeId)
        put("code", i.code); put("name", i.name); put("quantity", i.quantity)
        put("unitPrice", i.unitPrice); put("subtotal", i.subtotal)
    }

    private fun workEntryToJson(e: WorkEntryEntity) = JSONObject().apply {
        put("id", e.id); put("workerId", e.workerId); put("workerName", e.workerName); put("dateMillis", e.dateMillis)
        put("workTypeId", e.workTypeId); put("code", e.code); put("name", e.name); put("quantity", e.quantity)
        put("unitPrice", e.unitPrice); put("subtotal", e.subtotal); put("paymentId", e.paymentId ?: JSONObject.NULL)
    }
    private fun paymentToJson(p: PaymentEntity) = JSONObject().apply {
        put("id", p.id); put("workerId", p.workerId); put("workerName", p.workerName); put("companyName", p.companyName)
        put("paidAtMillis", p.paidAtMillis); put("periodType", p.periodType); put("periodLabel", p.periodLabel); put("total", p.total)
    }
    private fun paymentItemToJson(i: PaymentItemEntity) = JSONObject().apply {
        put("id", i.id); put("paymentId", i.paymentId); put("workEntryId", i.workEntryId); put("dateMillis", i.dateMillis)
        put("code", i.code); put("name", i.name); put("quantity", i.quantity); put("unitPrice", i.unitPrice); put("subtotal", i.subtotal)
    }


    private fun workEntryFromJson(o: JSONObject) = WorkEntryEntity(
        id = o.getLong("id"), workerId = o.getLong("workerId"), workerName = o.getString("workerName"), dateMillis = o.getLong("dateMillis"),
        workTypeId = o.getLong("workTypeId"), code = o.getString("code"), name = o.getString("name"), quantity = o.getInt("quantity"),
        unitPrice = o.getLong("unitPrice"), subtotal = o.getLong("subtotal"), paymentId = if (o.isNull("paymentId")) null else o.optLong("paymentId")
    )
    private fun paymentFromJson(o: JSONObject) = PaymentEntity(
        id = o.getLong("id"), workerId = o.getLong("workerId"), workerName = o.getString("workerName"), companyName = o.optString("companyName", ""),
        paidAtMillis = o.getLong("paidAtMillis"), periodType = o.getString("periodType"), periodLabel = o.getString("periodLabel"), total = o.getLong("total")
    )
    private fun paymentItemFromJson(o: JSONObject) = PaymentItemEntity(
        id = o.getLong("id"), paymentId = o.getLong("paymentId"), workEntryId = o.getLong("workEntryId"), dateMillis = o.getLong("dateMillis"),
        code = o.getString("code"), name = o.getString("name"), quantity = o.getInt("quantity"), unitPrice = o.getLong("unitPrice"), subtotal = o.getLong("subtotal")
    )

    private fun workerFromJson(o: JSONObject) = WorkerEntity(
        id = o.getLong("id"), name = o.getString("name"),
        documentId = o.optNullableString("documentId"), phone = o.optNullableString("phone"),
        isActive = o.optBoolean("isActive", true), createdAt = o.optLong("createdAt", System.currentTimeMillis())
    )
    private fun workTypeFromJson(o: JSONObject) = WorkTypeEntity(
        id = o.getLong("id"), code = o.getString("code"), name = o.getString("name"),
        unitPrice = o.getLong("unitPrice"), isActive = o.optBoolean("isActive", true),
        createdAt = o.optLong("createdAt", System.currentTimeMillis())
    )
    private fun settlementFromJson(o: JSONObject) = SettlementEntity(
        id = o.getLong("id"), workerId = o.getLong("workerId"), workerName = o.getString("workerName"), companyName = o.optString("companyName", ""),
        dateMillis = o.getLong("dateMillis"), periodLabel = o.getString("periodLabel"), total = o.getLong("total")
    )
    private fun itemFromJson(o: JSONObject) = SettlementItemEntity(
        id = o.getLong("id"), settlementId = o.getLong("settlementId"), workTypeId = o.getLong("workTypeId"),
        code = o.getString("code"), name = o.getString("name"), quantity = o.getInt("quantity"),
        unitPrice = o.getLong("unitPrice"), subtotal = o.getLong("subtotal")
    )

    private fun JSONObject.optNullableString(key: String): String? =
        if (isNull(key)) null else optString(key).takeIf { it.isNotBlank() }

    private inline fun <T> JSONArray?.toEntities(factory: (JSONObject) -> T): List<T> {
        if (this == null) return emptyList()
        return buildList(length()) { for (i in 0 until length()) add(factory(getJSONObject(i))) }
    }
}

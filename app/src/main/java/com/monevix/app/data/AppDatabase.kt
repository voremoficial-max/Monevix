package com.monevix.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.monevix.app.data.worker.WorkerDao
import com.monevix.app.data.worker.WorkerEntity
import com.monevix.app.data.worktype.WorkTypeDao
import com.monevix.app.data.worktype.WorkTypeEntity
import com.monevix.app.data.settlement.SettlementDao
import com.monevix.app.data.settlement.SettlementEntity
import com.monevix.app.data.settlement.SettlementItemEntity

/**
 * Base de datos local de Monevix.
 *
 * En fases posteriores se agregarán aquí las entidades Settlement y
 * SettlementItem (Fases 4 y 5), junto con sus DAOs y las migraciones
 * correspondientes usando Room.Migration para no perder datos existentes.
 */
@Database(
    entities = [WorkerEntity::class, WorkTypeEntity::class, SettlementEntity::class, SettlementItemEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun workerDao(): WorkerDao

    abstract fun workTypeDao(): WorkTypeDao

    abstract fun settlementDao(): SettlementDao

    companion object {
        private const val DATABASE_NAME = "monevix_database"

        /**
         * FASE 3: agrega la tabla `work_types` con su índice único por código.
         * No modifica ni borra la tabla `workers` existente, así que los
         * trabajadores creados en la Fase 2 se conservan intactos.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `work_types` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `code` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `unitPrice` INTEGER NOT NULL,
                        `isActive` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_work_types_code` ON `work_types` (`code`)"
                )
            }
        }


        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `settlements` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `workerId` INTEGER NOT NULL,
                        `workerName` TEXT NOT NULL,
                        `dateMillis` INTEGER NOT NULL,
                        `periodLabel` TEXT NOT NULL,
                        `total` INTEGER NOT NULL,
                        FOREIGN KEY(`workerId`) REFERENCES `workers`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_settlements_workerId` ON `settlements` (`workerId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_settlements_dateMillis` ON `settlements` (`dateMillis`)")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `settlement_items` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `settlementId` INTEGER NOT NULL,
                        `workTypeId` INTEGER NOT NULL,
                        `code` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `quantity` INTEGER NOT NULL,
                        `unitPrice` INTEGER NOT NULL,
                        `subtotal` INTEGER NOT NULL,
                        FOREIGN KEY(`settlementId`) REFERENCES `settlements`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_settlement_items_settlementId` ON `settlement_items` (`settlementId`)")
            }
        }

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build().also { instance = it }
            }
        }
    }
}

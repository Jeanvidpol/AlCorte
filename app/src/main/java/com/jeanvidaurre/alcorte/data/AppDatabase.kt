package com.alcorte.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Card::class, Expense::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "al_corte.db"
                ).build().also { INSTANCE = it }
            }
    }
}

/**
 * Repository: única puerta de entrada a los datos para toda la app.
 * Equivalente a las funciones loadData/saveCards/saveExpenses del HTML.
 */
class Repository(private val db: AppDatabase) {
    val cards get() = db.cardDao().observeAll()
    val expenses get() = db.expenseDao().observeAll()

    suspend fun saveCard(card: Card) = db.cardDao().upsert(card)
    suspend fun deleteCard(card: Card) {
        db.expenseDao().deleteByCard(card.id) // igual que en el HTML: borrar tarjeta borra sus gastos
        db.cardDao().delete(card)
    }
    suspend fun saveExpense(expense: Expense) = db.expenseDao().upsert(expense)
    suspend fun deleteExpense(expense: Expense) = db.expenseDao().delete(expense)

    suspend fun deleteAllData() {
        db.expenseDao().deleteAll()
        db.cardDao().deleteAll()
    }

    /** Para exportar respaldo JSON (equivalente al botón "Exportar respaldo") */
    suspend fun exportSnapshot(): Pair<List<Card>, List<Expense>> =
        db.cardDao().getAllOnce() to db.expenseDao().getAllOnce()

    /** Para importar respaldo — reemplaza todo, igual que confirmamos en el HTML */
    suspend fun importSnapshot(cards: List<Card>, expenses: List<Expense>) {
        db.cardDao().deleteAll()
        db.expenseDao().deleteAll()
        cards.forEach { db.cardDao().upsert(it) }
        expenses.forEach { db.expenseDao().upsert(it) }
    }

    suspend fun insertCard(card: com.alcorte.app.data.Card) {
        db.cardDao().upsert(card)
    }
}

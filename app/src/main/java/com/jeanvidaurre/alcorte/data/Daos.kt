package com.alcorte.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Query("SELECT * FROM cards ORDER BY name")
    fun observeAll(): Flow<List<Card>>

    @Query("SELECT * FROM cards WHERE id = :id")
    suspend fun getById(id: String): Card?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(card: Card)

    @Delete
    suspend fun delete(card: Card)

    @Query("DELETE FROM cards")
    suspend fun deleteAll()

    @Query("SELECT * FROM cards")
    suspend fun getAllOnce(): List<Card>
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun observeAll(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE cardId = :cardId")
    suspend fun getByCard(cardId: String): List<Expense>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    @Query("DELETE FROM expenses WHERE cardId = :cardId")
    suspend fun deleteByCard(cardId: String)

    @Query("DELETE FROM expenses")
    suspend fun deleteAll()

    @Query("SELECT * FROM expenses")
    suspend fun getAllOnce(): List<Expense>
}

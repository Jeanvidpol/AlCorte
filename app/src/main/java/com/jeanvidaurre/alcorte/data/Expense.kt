package com.alcorte.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.UUID

enum class InterestType { SIN, CON }

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val cardId: String,
    val amountPerInstallment: Double,   // monto por cuota (lo que el usuario ingresa)
    val installments: Int = 1,
    val interestType: InterestType? = null, // null si installments == 1
    val category: String,
    val note: String = "",
    val date: LocalDate,                // fecha de la compra (hoy o pasado, nunca futuro)
    val firstCutoffDate: LocalDate       // el "nextCutoff" de la tarjeta al momento de crear el gasto
) {
    val totalAmount: Double get() = amountPerInstallment * installments
}

package com.alcorte.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class Currency { PEN, USD }

@Entity(tableName = "cards")
data class Card(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val last4: String? = null,
    val cutoffDay: Int,      // 1..31 — día de corte
    val dueDay: Int,         // 1..31 — día de pago
    val currency: Currency = Currency.PEN,
    val lastFourDigits: String? = null
)

package com.alcorte.app.data

import androidx.room.TypeConverter
import java.time.LocalDate

/** Room no sabe guardar LocalDate ni enums de forma nativa: los convertimos a texto plano. */
class Converters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString() // ISO-8601: "2026-09-06"

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromCurrency(c: Currency?): String? = c?.name

    @TypeConverter
    fun toCurrency(value: String?): Currency? = value?.let { Currency.valueOf(it) }

    @TypeConverter
    fun fromInterestType(t: InterestType?): String? = t?.name

    @TypeConverter
    fun toInterestType(value: String?): InterestType? = value?.let { InterestType.valueOf(it) }
}

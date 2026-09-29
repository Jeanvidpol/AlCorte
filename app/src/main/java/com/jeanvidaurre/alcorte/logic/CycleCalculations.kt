package com.alcorte.app.logic

import com.alcorte.app.data.Card
import com.alcorte.app.data.Expense
import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class CardStatus { OPTIMO, REGULAR, NO_RECOMENDADO }

data class CycleDates(
    val prevCutoff: LocalDate,
    val nextCutoff: LocalDate,
    val nextDue: LocalDate
)

data class CardStatusInfo(
    val status: CardStatus,
    val daysToPay: Int,       // días desde hoy hasta nextDue
    val daysSinceCutoff: Int, // días desde prevCutoff hasta hoy
    val dates: CycleDates
)

/**
 * Ajusta un día de mes al máximo real de ese mes (ej. día 31 en febrero -> 28).
 * Equivalente a clampDate() en el HTML.
 */
private fun clampDayInMonth(year: Int, month: Int, day: Int): LocalDate {
    val ym = java.time.YearMonth.of(year, month)
    return LocalDate.of(year, month, minOf(day, ym.lengthOfMonth()))
}

private fun clampToMonth(base: LocalDate, day: Int): LocalDate =
    clampDayInMonth(base.year, base.monthValue, day)

/**
 * Calcula el ciclo de facturación actual de una tarjeta respecto a una fecha dada.
 * Traducción directa de computeCycle() del HTML.
 */
fun computeCycle(card: Card, today: LocalDate): CycleDates {
    var prevCutoff = clampToMonth(today, card.cutoffDay)
    if (prevCutoff.isAfter(today)) prevCutoff = prevCutoff.minusMonths(1).let { clampDayInMonth(it.year, it.monthValue, card.cutoffDay) }

    val nextCutoff = prevCutoff.plusMonths(1).let { clampDayInMonth(it.year, it.monthValue, card.cutoffDay) }

    var nextDue = clampToMonth(nextCutoff, card.dueDay)
    if (!nextDue.isAfter(nextCutoff)) {
        nextDue = nextCutoff.plusMonths(1).let { clampDayInMonth(it.year, it.monthValue, card.dueDay) }
    }
    return CycleDates(prevCutoff, nextCutoff, nextDue)
}

/**
 * Estado de conveniencia de la tarjeta hoy: Óptimo / Regular / No recomendado.
 * Reglas acordadas: corte y corte+1 = No recomendado; corte+2 a corte+6 = Óptimo; resto = Regular.
 */
fun cardStatus(card: Card, today: LocalDate): CardStatusInfo {
    val dates = computeCycle(card, today)
    val daysSince = ChronoUnit.DAYS.between(dates.prevCutoff, today).toInt()
    val status = when {
        daysSince <= 1 -> CardStatus.NO_RECOMENDADO
        daysSince <= 6 -> CardStatus.OPTIMO
        else -> CardStatus.REGULAR
    }
    val daysToPay = ChronoUnit.DAYS.between(today, dates.nextDue).toInt()
    return CardStatusInfo(status, daysToPay, daysSince, dates)
}

/**
 * ¿La cuota de este gasto aplica al ciclo cuyo corte es targetNextCutoff?
 * Traducción directa de expenseAppliesToCycle() del HTML — así se reparten
 * las compras en cuotas entre los ciclos correspondientes.
 */
fun expenseAppliesToCycle(expense: Expense, targetNextCutoff: LocalDate): Boolean {
    val firstMonthIndex = expense.firstCutoffDate.year * 12 + expense.firstCutoffDate.monthValue
    val targetMonthIndex = targetNextCutoff.year * 12 + targetNextCutoff.monthValue
    val diff = targetMonthIndex - firstMonthIndex
    return diff in 0 until expense.installments
}

/** Consumo acumulado del ciclo ACTUAL de una tarjeta (no histórico infinito). */
fun cycleSpend(card: Card, expenses: List<Expense>, today: LocalDate): Double {
    val dates = computeCycle(card, today)
    return expenses
        .filter { it.cardId == card.id && expenseAppliesToCycle(it, dates.nextCutoff) }
        .sumOf { it.amountPerInstallment }
}

/**
 * Recomendación del día: entre las tarjetas en estado Óptimo, la de mayor plazo;
 * si ninguna está Óptima, la de mayor plazo entre todas (honesto, sin inventar "óptimo" falso).
 */
fun recommendedCard(cards: List<Card>, today: LocalDate): Pair<Card, CardStatusInfo>? {
    if (cards.isEmpty()) return null
    val withStatus = cards.map { it to cardStatus(it, today) }
    val optimal = withStatus.filter { it.second.status == CardStatus.OPTIMO }
    val pool = optimal.ifEmpty { withStatus }
    return pool.maxByOrNull { it.second.daysToPay }
}

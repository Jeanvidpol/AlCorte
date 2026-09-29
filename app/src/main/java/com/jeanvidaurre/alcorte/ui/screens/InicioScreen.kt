package com.jeanvidaurre.alcorte.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alcorte.app.data.Card
import com.alcorte.app.data.Expense
import com.alcorte.app.logic.computeCycle
import com.jeanvidaurre.alcorte.ui.theme.BackgroundCream
import com.jeanvidaurre.alcorte.ui.theme.JakartaFontFamily
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private val CardWhite = Color(0xFFFFFFFF)
private val LightInputBg = Color(0xFFEDF4F0)
private val BorderSubtle = Color(0xFFE4EDE7)
private val DarkHeader = Color(0xFF1F2D26)
private val TextMuted = Color(0xFF6B8074)
private val TerracottaPrimaryColor = Color(0xFF9E3E26)

private val GoldenWindowBg = Color(0xFFEBF7F0)
private val GoldenWindowBorder = Color(0xFFBFE5D1)
private val GoldenIconBg = Color(0xFF1B6E4A)
private val GoldenText = Color(0xFF16593C)

private val BlueBadgeBg = Color(0xFFE0F0F7)
private val BlueBadgeText = Color(0xFF2C7A9E)
private val AlertBadgeBg = Color(0xFFFDE8E8)
private val AlertBadgeText = Color(0xFFB9442C)

@Composable
fun InicioScreen(
    cards: List<Card>,
    expenses: List<Expense>,
    onGoAddCard: () -> Unit = {},
    onGoToHistory: () -> Unit = {}
) {
    val today = LocalDate.now()
    val currentMonth = YearMonth.now()
    val esFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM", Locale.forLanguageTag("es-PE"))

    val currentMonthExpenses = remember(expenses, currentMonth) {
        expenses.filter { YearMonth.from(it.date) == currentMonth }
    }

    val cardMap = remember(cards) { cards.associateBy { it.id } }

    val totalPen = remember(currentMonthExpenses, cardMap) {
        currentMonthExpenses
            .filter { cardMap[it.cardId]?.currency?.name?.contains("USD") != true }
            .sumOf { it.totalAmount }
    }

    val totalUsd = remember(currentMonthExpenses, cardMap) {
        currentMonthExpenses
            .filter { cardMap[it.cardId]?.currency?.name?.contains("USD") == true }
            .sumOf { it.totalAmount }
    }

    val bestCardToUse = remember(cards, expenses, today) {
        if (cards.isEmpty()) null
        else {
            cards.map { card ->
                val cycle = computeCycle(card, today)

                val currentMonthCutoff = LocalDate.of(today.year, today.month, card.cutoffDay.coerceAtMost(today.lengthOfMonth()))
                val lastCutoff = if (today.isBefore(currentMonthCutoff) || today.isEqual(currentMonthCutoff)) {
                    currentMonthCutoff.minusMonths(1)
                } else {
                    currentMonthCutoff
                }

                val daysSinceLastCutoff = ChronoUnit.DAYS.between(lastCutoff, today).toInt()
                val isSafeFromCutoffRisk = daysSinceLastCutoff >= 1

                val totalDaysOfFinancing = ChronoUnit.DAYS.between(today, cycle.nextDue).toInt()

                val cycleSpent = expenses
                    .filter { it.cardId == card.id && it.firstCutoffDate == cycle.nextCutoff }
                    .sumOf { it.totalAmount }

                val effectiveScore = if (isSafeFromCutoffRisk) totalDaysOfFinancing else -999

                Triple(card, effectiveScore, cycleSpent)
            }.sortedWith(
                compareByDescending<Triple<Card, Int, Double>> { it.second }
                    .thenBy { it.third }
            ).firstOrNull()?.takeIf { it.second > 0 }?.first
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundCream)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (cards.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(BorderSubtle))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = TerracottaPrimaryColor,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        "Comienza agregando tu primera tarjeta",
                        fontFamily = JakartaFontFamily,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkHeader,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Configura el día de corte y vencimiento para controlar tus ciclos.",
                        fontFamily = JakartaFontFamily,
                        fontSize = 12.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = onGoAddCard,
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimaryColor),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Agregar tarjeta", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            bestCardToUse?.let { card ->
                val cycle = computeCycle(card, today)
                val daysToCutoff = ChronoUnit.DAYS.between(today, cycle.nextCutoff).toInt()
                val totalDaysOfFinancing = ChronoUnit.DAYS.between(today, cycle.nextDue).toInt()
                val isOptimal = daysToCutoff >= 18

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isOptimal) GoldenWindowBg else LightInputBg),
                    border = BorderStroke(1.2.dp, if (isOptimal) GoldenWindowBorder else BorderSubtle)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (isOptimal) GoldenIconBg else TerracottaPrimaryColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isOptimal) Icons.Default.CheckCircle else Icons.Default.Stars,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isOptimal) "¡MOMENTO IDEAL PARA COMPRAR!" else "TARJETA SUGERIDA HOY",
                                    fontFamily = JakartaFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOptimal) GoldenText else TerracottaPrimaryColor,
                                    letterSpacing = 0.8.sp
                                )
                                Text(
                                    text = "Usa tu ${card.name}",
                                    fontFamily = JakartaFontFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkHeader
                                )
                            }
                        }

                        Text(
                            text = buildAnnotatedString {
                                append("Su ciclo cerró recientemente. Al comprar hoy, esta compra no cerrará hasta el ")
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = DarkHeader)) {
                                    append(cycle.nextCutoff.format(esFormatter))
                                }
                                append(" y pagarás recién el ")
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = if (isOptimal) GoldenText else TerracottaPrimaryColor)) {
                                    append(cycle.nextDue.format(esFormatter))
                                }
                                append(" (~$totalDaysOfFinancing días de financiamiento sin interés).")
                            },
                            fontFamily = JakartaFontFamily,
                            fontSize = 12.sp,
                            color = DarkHeader.copy(alpha = 0.85f),
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(BorderSubtle))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = TerracottaPrimaryColor,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Ciclos y vencimientos próximos",
                                fontFamily = JakartaFontFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkHeader
                            )
                        }
                        Text(
                            "${cards.size} tarjeta${if (cards.size != 1) "s" else ""}",
                            fontFamily = JakartaFontFamily,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaPrimaryColor
                        )
                    }

                    cards.forEach { card ->
                        val cycle = computeCycle(card, today)
                        val daysUntilCutoff = ChronoUnit.DAYS.between(today, cycle.nextCutoff).toInt()
                        val cardCycleExpenses = expenses.filter { it.cardId == card.id && it.firstCutoffDate == cycle.nextCutoff }
                        val cycleAmount = cardCycleExpenses.sumOf { it.totalAmount }
                        val currencySymbol = if (card.currency.name.contains("USD")) "$" else "S/"
                        val digits = card.lastFourDigits ?: card.last4

                        val (badgeLabel, badgeBg, badgeTextColor) = when {
                            daysUntilCutoff == 0 -> Triple("Cierra hoy", AlertBadgeBg, AlertBadgeText)
                            daysUntilCutoff == 1 -> Triple("Cierra mañana", AlertBadgeBg, AlertBadgeText)
                            daysUntilCutoff in 2..3 -> Triple("Cierra en $daysUntilCutoff días", AlertBadgeBg, AlertBadgeText)
                            daysUntilCutoff >= 18 -> Triple("Ciclo nuevo · Corta en $daysUntilCutoff días", GoldenWindowBg, GoldenText)
                            else -> Triple("Corta en $daysUntilCutoff días", BlueBadgeBg, BlueBadgeText)
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = LightInputBg,
                            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(BorderSubtle)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.AccessTime,
                                            contentDescription = null,
                                            tint = TextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                card.name,
                                                fontFamily = JakartaFontFamily,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DarkHeader
                                            )
                                            if (!digits.isNullOrBlank()) {
                                                Text(
                                                    "•••• $digits",
                                                    fontFamily = JakartaFontFamily,
                                                    fontSize = 10.sp,
                                                    color = TextMuted
                                                )
                                            }
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = badgeBg
                                    ) {
                                        Text(
                                            text = badgeLabel,
                                            fontFamily = JakartaFontFamily,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = badgeTextColor,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Column {
                                        Text("CONSUMO DEL CICLO", fontFamily = JakartaFontFamily, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                        Text(
                                            "$currencySymbol ${String.format(Locale.US, "%.2f", cycleAmount)}",
                                            fontFamily = JakartaFontFamily,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TerracottaPrimaryColor
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("PAGO LÍMITE", fontFamily = JakartaFontFamily, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                        Text(
                                            cycle.nextDue.format(esFormatter),
                                            fontFamily = JakartaFontFamily,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = DarkHeader
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(BorderSubtle))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ReceiptLong,
                        contentDescription = null,
                        tint = DarkHeader,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Resumen de consumos del mes",
                        fontFamily = JakartaFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkHeader
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = LightInputBg,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("SOLES (PEN)", fontFamily = JakartaFontFamily, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                            Text("Gasto del mes", fontFamily = JakartaFontFamily, fontSize = 11.sp, color = TextMuted)
                            Text(
                                "S/ ${String.format(Locale.US, "%.2f", totalPen)}",
                                fontFamily = JakartaFontFamily,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkHeader
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = LightInputBg,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("DÓLARES (USD)", fontFamily = JakartaFontFamily, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                            Text("Gasto del mes", fontFamily = JakartaFontFamily, fontSize = 11.sp, color = TextMuted)
                            Text(
                                "$ ${String.format(Locale.US, "%.2f", totalUsd)}",
                                fontFamily = JakartaFontFamily,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkHeader
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = LightInputBg,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onGoToHistory() }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 11.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ReceiptLong,
                            contentDescription = null,
                            tint = TerracottaPrimaryColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "VER TODOS LOS GASTOS EN HISTORIAL",
                            fontFamily = JakartaFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaPrimaryColor,
                            letterSpacing = 0.4.sp
                        )
                    }
                }
            }
        }
    }
}
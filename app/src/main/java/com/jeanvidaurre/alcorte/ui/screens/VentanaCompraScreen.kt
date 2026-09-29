package com.jeanvidaurre.alcorte.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alcorte.app.data.Card
import com.alcorte.app.data.Expense
import com.alcorte.app.logic.computeCycle
import com.jeanvidaurre.alcorte.ui.theme.BackgroundCream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private val CardWhite = Color(0xFFFFFFFF)
private val LightInputBg = Color(0xFFEDF4F0)
private val BorderSubtle = Color(0xFFE4EDE7)
private val DarkHeader = Color(0xFF1F2D26)
private val TextMuted = Color(0xFF6B8074)
private val TerracottaPrimary = Color(0xFF9E3E26)

private val RedBadgeBg = Color(0xFF9E3E26)
private val RedCardBg = Color(0xFFFDECEB)
private val RedCardText = Color(0xFF7A2012)

private val YellowBadgeBg = Color(0xFFB57C1E)
private val YellowCardBg = Color(0xFFFEF8E7)
private val YellowCardText = Color(0xFF7D500B)

private val GreenBadgeBg = Color(0xFF1B6E4A)
private val GreenCardBg = Color(0xFFE6F4EA)
private val GreenCardText = Color(0xFF0D4A30)

private val GreenAltCardBg = Color(0xFFEFF7F2)

enum class WindowStatus {
    NO_RECOMENDADO,
    REGULAR,
    RECOMENDADO
}

@Composable
fun VentanaCompraScreen(
    card: Card,
    allCards: List<Card> = emptyList(),
    expenses: List<Expense> = emptyList(),
    onBack: () -> Unit,
    onRegisterExpense: (Card) -> Unit
) {
    val today = LocalDate.now()
    val esFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM", Locale.forLanguageTag("es-PE"))
    val esShortFormatter = DateTimeFormatter.ofPattern("d 'de' MMM", Locale.forLanguageTag("es-PE"))
    val cycle = computeCycle(card, today)

    val daysUntilCutoff = ChronoUnit.DAYS.between(today, cycle.nextCutoff).toInt()
    val daysUntilDue = ChronoUnit.DAYS.between(today, cycle.nextDue).toInt()
    val prevCutoff = cycle.nextCutoff.minusMonths(1)

    val status = remember(daysUntilCutoff, today, card) {
        when {
            daysUntilCutoff <= 2 || today.dayOfMonth == card.cutoffDay -> WindowStatus.NO_RECOMENDADO
            daysUntilCutoff > 20 -> WindowStatus.RECOMENDADO
            else -> WindowStatus.REGULAR
        }
    }

    val cardExpenses = remember(expenses, card) { expenses.filter { it.cardId == card.id } }

    val (expensesThisBilling, expensesNextBilling) = remember(cardExpenses, cycle.nextCutoff) {
        cardExpenses.partition { exp ->
            !exp.date.isAfter(cycle.nextCutoff)
        }
    }

    val totalThisBilling = expensesThisBilling.sumOf { it.totalAmount }
    val totalNextBilling = expensesNextBilling.sumOf { it.totalAmount }
    val currencySymbol = if (card.currency.name.contains("USD")) "$" else "S/"

    val otherCardsRanked = remember(allCards, card, today) {
        allCards.filter { it.id != card.id }
            .map { other ->
                val otherCycle = computeCycle(other, today)
                val otherDaysToDue = ChronoUnit.DAYS.between(today, otherCycle.nextDue).toInt()
                val otherDaysToCutoff = ChronoUnit.DAYS.between(today, otherCycle.nextCutoff).toInt()
                Triple(other, otherDaysToDue, otherDaysToCutoff)
            }
            .sortedByDescending { it.second }
    }

    val bestAlternative = otherCardsRanked.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundCream)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable { onBack() }
                .padding(vertical = 4.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = DarkHeader,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Volver al inicio", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = DarkHeader)
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Detalle de ventana de compra", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DarkHeader)
            Text("Cálculo según la fecha de hoy", fontSize = 12.sp, color = TextMuted)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = LightInputBg),
            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(BorderSubtle))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("TARJETA EVALUADA", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 0.5.sp)
                    Spacer(Modifier.height(2.dp))
                    Text("${card.name} (•••• 5612)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkHeader)
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = CardWhite,
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(BorderSubtle))
                ) {
                    Text(
                        if (card.currency.name.contains("USD")) "USD · Dólares" else "PEN · Soles",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
        }

        when (status) {
            WindowStatus.NO_RECOMENDADO -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = RedCardBg),
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(RedBadgeBg.copy(alpha = 0.2f)))
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(shape = RoundedCornerShape(4.dp), color = RedBadgeBg) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("NO RECOMENDADO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 0.5.sp)
                            }
                        }
                        Text(
                            text = if (today.dayOfMonth == card.cutoffDay) {
                                "Hoy es exactamente el día de corte de esta tarjeta. Las compras realizadas hoy pueden ingresar en el corte que cierra en horas y requerirán pago en el plazo más corto."
                            } else {
                                "El ciclo de esta tarjeta cierra en apenas $daysUntilCutoff días. Realizar compras ahora te dará muy pocos días de financiamiento antes de la fecha de pago."
                            },
                            fontSize = 12.sp,
                            color = RedCardText,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
            WindowStatus.REGULAR -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = YellowCardBg),
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(YellowBadgeBg.copy(alpha = 0.2f)))
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(shape = RoundedCornerShape(4.dp), color = YellowBadgeBg) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("COMPRA REGULAR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 0.5.sp)
                            }
                        }
                        Text(
                            text = "Día intermedio del período de facturación. Tu consumo entra al ciclo activo con un plazo de pago razonable.",
                            fontSize = 12.sp,
                            color = YellowCardText,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
            WindowStatus.RECOMENDADO -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = GreenCardBg),
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(GreenBadgeBg.copy(alpha = 0.2f)))
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(shape = RoundedCornerShape(4.dp), color = GreenBadgeBg) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("VENTANA ÓPTIMA · RECOMENDADO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 0.5.sp)
                            }
                        }
                        Text(
                            text = "¡Excelente momento para comprar! Tu tarjeta acaba de cortar recientemente, otorgándote el plazo máximo de financiamiento libre de intereses.",
                            fontSize = 12.sp,
                            color = GreenCardText,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(BorderSubtle))
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("PLAZO RESULTANTE DE FINANCIAMIENTO", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 0.5.sp)
                Text(
                    text = "Comprando hoy, tienes\nhasta $daysUntilDue días para pagar",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TerracottaPrimary,
                    lineHeight = 27.sp
                )
                Surface(shape = RoundedCornerShape(6.dp), color = LightInputBg, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Comprando hoy (${today.format(esFormatter)}), tu consumo entra en el corte del ${cycle.nextCutoff.format(esFormatter)} y pagarías el ${cycle.nextDue.format(esFormatter)}.",
                        fontSize = 12.sp,
                        color = TextMuted,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(BorderSubtle))
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("CONTROL DE FACTURACIÓN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DarkHeader, letterSpacing = 0.5.sp)
                    Text("Corte: ${cycle.nextCutoff.format(esShortFormatter)}", fontSize = 11.sp, color = TerracottaPrimary, fontWeight = FontWeight.Bold)
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(7.dp).clip(RoundedCornerShape(3.5.dp)).background(TerracottaPrimary))
                            Spacer(Modifier.width(6.dp))
                            Text("Entran en esta facturación (Pagas el ${cycle.nextDue.format(esShortFormatter)})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkHeader)
                        }
                        Text("$currencySymbol ${String.format(Locale.US, "%.2f", totalThisBilling)}", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = TerracottaPrimary)
                    }

                    if (expensesThisBilling.isEmpty()) {
                        Surface(shape = RoundedCornerShape(6.dp), color = LightInputBg, modifier = Modifier.fillMaxWidth()) {
                            Text("No hay compras registradas antes de este corte.", fontSize = 11.sp, color = TextMuted, modifier = Modifier.padding(8.dp))
                        }
                    } else {
                        expensesThisBilling.forEach { exp ->
                            BillingMovementRow(exp = exp, currencySymbol = currencySymbol, esFormatter = esShortFormatter, isCurrentCycle = true)
                        }
                    }
                }

                HorizontalDivider(color = BorderSubtle, thickness = 0.8.dp)

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(7.dp).clip(RoundedCornerShape(3.5.dp)).background(GreenBadgeBg))
                            Spacer(Modifier.width(6.dp))
                            Text("Pasan al siguiente ciclo (+30 días de plazo)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GreenCardText)
                        }
                        Text("$currencySymbol ${String.format(Locale.US, "%.2f", totalNextBilling)}", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = GreenCardText)
                    }

                    if (expensesNextBilling.isEmpty()) {
                        Surface(shape = RoundedCornerShape(6.dp), color = LightInputBg, modifier = Modifier.fillMaxWidth()) {
                            Text("Aún no tienes compras que pasen al siguiente período.", fontSize = 11.sp, color = TextMuted, modifier = Modifier.padding(8.dp))
                        }
                    } else {
                        expensesNextBilling.forEach { exp ->
                            BillingMovementRow(exp = exp, currencySymbol = currencySymbol, esFormatter = esShortFormatter, isCurrentCycle = false)
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(BorderSubtle))
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("CALENDARIO DE FACTURACIÓN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DarkHeader, letterSpacing = 0.5.sp)
                    Text("Base: ${today.format(esFormatter)}", fontSize = 10.5.sp, color = TextMuted)
                }

                DesgloseItem(
                    icon = Icons.Default.History,
                    iconBg = LightInputBg,
                    title = "CORTE ANTERIOR",
                    dateText = prevCutoff.format(esFormatter),
                    subText = "Ciclo previo liquidado"
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (status == WindowStatus.NO_RECOMENDADO) RedCardBg else LightInputBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (status == WindowStatus.NO_RECOMENDADO) RedBadgeBg else DarkHeader),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("FECHA DE HOY", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                if (status == WindowStatus.NO_RECOMENDADO) {
                                    Spacer(Modifier.width(6.dp))
                                    Surface(shape = RoundedCornerShape(3.dp), color = RedBadgeBg.copy(alpha = 0.15f)) {
                                        Text("Cierre de ciclo", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = RedBadgeBg, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                    }
                                }
                            }
                            Text(today.format(esFormatter), fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = DarkHeader)
                            Text(
                                if (status == WindowStatus.NO_RECOMENDADO) "Día de corte de la tarjeta. Las transacciones pasan al balance emitido hoy."
                                else "Día en curso del período de facturación activo.",
                                fontSize = 11.sp,
                                color = TextMuted,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                DesgloseItem(
                    icon = Icons.Default.EventRepeat,
                    iconBg = LightInputBg,
                    title = "PRÓXIMO CORTE",
                    dateText = if (today == cycle.nextCutoff) "Hoy, ${cycle.nextCutoff.format(esFormatter)}" else cycle.nextCutoff.format(esFormatter),
                    subText = "Las compras después de hoy pasarán al ciclo del ${cycle.nextCutoff.plusMonths(1).format(esFormatter)}."
                )

                DesgloseItem(
                    icon = Icons.Default.CalendarToday,
                    iconBg = LightInputBg,
                    title = "FECHA LÍMITE DE PAGO ESTIMADA",
                    dateText = cycle.nextDue.format(esFormatter),
                    subText = "Vencimiento estimado del ciclo"
                )
            }
        }

        if (otherCardsRanked.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = GreenAltCardBg),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(BorderSubtle))
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CreditCard, contentDescription = null, tint = GreenCardText, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("ALTERNATIVAS CONVENIENTES EN TU BILLETERA", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = GreenCardText, letterSpacing = 0.4.sp)
                    }

                    Text("Otras tarjetas registradas con ventanas de financiamiento más amplias según la fecha de hoy:", fontSize = 11.5.sp, color = TextMuted, lineHeight = 15.sp)

                    otherCardsRanked.forEachIndexed { index, (altCard, altDaysDue, altDaysCutoff) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CardWhite,
                            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(BorderSubtle)),
                            modifier = Modifier.fillMaxWidth().clickable { onRegisterExpense(altCard) }
                        ) {
                            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(altCard.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkHeader)
                                        Spacer(Modifier.width(6.dp))
                                        Text("(•••• 5612)", fontSize = 10.5.sp, color = TextMuted)
                                    }
                                    val cutoffAltText = if (altDaysCutoff == 1) "Corte en 1 día" else "Corte en $altDaysCutoff días"
                                    Text(cutoffAltText, fontSize = 11.sp, color = TextMuted)
                                    if (index == 0) {
                                        Text("OPCIÓN MÁS CONVENIENTE HOY", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = GreenCardText)
                                    }
                                }

                                Surface(shape = RoundedCornerShape(6.dp), color = LightInputBg) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                        Text("$altDaysDue días", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GreenCardText)
                                        Text("HASTA EL PAGO", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (status == WindowStatus.NO_RECOMENDADO && bestAlternative != null) {
                Button(
                    onClick = { onRegisterExpense(bestAlternative.first) },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenBadgeBg),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Usar ${bestAlternative.first.name} en su lugar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = RedCardBg,
                    modifier = Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(8.dp)).clickable { onRegisterExpense(card) }
                ) {
                    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = RedBadgeBg, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Registrar gasto con ${card.name} de todos modos", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RedBadgeBg)
                    }
                }
            } else {
                Button(
                    onClick = { onRegisterExpense(card) },
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Registrar gasto con ${card.name}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                }
            }

            Text(
                text = "Volver al inicio",
                fontSize = 13.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().clickable { onBack() }
            )
        }
    }
}

@Composable
private fun BillingMovementRow(
    exp: Expense,
    currencySymbol: String,
    esFormatter: DateTimeFormatter,
    isCurrentCycle: Boolean
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isCurrentCycle) LightInputBg else GreenCardBg.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(exp.note.ifBlank { exp.category }, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkHeader)
                Text(
                    text = "${exp.date.format(esFormatter)} · ${if (isCurrentCycle) "Cierra en días" else "Próximo mes"}",
                    fontSize = 10.5.sp,
                    color = TextMuted
                )
            }

            Text(
                text = "$currencySymbol ${String.format(Locale.US, "%.2f", exp.totalAmount)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCurrentCycle) DarkHeader else GreenCardText
            )
        }
    }
}

@Composable
private fun DesgloseItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    title: String,
    dateText: String,
    subText: String
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(6.dp)).background(iconBg), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(title, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = TextMuted)
            Text(dateText, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = DarkHeader)
            Text(subText, fontSize = 11.sp, color = TextMuted)
        }
    }
}
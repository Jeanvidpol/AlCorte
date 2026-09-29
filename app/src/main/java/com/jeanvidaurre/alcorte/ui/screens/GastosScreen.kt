package com.jeanvidaurre.alcorte.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alcorte.app.data.Card
import com.alcorte.app.data.Expense
import com.alcorte.app.data.InterestType
import com.alcorte.app.logic.computeCycle
import com.jeanvidaurre.alcorte.ui.theme.BackgroundCream
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private val CardBackground = Color(0xFFFFFFFF)
private val LightInputBg = Color(0xFFEDF4F0)
private val CardBorderColor = Color(0xFFE4EDE7)
private val DarkSlate = Color(0xFF1E2D27)
private val MutedSlate = Color(0xFF6B8074)
private val TerracottaBtn = Color(0xFF9E3E26)
private val GreenActive = Color(0xFF1B6E4A)

private val BadgeOptimoBg = Color(0xFFD4EFE1)
private val BadgeOptimoText = Color(0xFF1B6E4A)
private val BadgeRegularBg = Color(0xFFE0F0F7)
private val BadgeRegularText = Color(0xFF2C7A9E)
private val BadgeCerrandoBg = Color(0xFFFDEBD0)
private val BadgeCerrandoText = Color(0xFFB95A00)

data class CategoryOption(val name: String, val icon: ImageVector)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GastosScreen(
    cards: List<Card>,
    onSaveExpense: (Expense) -> Unit,
    onNavigateToCards: () -> Unit
) {
    var amountText by remember { mutableStateOf("500") }
    var selectedInstallments by remember { mutableIntStateOf(3) }
    var isInterestFree by remember { mutableStateOf(false) }
    var exactMonthlyQuotaText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Supermercado") }
    var merchant by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var selectedCardId by remember { mutableStateOf(cards.firstOrNull()?.id ?: "") }

    LaunchedEffect(cards) {
        if (selectedCardId.isEmpty() && cards.isNotEmpty()) {
            selectedCardId = cards.first().id
        }
    }

    val selectedCard = cards.find { it.id == selectedCardId } ?: cards.firstOrNull()

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateDisplay = selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    val esFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM", Locale.forLanguageTag("es-PE"))

    val isUsd = selectedCard?.currency?.name?.contains("USD") == true
    val currencySymbol = if (isUsd) "$" else "S/"
    val currencyCode = if (isUsd) "USD" else "PEN"

    val categories = listOf(
        CategoryOption("Supermercado", Icons.Default.ShoppingCart),
        CategoryOption("Restaurantes", Icons.Default.Restaurant),
        CategoryOption("Servicios", Icons.Default.Bolt),
        CategoryOption("Transporte", Icons.Default.DirectionsCar),
        CategoryOption("Salud", Icons.Default.FavoriteBorder),
        CategoryOption("Entretenimiento", Icons.Default.Movie),
        CategoryOption("Educación", Icons.Default.School),
        CategoryOption("Otros", Icons.Default.Category)
    )

    val currentTotalAmount = amountText.toDoubleOrNull() ?: 0.0
    val exactQuotaInput = exactMonthlyQuotaText.toDoubleOrNull()

    val quotaAmount = if (!isInterestFree && selectedInstallments > 1 && exactQuotaInput != null && exactQuotaInput > 0) {
        exactQuotaInput
    } else {
        if (selectedInstallments > 0) currentTotalAmount / selectedInstallments else currentTotalAmount
    }

    val effectiveTotal = if (!isInterestFree && selectedInstallments > 1 && exactQuotaInput != null && exactQuotaInput > 0) {
        exactQuotaInput * selectedInstallments
    } else {
        currentTotalAmount
    }

    val isValid = cards.isNotEmpty() && currentTotalAmount > 0 && merchant.trim().isNotBlank()

    val (cutoffDate, dueDate, daysToCutoff) = remember(selectedCard?.id, selectedDate) {
        if (selectedCard == null) {
            Triple(selectedDate.plusDays(15), selectedDate.plusDays(40), 15)
        } else {
            try {
                val cycle = computeCycle(selectedCard, selectedDate)
                val days = ChronoUnit.DAYS.between(selectedDate, cycle.nextCutoff).toInt()
                Triple(cycle.nextCutoff, cycle.nextDue, days)
            } catch (e: Exception) {
                Triple(selectedDate.plusDays(15), selectedDate.plusDays(40), 15)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundCream)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null, tint = MutedSlate, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Registro de consumo", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MutedSlate)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CardBorderColor))
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Monto total de la compra",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkSlate
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = if (selectedCard != null) "Moneda fijada por tarjeta: $currencyCode ($currencySymbol)" else "Selecciona una tarjeta",
                            fontSize = 11.5.sp,
                            color = MutedSlate
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = LightInputBg,
                        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CardBorderColor))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = MutedSlate, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = currencyCode,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkSlate
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(LightInputBg)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "$currencySymbol ",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (amountText.isEmpty()) MutedSlate.copy(alpha = 0.5f) else DarkSlate
                            )
                            BasicTextField(
                                value = amountText,
                                onValueChange = { input ->
                                    if (input.isEmpty() || input.all { it.isDigit() || it == '.' }) {
                                        amountText = input
                                    }
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                textStyle = TextStyle(
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkSlate
                                ),
                                decorationBox = { inner ->
                                    if (amountText.isEmpty()) {
                                        Text(
                                            "0.00",
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MutedSlate.copy(alpha = 0.5f)
                                        )
                                    }
                                    inner()
                                }
                            )
                        }

                        if (amountText.isNotEmpty()) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Limpiar",
                                tint = DarkSlate,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { amountText = "" }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Sugerido:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                    val quickAmounts = if (isUsd) listOf("50", "100", "250", "500") else listOf("50", "100", "300", "1000")
                    quickAmounts.forEach { quickAmount ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = LightInputBg,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { amountText = quickAmount }
                        ) {
                            Text(
                                text = "$currencySymbol $quickAmount",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = DarkSlate,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        if (cards.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CardBorderColor))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.CreditCardOff, contentDescription = null, tint = MutedSlate, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.height(10.dp))
                    Text("No tienes tarjetas registradas. Agrega una tarjeta primero.", fontSize = 13.sp, color = MutedSlate, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = onNavigateToCards,
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaBtn),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Agregar tarjeta", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CardBorderColor))
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tarjeta", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                        Text("${cards.size} registrada${if (cards.size > 1) "s" else ""}", fontSize = 12.sp, color = TerracottaBtn)
                    }

                    cards.forEach { card ->
                        val isCardSelected = selectedCardId == card.id
                        val digits = card.lastFourDigits ?: card.last4

                        val (daysLeft, statusText, statusBg, statusColor) = remember(card.id, selectedDate) {
                            try {
                                val cardCycle = computeCycle(card, selectedDate)
                                val left = ChronoUnit.DAYS.between(selectedDate, cardCycle.nextCutoff).toInt()
                                val st = when {
                                    left > 20 -> Triple("• Óptimo", BadgeOptimoBg, BadgeOptimoText)
                                    left in 6..20 -> Triple("• Regular", BadgeRegularBg, BadgeRegularText)
                                    else -> Triple("• Por cerrar", BadgeCerrandoBg, BadgeCerrandoText)
                                }
                                Tuple4(left, st.first, st.second, st.third)
                            } catch (e: Exception) {
                                Tuple4(15, "• Regular", BadgeRegularBg, BadgeRegularText)
                            }
                        }

                        val cutoffLabel = if (daysLeft == 1) "corta en 1 día" else "corta en $daysLeft días"

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isCardSelected) LightInputBg else CardBackground,
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = SolidColor(if (isCardSelected) TerracottaBtn else CardBorderColor)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedCardId = card.id }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isCardSelected) TerracottaBtn else MutedSlate),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = card.name.take(3).uppercase(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(card.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkSlate)

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            if (!digits.isNullOrBlank()) {
                                                Text("•••• $digits · ", fontSize = 11.sp, color = MutedSlate)
                                            }
                                            Text("${card.currency.name} | Corte: día ${card.cutoffDay}", fontSize = 11.sp, color = MutedSlate)
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Surface(shape = RoundedCornerShape(4.dp), color = statusBg) {
                                                Text(
                                                    text = statusText,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = statusColor,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Text(
                                                text = "· $cutoffLabel",
                                                fontSize = 10.5.sp,
                                                color = MutedSlate
                                            )
                                        }
                                    }
                                }

                                Icon(
                                    imageVector = if (isCardSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (isCardSelected) TerracottaBtn else MutedSlate,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = LightInputBg,
                        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CardBorderColor)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.EventRepeat, contentDescription = null, tint = DarkSlate, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Período en curso", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                            }

                            Text(
                                text = buildAnnotatedString {
                                    append("Esta compra cerrará el ")
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = DarkSlate)) {
                                        append(cutoffDate.format(esFormatter))
                                    }
                                    append(" y se pagará el ")
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = DarkSlate)) {
                                        append(dueDate.format(esFormatter))
                                    }
                                    append(".")
                                },
                                fontSize = 12.sp,
                                color = MutedSlate,
                                lineHeight = 16.sp
                            )

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = CardBackground,
                                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CardBorderColor))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = DarkSlate, modifier = Modifier.size(13.dp))
                                    Spacer(Modifier.width(6.dp))
                                    val dayCountText = if (daysToCutoff == 1) "1 DÍA AL PRÓXIMO CORTE" else "$daysToCutoff DÍAS AL PRÓXIMO CORTE"
                                    Text(
                                        dayCountText,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkSlate
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CardBorderColor))
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Número de cuotas", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                    Text("Seleccionado: $selectedInstallments cuota${if (selectedInstallments > 1) "s" else ""}", fontSize = 12.sp, color = MutedSlate)
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1, 3, 6, 12).forEach { num ->
                        val isSel = selectedInstallments == num
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) TerracottaBtn else LightInputBg)
                                .clickable { selectedInstallments = num },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$num",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else DarkSlate
                            )
                        }
                    }
                }

                if (selectedInstallments > 1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Modalidad de cuotas", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                        Text("(Informativo)", fontSize = 11.sp, color = MutedSlate)
                    }

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isInterestFree) Color.White else LightInputBg,
                            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(if (isInterestFree) CardBorderColor else Color.Transparent)),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    isInterestFree = true
                                    exactMonthlyQuotaText = ""
                                }
                        ) {
                            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (isInterestFree) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (isInterestFree) GreenActive else MutedSlate,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Sin interés", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = DarkSlate)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (!isInterestFree) Color.White else LightInputBg,
                            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(if (!isInterestFree) CardBorderColor else Color.Transparent)),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { isInterestFree = false }
                        ) {
                            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (!isInterestFree) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (!isInterestFree) GreenActive else MutedSlate,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Con interés", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TerracottaBtn)
                            }
                        }
                    }

                    if (!isInterestFree) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = LightInputBg.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, CardBorderColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Cuota mensual del voucher (opcional)",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = DarkSlate
                                    )
                                    Text("Según POS / Banco", fontSize = 10.sp, color = MutedSlate)
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = CardBackground,
                                    border = BorderStroke(1.dp, CardBorderColor),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("$currencySymbol ", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                                        BasicTextField(
                                            value = exactMonthlyQuotaText,
                                            onValueChange = { input ->
                                                if (input.isEmpty() || input.all { it.isDigit() || it == '.' }) {
                                                    exactMonthlyQuotaText = input
                                                }
                                            },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            textStyle = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = DarkSlate),
                                            decorationBox = { inner ->
                                                if (exactMonthlyQuotaText.isEmpty()) {
                                                    Text(
                                                        "Ej. ${String.format(Locale.US, "%.2f", quotaAmount)} / mes",
                                                        fontSize = 12.sp,
                                                        color = MutedSlate.copy(alpha = 0.7f)
                                                    )
                                                }
                                                inner()
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }

                                Text(
                                    text = "Ingresa el monto de la cuota si en tienda ya te dieron el valor final con intereses.",
                                    fontSize = 10.sp,
                                    color = MutedSlate,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }
                }

                if (currentTotalAmount > 0) {
                    val cardNameDisplay = selectedCard?.name ?: "tu tarjeta"

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = LightInputBg,
                        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CardBorderColor)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (selectedInstallments > 1) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "CUOTA ESTIMADA / MES",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MutedSlate,
                                            letterSpacing = 0.5.sp
                                        )
                                        Row(verticalAlignment = Alignment.Bottom) {
                                            Text(
                                                text = "$currencySymbol ${String.format(Locale.US, "%.2f", quotaAmount)}",
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TerracottaBtn
                                            )
                                            Text(
                                                text = " /mes",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MutedSlate,
                                                modifier = Modifier.padding(bottom = 2.dp)
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = CardBackground,
                                        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CardBorderColor))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalAlignment = Alignment.End
                                        ) {
                                            Text(
                                                text = if (exactQuotaInput != null && exactQuotaInput > 0) "TOTAL CON INTERÉS" else "TOTAL COMPRA",
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MutedSlate
                                            )
                                            Text(
                                                "$currencySymbol ${String.format(Locale.US, "%.2f", effectiveTotal)}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DarkSlate
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider(color = CardBorderColor, thickness = 0.8.dp)

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Calculate, contentDescription = null, tint = MutedSlate, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "$selectedInstallments cuotas de $currencySymbol ${String.format(Locale.US, "%.2f", quotaAmount)} en $cardNameDisplay.",
                                        fontSize = 11.5.sp,
                                        color = DarkSlate
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "TOTAL A PAGAR",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MutedSlate,
                                            letterSpacing = 0.5.sp
                                        )
                                        Text(
                                            text = "$currencySymbol ${String.format(Locale.US, "%.2f", currentTotalAmount)}",
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DarkSlate
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = BadgeOptimoBg
                                    ) {
                                        Text(
                                            text = "1 CUOTA DIRECTA",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BadgeOptimoText,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = MutedSlate, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Cálculo referencial. No incluye el seguro de desgravamen ni comisiones fijas del banco, los cuales se liquidan al cierre del ciclo.",
                                    fontSize = 10.5.sp,
                                    color = MutedSlate,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CardBorderColor))
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Rubro o categoría", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                    Text("1 seleccionada", fontSize = 12.sp, color = MutedSlate)
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSel = selectedCategory == cat.name
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSel) TerracottaBtn else LightInputBg,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { selectedCategory = cat.name }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Icon(cat.icon, contentDescription = null, tint = if (isSel) Color.White else DarkSlate, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(cat.name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = if (isSel) Color.White else DarkSlate)
                            }
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(CardBorderColor))
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Comercio o establecimiento *", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkSlate)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(LightInputBg)
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = MutedSlate, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        BasicTextField(
                            value = merchant,
                            onValueChange = { merchant = it },
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 13.sp, color = DarkSlate),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { inner ->
                                if (merchant.isEmpty()) {
                                    Text("Wong, Tottus, Netflix, etc.", fontSize = 13.sp, color = MutedSlate.copy(alpha = 0.7f))
                                }
                                inner()
                            }
                        )
                    }
                }

                Text("Detalle / notas adicionales (opcional)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkSlate)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(LightInputBg)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    BasicTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        textStyle = TextStyle(fontSize = 13.sp, color = DarkSlate),
                        modifier = Modifier.fillMaxSize(),
                        decorationBox = { inner ->
                            if (notes.isEmpty()) {
                                Text(
                                    "Compra mensual, cena de negocios,\nsuscripción anual...",
                                    fontSize = 13.sp,
                                    color = MutedSlate.copy(alpha = 0.7f),
                                    lineHeight = 17.sp
                                )
                            }
                            inner()
                        }
                    )
                }

                Text("Fecha del consumo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkSlate)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(LightInputBg)
                        .clickable { showDatePicker = true }
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MutedSlate, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(dateDisplay, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = DarkSlate)
                        }
                        Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha", tint = DarkSlate, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        Button(
            onClick = {
                val card = selectedCard ?: cards.firstOrNull() ?: return@Button
                if (isValid) {
                    val nextCutoff = try {
                        computeCycle(card, selectedDate).nextCutoff
                    } catch (e: Exception) {
                        selectedDate.plusDays(15)
                    }
                    val fullNote = if (notes.isNotBlank()) "${merchant.trim()} - ${notes.trim()}" else merchant.trim()
                    val interest = if (selectedInstallments > 1) {
                        if (isInterestFree) InterestType.SIN else InterestType.CON
                    } else null

                    onSaveExpense(
                        Expense(
                            cardId = card.id,
                            amountPerInstallment = quotaAmount,
                            installments = selectedInstallments,
                            interestType = interest,
                            category = selectedCategory,
                            note = fullNote,
                            date = selectedDate,
                            firstCutoffDate = nextCutoff
                        )
                    )
                }
            },
            enabled = isValid,
            colors = ButtonDefaults.buttonColors(
                containerColor = TerracottaBtn,
                disabledContainerColor = Color(0xFFD3DDD7)
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(
                Icons.Default.AddCircleOutline,
                contentDescription = null,
                tint = if (isValid) Color.White else MutedSlate,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Registrar gasto",
                color = if (isValid) Color.White else MutedSlate,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        OutlinedButton(
            onClick = { onNavigateToCards() },
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, CardBorderColor),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = DarkSlate
            )
        ) {
            Text(
                text = "Cancelar",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = MutedSlate, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(6.dp))
            Text("Los datos se guardan únicamente en tu teléfono.", fontSize = 11.sp, color = MutedSlate)
        }

        Spacer(Modifier.height(28.dp))
    }

    if (showDatePicker) {
        val initialMillis = selectedDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

        DatePickerGridDialog(datePickerState, onDismiss = { showDatePicker = false }, onConfirm = { millis ->
            selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
            showDatePicker = false
        })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerGridDialog(
    state: DatePickerState,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { state.selectedDateMillis?.let { onConfirm(it) } }
            ) {
                Text("Aceptar", color = TerracottaBtn, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = MutedSlate)
            }
        }
    ) {
        DatePicker(state = state)
    }
}

private data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
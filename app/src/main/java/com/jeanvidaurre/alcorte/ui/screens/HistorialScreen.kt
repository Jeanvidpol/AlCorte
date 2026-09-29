package com.jeanvidaurre.alcorte.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.alcorte.app.data.Card
import com.alcorte.app.data.Expense
import com.jeanvidaurre.alcorte.ui.theme.BackgroundCream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val CardWhite = Color(0xFFFFFFFF)
private val LightInputBg = Color(0xFFEDF4F0)
private val BorderSubtle = Color(0xFFE4EDE7)
private val DarkHeader = Color(0xFF1F2D26)
private val TextMuted = Color(0xFF6B8074)
private val TerracottaPrimary = Color(0xFF9E3E26)
private val BadgeGreenBg = Color(0xFFD4EFE1)
private val BadgeGreenText = Color(0xFF1B6E4A)
private val BadgeInstallmentBg = Color(0xFFFDE8E4)
private val BadgeInstallmentText = Color(0xFFB9442C)
private val ButtonDeleteBg = Color(0xFFFDE8E8)
private val ButtonDeleteText = Color(0xFFB83232)

@Composable
fun HistorialScreen(
    cards: List<Card>,
    expenses: List<Expense>,
    onDeleteExpense: (Expense) -> Unit = {},
    onUpdateExpense: (Expense) -> Unit = {},
    onRegisterFirstCard: () -> Unit = {},
    onGoToRegisterExpense: () -> Unit = {}
) {
    var selectedCardFilterId by remember { mutableStateOf<String?>(null) }
    var selectedMonthDate by remember { mutableStateOf(LocalDate.now()) }
    var expandedDropdown by remember { mutableStateOf(false) }
    var showMonthYearPicker by remember { mutableStateOf(false) }

    var expenseToViewDetails by remember { mutableStateOf<Expense?>(null) }
    var expenseToEdit by remember { mutableStateOf<Expense?>(null) }
    var expenseToDeleteConfirm by remember { mutableStateOf<Expense?>(null) }

    val filteredExpenses = expenses.filter { exp ->
        val matchesCard = selectedCardFilterId == null || exp.cardId == selectedCardFilterId
        val matchesMonth = exp.date.year == selectedMonthDate.year && exp.date.monthValue == selectedMonthDate.monthValue
        matchesCard && matchesMonth
    }

    val totalPen = filteredExpenses
        .filter { exp -> cards.find { it.id == exp.cardId }?.currency?.name?.contains("PEN") == true }
        .sumOf { it.totalAmount }

    val totalUsd = filteredExpenses
        .filter { exp -> cards.find { it.id == exp.cardId }?.currency?.name?.contains("USD") == true }
        .sumOf { it.totalAmount }

    val monthName = selectedMonthDate.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("es-PE"))).uppercase()

    val today = LocalDate.now()
    val yesterday = today.minusDays(1)

    val groupedExpenses = remember(filteredExpenses) {
        filteredExpenses.groupBy { it.date }.toList().sortedWith(
            compareByDescending<Pair<LocalDate, List<Expense>>> { (date, _) -> date == today }
                .thenByDescending { (date, _) -> date }
        )
    }

    val activeCard = cards.find { it.id == selectedCardFilterId }
    val activeFilterLabel = activeCard?.name ?: "Todas las tarjetas (${cards.size})"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundCream)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            border = BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.AutoMirrored.Filled.Notes,
                        contentDescription = null,
                        tint = DarkHeader,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Historial de movimientos",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = DarkHeader
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Consumo total", fontSize = 11.5.sp, color = TextMuted)
                        Text(
                            text = "${filteredExpenses.size} movimiento${if (filteredExpenses.size != 1) "s" else ""}",
                            fontSize = 11.5.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (totalPen > 0 || (totalPen == 0.0 && totalUsd == 0.0)) {
                            Text(
                                text = "S/ ${String.format(Locale.US, "%,.2f", totalPen)}",
                                fontSize = 23.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkHeader
                            )
                        }
                        if (totalPen > 0 && totalUsd > 0) {
                            Text("+", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TerracottaPrimary)
                        }
                        if (totalUsd > 0) {
                            Text(
                                text = "$ ${String.format(Locale.US, "%,.2f", totalUsd)}",
                                fontSize = 23.sp,
                                fontWeight = FontWeight.Bold,
                                color = TerracottaPrimary
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Filtrar por tarjeta", fontSize = 11.5.sp, color = TextMuted)
                        if (selectedCardFilterId != null) {
                            Text(
                                text = "Limpiar",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = TerracottaPrimary,
                                modifier = Modifier.clickable { selectedCardFilterId = null }
                            )
                        }
                    }

                    Box {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = LightInputBg,
                            border = BorderStroke(
                                1.dp,
                                if (selectedCardFilterId != null) TerracottaPrimary.copy(alpha = 0.4f) else BorderSubtle
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { expandedDropdown = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CreditCard,
                                        contentDescription = null,
                                        tint = if (selectedCardFilterId != null) TerracottaPrimary else TextMuted,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = activeFilterLabel,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = DarkHeader,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Desplegar",
                                    tint = DarkHeader,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .background(CardWhite)
                        ) {
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Apps,
                                        contentDescription = null,
                                        tint = if (selectedCardFilterId == null) TerracottaPrimary else TextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                text = {
                                    Text(
                                        text = "Todas las tarjetas (${cards.size})",
                                        fontWeight = if (selectedCardFilterId == null) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedCardFilterId == null) TerracottaPrimary else DarkHeader,
                                        fontSize = 13.sp
                                    )
                                },
                                onClick = {
                                    selectedCardFilterId = null
                                    expandedDropdown = false
                                }
                            )

                            HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)

                            cards.forEach { card ->
                                val isSelected = selectedCardFilterId == card.id
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.CreditCard,
                                            contentDescription = null,
                                            tint = if (isSelected) TerracottaPrimary else TextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    text = {
                                        Text(
                                            text = "${card.name} (${card.currency.name})",
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) TerracottaPrimary else DarkHeader,
                                            fontSize = 13.sp
                                        )
                                    },
                                    onClick = {
                                        selectedCardFilterId = card.id
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = LightInputBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { selectedMonthDate = selectedMonthDate.minusMonths(1) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.ChevronLeft,
                                contentDescription = "Mes anterior",
                                tint = DarkHeader,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { showMonthYearPicker = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = TerracottaPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = monthName,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkHeader
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Cambiar período",
                                tint = DarkHeader,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = { selectedMonthDate = selectedMonthDate.plusMonths(1) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = "Mes siguiente",
                                tint = DarkHeader,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        if (groupedExpenses.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                border = BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ReceiptLong,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "No hay consumos en este mes",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkHeader
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Tus gastos aparecerán listados aquí.",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }
        } else {
            groupedExpenses.forEach { (date, dailyExpenses) ->
                val dayPen = dailyExpenses
                    .filter { exp -> cards.find { it.id == exp.cardId }?.currency?.name?.contains("PEN") == true }
                    .sumOf { it.totalAmount }
                val dayUsd = dailyExpenses
                    .filter { exp -> cards.find { it.id == exp.cardId }?.currency?.name?.contains("USD") == true }
                    .sumOf { it.totalAmount }

                val subtotalStr = buildString {
                    if (dayPen > 0) append("S/ ${String.format(Locale.US, "%,.2f", dayPen)}")
                    if (dayPen > 0 && dayUsd > 0) append(" + ")
                    if (dayUsd > 0) append("$ ${String.format(Locale.US, "%,.2f", dayUsd)}")
                }

                val datePattern = DateTimeFormatter.ofPattern("d 'de' MMMM", Locale.forLanguageTag("es-PE"))
                val (bulletColor, headerDateTitle) = when (date) {
                    today -> TerracottaPrimary to "Hoy, ${date.format(datePattern)}"
                    yesterday -> TextMuted to "Ayer, ${date.format(datePattern)}"
                    else -> TextMuted to date.format(datePattern)
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(bulletColor)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = headerDateTitle,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkHeader
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Subtotal: ",
                                fontSize = 11.5.sp,
                                color = TextMuted
                            )
                            Text(
                                text = subtotalStr,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkHeader
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        border = BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Column {
                            dailyExpenses.forEachIndexed { index, expense ->
                                val card = cards.find { it.id == expense.cardId }
                                val isUsd = card?.currency?.name?.contains("USD") == true
                                val currencySymbol = if (isUsd) "$" else "S/"
                                val currencyLabel = if (isUsd) "Dólares (USD)" else "Soles (PEN)"

                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = expense.note.ifBlank { "Consumo" },
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DarkHeader,
                                            modifier = Modifier.weight(1f)
                                        )

                                        Text(
                                            text = "$currencySymbol ${String.format(Locale.US, "%,.2f", expense.totalAmount)}",
                                            fontSize = 19.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DarkHeader
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(3.dp),
                                            color = LightInputBg
                                        ) {
                                            Text(
                                                text = card?.name ?: "Tarjeta",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = TextMuted,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        if (expense.installments > 1) {
                                            val perMonth = expense.amountPerInstallment
                                            Surface(
                                                shape = RoundedCornerShape(3.dp),
                                                color = BadgeInstallmentBg
                                            ) {
                                                Text(
                                                    text = "CUOTA 1 DE ${expense.installments} ($currencySymbol ${String.format(Locale.US, "%,.2f", perMonth)}/MES)",
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = BadgeInstallmentText,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                )
                                            }
                                        } else {
                                            Surface(
                                                shape = RoundedCornerShape(3.dp),
                                                color = BadgeGreenBg
                                            ) {
                                                Text(
                                                    text = "1 CUOTA",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = BadgeGreenText,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    if (expense.category.isNotBlank()) {
                                        Text(
                                            text = expense.category,
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Moneda: $currencyLabel",
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(
                                                onClick = { expenseToViewDetails = expense },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    Icons.AutoMirrored.Filled.Notes,
                                                    contentDescription = "Detalle",
                                                    tint = TextMuted,
                                                    modifier = Modifier.size(17.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = { expenseToEdit = expense },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Edit,
                                                    contentDescription = "Editar",
                                                    tint = TextMuted,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = { expenseToDeleteConfirm = expense },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.DeleteOutline,
                                                    contentDescription = "Borrar",
                                                    tint = TextMuted,
                                                    modifier = Modifier.size(17.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                if (index < dailyExpenses.size - 1) {
                                    HorizontalDivider(
                                        color = BorderSubtle,
                                        thickness = 1.dp,
                                        modifier = Modifier.padding(horizontal = 14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
    }

    expenseToViewDetails?.let { expense ->
        val card = cards.find { it.id == expense.cardId }
        val isUsd = card?.currency?.name?.contains("USD") == true
        val symbol = if (isUsd) "$" else "S/"

        Dialog(onDismissRequest = { expenseToViewDetails = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CardWhite,
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(LightInputBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = TerracottaPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = expense.note.ifBlank { "Consumo" },
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = DarkHeader
                            )
                            Text(
                                text = "Detalle del movimiento",
                                fontSize = 11.5.sp,
                                color = TextMuted
                            )
                        }
                    }

                    HorizontalDivider(color = BorderSubtle, thickness = 0.8.dp)

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        DetailRow(label = "Tarjeta", value = card?.name ?: "No asignada")
                        DetailRow(label = "Categoría", value = expense.category.ifBlank { "General" })
                        DetailRow(label = "Fecha", value = expense.date.format(DateTimeFormatter.ofPattern("dd 'de' MMMM, yyyy", Locale.forLanguageTag("es-PE"))))
                        DetailRow(
                            label = "Plan de pago",
                            value = if (expense.installments > 1) "${expense.installments} cuotas ($symbol ${String.format(Locale.US, "%.2f", expense.amountPerInstallment)}/mes)" else "1 cuota (Directo)",
                            highlight = expense.installments > 1
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = LightInputBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Monto Total",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = DarkHeader
                            )
                            Text(
                                text = "$symbol ${String.format(Locale.US, "%.2f", expense.totalAmount)}",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkHeader
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = DarkHeader,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { expenseToViewDetails = null }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Entendido",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    expenseToEdit?.let { currentExpense ->
        EditExpenseDialog(
            expense = currentExpense,
            cards = cards,
            onDismiss = { expenseToEdit = null },
            onSave = { updatedExpense ->
                onUpdateExpense(updatedExpense)
                expenseToEdit = null
            }
        )
    }

    expenseToDeleteConfirm?.let { expense ->
        Dialog(onDismissRequest = { expenseToDeleteConfirm = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CardWhite,
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(ButtonDeleteBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = null,
                            tint = ButtonDeleteText,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "¿Eliminar este consumo?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = DarkHeader,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Se quitará \"${expense.note}\" del registro y se descontará del saldo del mes.",
                            fontSize = 12.5.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center,
                            lineHeight = 17.sp
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CardWhite,
                            border = BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { expenseToDeleteConfirm = null }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Cancelar",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = DarkHeader
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ButtonDeleteBg,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onDeleteExpense(expense)
                                    expenseToDeleteConfirm = null
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Eliminar",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ButtonDeleteText
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showMonthYearPicker) {
        var tempYear by remember { mutableIntStateOf(selectedMonthDate.year) }
        val monthNames = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Set", "Oct", "Nov", "Dic")
        val currentMonth = today.monthValue
        val currentYear = today.year

        Dialog(onDismissRequest = { showMonthYearPicker = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CardWhite,
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = LightInputBg,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { tempYear-- }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "Año anterior", tint = DarkHeader, modifier = Modifier.size(20.dp))
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$tempYear",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = DarkHeader
                            )
                            Text(
                                text = "Selecciona el período",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = LightInputBg,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { tempYear++ }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "Año siguiente", tint = DarkHeader, modifier = Modifier.size(20.dp))
                            }
                        }
                    }

                    HorizontalDivider(color = BorderSubtle, thickness = 0.6.dp)

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        userScrollEnabled = false,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(monthNames) { index, mName ->
                            val monthNum = index + 1
                            val isSelected = tempYear == selectedMonthDate.year && monthNum == selectedMonthDate.monthValue
                            val isCurrentMonth = tempYear == currentYear && monthNum == currentMonth

                            val containerColor = when {
                                isSelected -> TerracottaPrimary
                                isCurrentMonth -> LightInputBg
                                else -> CardWhite
                            }

                            val textColor = when {
                                isSelected -> Color.White
                                isCurrentMonth -> TerracottaPrimary
                                else -> DarkHeader
                            }

                            val borderStroke = when {
                                isSelected -> null
                                isCurrentMonth -> BorderStroke(1.2.dp, TerracottaPrimary.copy(alpha = 0.5f))
                                else -> BorderStroke(1.dp, BorderSubtle)
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = containerColor,
                                border = borderStroke,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedMonthDate = LocalDate.of(tempYear, monthNum, 1)
                                        showMonthYearPicker = false
                                    }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = mName,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected || isCurrentMonth) FontWeight.Bold else FontWeight.Medium,
                                        color = textColor,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = BorderSubtle, thickness = 0.6.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                selectedMonthDate = LocalDate.now()
                                showMonthYearPicker = false
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.Today, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Mes actual",
                                color = TerracottaPrimary,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        TextButton(
                            onClick = { showMonthYearPicker = false },
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text(
                                text = "Cerrar",
                                color = TextMuted,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditExpenseDialog(
    expense: Expense,
    cards: List<Card>,
    onDismiss: () -> Unit,
    onSave: (Expense) -> Unit
) {
    var note by remember { mutableStateOf(expense.note) }
    var category by remember { mutableStateOf(expense.category) }
    var totalAmountStr by remember { mutableStateOf(expense.totalAmount.toString()) }
    var installments by remember { mutableIntStateOf(expense.installments) }
    var selectedCardId by remember { mutableStateOf(expense.cardId) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CardWhite,
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Editar movimiento",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = DarkHeader
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Descripción o nota", fontSize = 11.5.sp, fontWeight = FontWeight.Medium, color = DarkHeader)
                    Surface(shape = RoundedCornerShape(8.dp), color = LightInputBg, modifier = Modifier.fillMaxWidth()) {
                        TextField(
                            value = note,
                            onValueChange = { note = it },
                            placeholder = { Text("Ej. Compras supermercado", color = TextMuted, fontSize = 13.sp) },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, color = DarkHeader)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Monto total", fontSize = 11.5.sp, fontWeight = FontWeight.Medium, color = DarkHeader)
                    Surface(shape = RoundedCornerShape(8.dp), color = LightInputBg, modifier = Modifier.fillMaxWidth()) {
                        TextField(
                            value = totalAmountStr,
                            onValueChange = { totalAmountStr = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, color = DarkHeader)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Categoría", fontSize = 11.5.sp, fontWeight = FontWeight.Medium, color = DarkHeader)
                    Surface(shape = RoundedCornerShape(8.dp), color = LightInputBg, modifier = Modifier.fillMaxWidth()) {
                        TextField(
                            value = category,
                            onValueChange = { category = it },
                            placeholder = { Text("Ej. Supermercado, Servicios", color = TextMuted, fontSize = 13.sp) },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, color = DarkHeader)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Número de cuotas", fontSize = 11.5.sp, fontWeight = FontWeight.Medium, color = DarkHeader)
                    Surface(shape = RoundedCornerShape(8.dp), color = LightInputBg, modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { if (installments > 1) installments-- },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkHeader)
                            }
                            Text(
                                text = "$installments cuota${if (installments > 1) "s" else ""}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkHeader
                            )
                            IconButton(
                                onClick = { if (installments < 36) installments++ },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkHeader)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CardWhite,
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onDismiss() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("Cancelar", fontSize = 13.sp, color = DarkHeader)
                        }
                    }

                    val amountVal = totalAmountStr.toDoubleOrNull()
                    val canSave = note.isNotBlank() && amountVal != null && amountVal > 0.0

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (canSave) TerracottaPrimary else LightInputBg,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(enabled = canSave) {
                                val perInstallment = amountVal!! / installments
                                val updated = expense.copy(
                                    note = note.trim(),
                                    category = category.trim(),
                                    installments = installments,
                                    amountPerInstallment = perInstallment
                                )
                                onSave(updated)
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "Guardar cambios",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (canSave) Color.White else TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    highlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.5.sp,
            color = TextMuted
        )
        Text(
            text = value,
            fontSize = 12.5.sp,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium,
            color = if (highlight) TerracottaPrimary else DarkHeader
        )
    }
}
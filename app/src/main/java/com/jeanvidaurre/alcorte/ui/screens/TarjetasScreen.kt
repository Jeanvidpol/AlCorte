package com.jeanvidaurre.alcorte.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.alcorte.app.data.Currency
import com.alcorte.app.data.Expense
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
private val BadgeGreenBg = Color(0xFFD4EFE1)
private val BadgeGreenText = Color(0xFF1B6E4A)
private val BadgeBlueBg = Color(0xFFE2EFF7)
private val BadgeBlueText = Color(0xFF2C7A9E)
private val ButtonDeleteBg = Color(0xFFFDE8E8)
private val ButtonDeleteText = Color(0xFFB83232)

@Composable
fun TarjetasScreen(
    cards: List<Card>,
    expenses: List<Expense>,
    onSaveCard: (name: String, cutoff: Int, due: Int, currencyStr: String, lastFour: String?, existingId: String?) -> Unit,
    onDeleteCard: (Card) -> Unit,
    onNavigateToExpenses: () -> Unit
) {
    var selectedCardForDetail by remember { mutableStateOf<Card?>(null) }
    var isCreatingOrEditingCard by remember { mutableStateOf(false) }
    var cardToEdit by remember { mutableStateOf<Card?>(null) }
    var cardToDeleteConfirm by remember { mutableStateOf<Card?>(null) }

    when {
        isCreatingOrEditingCard -> {
            FormularioTarjetaView(
                initialCard = cardToEdit,
                onBack = {
                    isCreatingOrEditingCard = false
                    cardToEdit = null
                },
                onSave = { name, cutoff, due, curr, lastFour ->
                    onSaveCard(name, cutoff, due, curr, lastFour, cardToEdit?.id)
                    isCreatingOrEditingCard = false
                    cardToEdit = null
                }
            )
        }

        selectedCardForDetail != null -> {
            DetalleVentanaCompraView(
                card = selectedCardForDetail!!,
                allCards = cards,
                onBack = { selectedCardForDetail = null },
                onSelectOtherCard = { newCard -> selectedCardForDetail = newCard },
                onRegisterExpenseWithCard = {
                    onNavigateToExpenses()
                }
            )
        }

        else -> {
            val today = LocalDate.now()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundCream)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Tarjetas registradas (${cards.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = DarkHeader
                        )
                        Text(
                            text = "Libreta de ciclos y vencimientos",
                            fontSize = 11.5.sp,
                            color = TextMuted
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = TerracottaPrimary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                cardToEdit = null
                                isCreatingOrEditingCard = true
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "NUEVA TARJETA",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                if (cards.isEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        border = BorderStroke(1.dp, TerracottaPrimary.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = LightInputBg,
                                border = BorderStroke(1.dp, TerracottaPrimary.copy(alpha = 0.3f)),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.CreditCard,
                                        contentDescription = null,
                                        tint = TerracottaPrimary,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "LIBRETA DE VENCIMIENTOS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Billetera sin tarjetas",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = DarkHeader
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "Aquí podrás consultar tus fechas de corte, días de pago y límites de cada plástico.",
                                    fontSize = 12.5.sp,
                                    color = TextMuted,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 17.sp
                                )
                            }

                            Spacer(Modifier.height(4.dp))

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = TerracottaPrimary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        cardToEdit = null
                                        isCreatingOrEditingCard = true
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "Nueva tarjeta",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            HorizontalDivider(
                                color = BorderSubtle,
                                thickness = 0.8.dp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "100% privado y manual. Tus datos nunca salen de este teléfono.",
                                    fontSize = 11.sp,
                                    color = TextMuted,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    cards.forEach { card ->
                        val cardExpenses = expenses.filter { it.cardId == card.id }
                        val totalSpent = cardExpenses.sumOf { it.totalAmount }
                        val symbol = if (card.currency == Currency.USD) "$" else "S/"
                        val digits = card.lastFourDigits ?: card.last4

                        var nextCutoff = today.withDayOfMonth(card.cutoffDay.coerceAtMost(today.lengthOfMonth()))
                        if (!nextCutoff.isAfter(today)) {
                            val nm = today.plusMonths(1)
                            nextCutoff = nm.withDayOfMonth(card.cutoffDay.coerceAtMost(nm.lengthOfMonth()))
                        }
                        val daysToCutoff = ChronoUnit.DAYS.between(today, nextCutoff)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            border = BorderStroke(1.dp, BorderSubtle)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            Icons.Default.CreditCard,
                                            contentDescription = null,
                                            tint = TerracottaPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = card.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = DarkHeader,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (!digits.isNullOrBlank()) {
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                text = "•••• $digits",
                                                fontSize = 11.sp,
                                                color = TextMuted
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (daysToCutoff <= 3) BadgeBlueBg else BadgeGreenBg
                                    ) {
                                        Text(
                                            text = if (daysToCutoff == 0L) "Corte hoy" else "Corte en $daysToCutoff días",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (daysToCutoff <= 3) BadgeBlueText else BadgeGreenText,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = LightInputBg.copy(alpha = 0.6f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                text = "DÍA DE CORTE",
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextMuted,
                                                letterSpacing = 0.8.sp
                                            )
                                            Text(
                                                text = "Día ${card.cutoffDay}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DarkHeader
                                            )
                                            Text(
                                                text = "de cada mes",
                                                fontSize = 9.5.sp,
                                                color = TextMuted
                                            )
                                        }

                                        Column(
                                            horizontalAlignment = Alignment.End,
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Text(
                                                text = "LÍMITE DE PAGO",
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextMuted,
                                                letterSpacing = 0.8.sp
                                            )
                                            Text(
                                                text = "Día ${card.dueDay}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DarkHeader
                                            )
                                            Text(
                                                text = "Mes siguiente",
                                                fontSize = 9.5.sp,
                                                color = TextMuted
                                            )
                                        }
                                    }
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                    Text("Consumo acumulado", fontSize = 10.5.sp, color = TextMuted)
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            text = "$symbol ${String.format(Locale.US, "%,.2f", totalSpent)}",
                                            fontSize = 19.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TerracottaPrimary
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = card.currency.name,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextMuted,
                                            modifier = Modifier.padding(bottom = 2.dp)
                                        )
                                    }
                                }

                                HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = BadgeBlueBg,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { selectedCardForDetail = card }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = BadgeBlueText, modifier = Modifier.size(13.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                text = "Ver ventana de compra",
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = BadgeBlueText
                                            )
                                        }
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = LightInputBg,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .clickable {
                                                    cardToEdit = card
                                                    isCreatingOrEditingCard = true
                                                }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = null, tint = DarkHeader, modifier = Modifier.size(12.dp))
                                                Spacer(Modifier.width(3.dp))
                                                Text("Editar", fontSize = 10.5.sp, fontWeight = FontWeight.Medium, color = DarkHeader)
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = ButtonDeleteBg,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .clickable { cardToDeleteConfirm = card }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = ButtonDeleteText, modifier = Modifier.size(12.dp))
                                                Spacer(Modifier.width(3.dp))
                                                Text("Eliminar", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = ButtonDeleteText)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
            }
        }
    }

    cardToDeleteConfirm?.let { card ->
        Dialog(onDismissRequest = { cardToDeleteConfirm = null }) {
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
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = ButtonDeleteText, modifier = Modifier.size(24.dp))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("¿Eliminar ${card.name}?", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = DarkHeader)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Se eliminarán también todos los consumos asociados a esta tarjeta.",
                            fontSize = 12.5.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
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
                                .clickable { cardToDeleteConfirm = null }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("Cancelar", fontSize = 13.sp, color = DarkHeader)
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
                                    onDeleteCard(card)
                                    cardToDeleteConfirm = null
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("Eliminar", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ButtonDeleteText)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormularioTarjetaView(
    initialCard: Card?,
    onBack: () -> Unit,
    onSave: (name: String, cutoff: Int, due: Int, currencyStr: String, lastFour: String?) -> Unit
) {
    var name by remember { mutableStateOf(initialCard?.name ?: "") }
    var cutoffDay by remember { mutableIntStateOf(initialCard?.cutoffDay ?: 18) }
    var dueDay by remember { mutableIntStateOf(initialCard?.dueDay ?: 12) }
    var lastFourDigits by remember { mutableStateOf(initialCard?.lastFourDigits ?: initialCard?.last4 ?: "") }
    var currencyStr by remember { mutableStateOf(if (initialCard?.currency == Currency.USD) "USD" else "PEN") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundCream)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = CardWhite,
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onBack() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = DarkHeader, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.width(10.dp))

            Text(
                text = "Volver a tarjetas",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium,
                color = DarkHeader
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            border = BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialCard == null) "Nueva tarjeta" else "Editar tarjeta",
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = DarkHeader
                    )
                    Surface(shape = RoundedCornerShape(4.dp), color = LightInputBg) {
                        Text("Registro manual", fontSize = 10.sp, color = TextMuted, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
                Text(
                    text = "Configura el ciclo de facturación para calcular con precisión tus fechas y vencimientos.",
                    fontSize = 11.5.sp,
                    color = TextMuted,
                    lineHeight = 16.sp
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            border = BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Nombre o banco *", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = DarkHeader)
                        Text("Requerido", fontSize = 10.5.sp, color = TextMuted)
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = LightInputBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = { Text("Ej. BCP Visa Signature", color = TextMuted, fontSize = 13.sp) },
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
                    Text("Un nombre que te permita identificarla fácilmente.", fontSize = 10.sp, color = TextMuted)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Día de corte *", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = DarkHeader)
                        StepperNumberPicker(
                            value = cutoffDay,
                            onValueChange = { cutoffDay = it.coerceIn(1, 31) }
                        )
                        Text("El día del mes en que emite tu estado de cuenta.", fontSize = 10.sp, color = TextMuted, lineHeight = 13.sp)
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Día de pago *", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = DarkHeader)
                        StepperNumberPicker(
                            value = dueDay,
                            onValueChange = { dueDay = it.coerceIn(1, 31) }
                        )
                        Text("Fecha límite para pagar el ciclo cerrado.", fontSize = 10.sp, color = TextMuted, lineHeight = 13.sp)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Últimos 4 dígitos ", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = DarkHeader)
                            Text("(opcional)", fontSize = 10.5.sp, color = TextMuted)
                        }
                        Text("Identificador", fontSize = 10.5.sp, color = TextMuted)
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = LightInputBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "•••• ",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                            TextField(
                                value = lastFourDigits,
                                onValueChange = { input ->
                                    if (input.length <= 4 && input.all { it.isDigit() }) {
                                        lastFourDigits = input
                                    }
                                },
                                placeholder = { Text("4921", color = TextMuted.copy(alpha = 0.5f), fontSize = 13.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                    Text("Útil para diferenciar tarjetas del mismo banco.", fontSize = 10.sp, color = TextMuted)
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Moneda de la tarjeta *", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = DarkHeader)
                        Text("Principal", fontSize = 10.5.sp, color = TextMuted)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val isPen = currencyStr == "PEN"
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isPen) LightInputBg else CardWhite,
                            border = BorderStroke(1.2.dp, if (isPen) TerracottaPrimary else BorderSubtle),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { currencyStr = "PEN" }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isPen,
                                    onClick = { currencyStr = "PEN" },
                                    colors = RadioButtonDefaults.colors(selectedColor = TerracottaPrimary)
                                )
                                Spacer(Modifier.width(2.dp))
                                Column {
                                    Text("Soles (PEN)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkHeader)
                                    Text("S/", fontSize = 10.sp, color = TextMuted)
                                }
                            }
                        }

                        val isUsd = currencyStr == "USD"
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isUsd) LightInputBg else CardWhite,
                            border = BorderStroke(1.2.dp, if (isUsd) TerracottaPrimary else BorderSubtle),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { currencyStr = "USD" }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isUsd,
                                    onClick = { currencyStr = "USD" },
                                    colors = RadioButtonDefaults.colors(selectedColor = TerracottaPrimary)
                                )
                                Spacer(Modifier.width(2.dp))
                                Column {
                                    Text("Dólares (USD)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkHeader)
                                    Text("$", fontSize = 10.sp, color = TextMuted)
                                }
                            }
                        }
                    }
                    Text("Establece la divisa predeterminada para el registro rápido.", fontSize = 10.sp, color = TextMuted)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = BadgeGreenBg.copy(alpha = 0.4f)),
            border = BorderStroke(1.dp, BadgeGreenBg)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = BadgeGreenText, modifier = Modifier.size(15.dp))
                    Text("Previsualización del ciclo de facturación", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = BadgeGreenText)
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CardWhite,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = name.ifBlank { "Nombre de la tarjeta" },
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = DarkHeader
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(shape = RoundedCornerShape(4.dp), color = LightInputBg) {
                                    Text(
                                        text = if (currencyStr == "USD") "USD ($)" else "PEN (S/)",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextMuted,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                if (lastFourDigits.isNotBlank()) {
                                    Text(
                                        text = "•••• $lastFourDigits",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextMuted
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Compras hasta el día $cutoffDay se pagan el día $dueDay del mes siguiente.",
                            fontSize = 11.sp,
                            color = DarkHeader
                        )
                    }
                }
            }
        }

        val canSave = name.isNotBlank()
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (canSave) TerracottaPrimary else LightInputBg,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable(enabled = canSave) {
                    onSave(name.trim(), cutoffDay, dueDay, currencyStr, lastFourDigits.ifBlank { null })
                }
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Save, contentDescription = null, tint = if (canSave) Color.White else TextMuted, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Guardar tarjeta",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (canSave) Color.White else TextMuted
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = CardWhite,
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable { onBack() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("Cancelar", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = DarkHeader)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(6.dp))
            Text("Los datos se guardan únicamente en tu teléfono.", fontSize = 10.5.sp, color = TextMuted)
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun StepperNumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = LightInputBg,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { if (value > 1) onValueChange(value - 1) },
                modifier = Modifier.size(32.dp)
            ) {
                Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkHeader)
            }

            Text(
                text = "$value",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = DarkHeader
            )

            IconButton(
                onClick = { if (value < 31) onValueChange(value + 1) },
                modifier = Modifier.size(32.dp)
            ) {
                Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkHeader)
            }
        }
    }
}

@Composable
private fun DetalleVentanaCompraView(
    card: Card,
    allCards: List<Card>,
    onBack: () -> Unit,
    onSelectOtherCard: (Card) -> Unit,
    onRegisterExpenseWithCard: () -> Unit
) {
    val today = LocalDate.now()

    var nextCutoff = today.withDayOfMonth(card.cutoffDay.coerceAtMost(today.lengthOfMonth()))
    if (!nextCutoff.isAfter(today)) {
        val nextMonth = today.plusMonths(1)
        nextCutoff = nextMonth.withDayOfMonth(card.cutoffDay.coerceAtMost(nextMonth.lengthOfMonth()))
    }
    val daysToCutoff = ChronoUnit.DAYS.between(today, nextCutoff)

    var nextDueDate = nextCutoff.withDayOfMonth(card.dueDay.coerceAtMost(nextCutoff.lengthOfMonth()))
    if (!nextDueDate.isAfter(nextCutoff)) {
        val monthAfter = nextCutoff.plusMonths(1)
        nextDueDate = monthAfter.withDayOfMonth(card.dueDay.coerceAtMost(monthAfter.lengthOfMonth()))
    }
    val daysToDue = ChronoUnit.DAYS.between(today, nextDueDate)

    val alternatives = remember(card, allCards) {
        allCards
            .filter { it.id != card.id }
            .map { other ->
                var otherCutoff = today.withDayOfMonth(other.cutoffDay.coerceAtMost(today.lengthOfMonth()))
                if (!otherCutoff.isAfter(today)) {
                    val m = today.plusMonths(1)
                    otherCutoff = m.withDayOfMonth(other.cutoffDay.coerceAtMost(m.lengthOfMonth()))
                }
                var otherDue = otherCutoff.withDayOfMonth(other.dueDay.coerceAtMost(otherCutoff.lengthOfMonth()))
                if (!otherDue.isAfter(otherCutoff)) {
                    val m = otherCutoff.plusMonths(1)
                    otherDue = m.withDayOfMonth(other.dueDay.coerceAtMost(m.lengthOfMonth()))
                }
                val dCutoff = ChronoUnit.DAYS.between(today, otherCutoff)
                val dDue = ChronoUnit.DAYS.between(today, otherDue)
                Triple(other, dCutoff, dDue)
            }
            .sortedByDescending { it.third }
    }

    val dateFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM", Locale.forLanguageTag("es-PE"))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundCream)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = CardWhite,
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onBack() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = DarkHeader, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.width(12.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = card.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = DarkHeader
                    )
                    val digits = card.lastFourDigits ?: card.last4
                    if (!digits.isNullOrBlank()) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "•••• $digits",
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    }
                }
                Text(
                    text = "Ventana de compra y corte",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            border = BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(LightInputBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Update, contentDescription = null, tint = DarkHeader, modifier = Modifier.size(20.dp))
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "PRÓXIMO CORTE",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = nextCutoff.format(dateFormatter),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = DarkHeader
                        )
                        Text(
                            text = "Faltan $daysToCutoff días para el cierre de ciclo.",
                            fontSize = 11.5.sp,
                            color = TextMuted
                        )
                    }
                }

                HorizontalDivider(color = BorderSubtle, thickness = 0.6.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(LightInputBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(18.dp))
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "FECHA LÍMITE DE PAGO ESTIMADA",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = nextDueDate.format(dateFormatter),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = DarkHeader
                        )
                        Text(
                            text = "Dispones de $daysToDue días de financiamiento si compras hoy.",
                            fontSize = 11.5.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        if (alternatives.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = BadgeGreenText, modifier = Modifier.size(16.dp))
                    Text(
                        text = "ALTERNATIVAS CONVENIENTES EN TU BILLETERA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BadgeGreenText,
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = "Toca cualquier tarjeta para consultar su ciclo o registrar el gasto con ella:",
                    fontSize = 11.5.sp,
                    color = TextMuted
                )

                alternatives.forEachIndexed { index, (altCard, dCutoff, dDue) ->
                    val isBestOption = index == 0 && dDue > daysToDue
                    val altDigits = altCard.lastFourDigits ?: altCard.last4

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectOtherCard(altCard) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        border = BorderStroke(
                            1.dp,
                            if (isBestOption) BadgeGreenText.copy(alpha = 0.4f) else BorderSubtle
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = altCard.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.5.sp,
                                        color = DarkHeader
                                    )
                                    if (!altDigits.isNullOrBlank()) {
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = "•••• $altDigits",
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                    }
                                }
                                Text(
                                    text = "Corte en $dCutoff días (${altCard.cutoffDay} de cada mes)",
                                    fontSize = 11.5.sp,
                                    color = TextMuted
                                )
                                if (isBestOption) {
                                    Text(
                                        text = "OPCIÓN MÁS CONVENIENTE HOY",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BadgeGreenText
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isBestOption) BadgeGreenBg else LightInputBg
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "$dDue días",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isBestOption) BadgeGreenText else DarkHeader
                                    )
                                    Text(
                                        text = "HASTA EL PAGO",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isBestOption) BadgeGreenText else TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = TerracottaPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable { onRegisterExpenseWithCard() }
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Registrar gasto con ${card.name}",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = CardWhite,
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable { onBack() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "Volver a mis tarjetas",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = DarkHeader
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}
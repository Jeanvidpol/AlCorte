package com.jeanvidaurre.alcorte.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
import com.jeanvidaurre.alcorte.ui.theme.BackgroundCream

private val CardWhite = Color(0xFFFFFFFF)
private val InputFieldBg = Color(0xFFEDF4F0)
private val BorderSubtle = Color(0xFFE4EDE7)
private val DarkHeader = Color(0xFF1F2D26)
private val TextMuted = Color(0xFF6B8074)
private val TerracottaBtn = Color(0xFF9E3E26)
private val StepBtnBg = Color(0xFFDFECE5)
private val GreenCalendar = Color(0xFF1B6E4A)
private val PreviewContainerBg = Color(0xFFE8F2EC)

@Composable
fun NuevaTarjetaScreen(
    currentCardsCount: Int = 0,
    cardToEdit: Card? = null,
    onBack: () -> Unit,
    onSaveCard: (name: String, cutoff: Int, due: Int, currency: String, digits: String) -> Unit
) {
    var cardName by remember(cardToEdit) { mutableStateOf(cardToEdit?.name ?: "") }
    var cutoffDay by remember(cardToEdit) { mutableIntStateOf(cardToEdit?.cutoffDay ?: 18) }
    var dueDay by remember(cardToEdit) { mutableIntStateOf(cardToEdit?.dueDay ?: 12) }
    var digits by remember(cardToEdit) { mutableStateOf(if (cardToEdit != null) "5612" else "") }
    var selectedCurrency by remember(cardToEdit) {
        mutableStateOf(if (cardToEdit?.currency?.name?.contains("USD") == true) "USD" else "PEN")
    }

    val isEditing = cardToEdit != null
    val folio = String.format("%03d", if (isEditing) 1 else currentCardsCount + 1)
    val isValid = cardName.trim().isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundCream)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onBack() }
                    .padding(vertical = 4.dp, horizontal = 2.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = DarkHeader,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Volver a tarjetas",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = DarkHeader
                )
            }

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = InputFieldBg,
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(BorderSubtle))
            ) {
                Text(
                    "Folio #$folio",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = InputFieldBg.copy(alpha = 0.5f)),
            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(BorderSubtle))
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (isEditing) "Editar tarjeta" else "Nueva tarjeta",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkHeader
                    )
                    Text(
                        "Registro manual",
                        fontSize = 11.5.sp,
                        color = TextMuted
                    )
                }
                Text(
                    "Configura el ciclo de facturación para calcular con precisión tus fechas y vencimientos.",
                    fontSize = 12.5.sp,
                    color = TextMuted,
                    lineHeight = 17.sp
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(BorderSubtle))
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Nombre o banco *",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkHeader
                        )
                        Text(
                            "Requerido",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(InputFieldBg)
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        BasicTextField(
                            value = cardName,
                            onValueChange = { cardName = it },
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 13.sp, color = DarkHeader),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { inner ->
                                if (cardName.isEmpty()) {
                                    Text(
                                        "Ej. BCP Visa Signature, Interbank Cashback",
                                        fontSize = 13.sp,
                                        color = TextMuted.copy(alpha = 0.7f)
                                    )
                                }
                                inner()
                            }
                        )
                    }
                    Text(
                        "Un nombre que te permita identificarla fácilmente.",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Día de corte *", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkHeader)
                        StepperCounter(
                            value = cutoffDay,
                            onMinus = { if (cutoffDay > 1) cutoffDay-- },
                            onPlus = { if (cutoffDay < 31) cutoffDay++ }
                        )
                        Text(
                            "El día en que el banco emite tu estado de cuenta.",
                            fontSize = 11.sp,
                            color = TextMuted,
                            lineHeight = 15.sp
                        )
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Día de pago *", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkHeader)
                        StepperCounter(
                            value = dueDay,
                            onMinus = { if (dueDay > 1) dueDay-- },
                            onPlus = { if (dueDay < 31) dueDay++ }
                        )
                        Text(
                            "Fecha límite para pagar el ciclo recién cerrado.",
                            fontSize = 11.sp,
                            color = TextMuted,
                            lineHeight = 15.sp
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Últimos 4 dígitos (opcional)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkHeader)
                        Text("Identificador", fontSize = 11.sp, color = TextMuted)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(InputFieldBg)
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "••••  ",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                            BasicTextField(
                                value = digits,
                                onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) digits = it },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkHeader
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                decorationBox = { inner ->
                                    if (digits.isEmpty()) {
                                        Text("4921", fontSize = 14.sp, color = TextMuted.copy(alpha = 0.5f))
                                    }
                                    inner()
                                }
                            )
                        }
                    }
                    Text("Útil para diferenciar tarjetas del mismo banco.", fontSize = 11.sp, color = TextMuted)
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Moneda de la tarjeta *", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkHeader)
                        Text("Principal", fontSize = 11.sp, color = TextMuted)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val isPen = selectedCurrency == "PEN"
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = InputFieldBg,
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = SolidColor(if (isPen) TerracottaBtn else Color.Transparent)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedCurrency = "PEN" }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isPen,
                                    onClick = { selectedCurrency = "PEN" },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = TerracottaBtn,
                                        unselectedColor = TextMuted
                                    ),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text("Soles (PEN)", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = DarkHeader)
                                    Text("S/", fontSize = 11.sp, color = TextMuted)
                                }
                            }
                        }

                        val isUsd = selectedCurrency == "USD"
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = InputFieldBg,
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = SolidColor(if (isUsd) TerracottaBtn else Color.Transparent)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedCurrency = "USD" }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isUsd,
                                    onClick = { selectedCurrency = "USD" },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = TerracottaBtn,
                                        unselectedColor = TextMuted
                                    ),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text("Dólares (USD)", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = DarkHeader)
                                    Text("$", fontSize = 11.sp, color = TextMuted)
                                }
                            }
                        }
                    }

                    Text(
                        "Establece la divisa predeterminada para el registro de tus compras.",
                        fontSize = 11.sp,
                        color = TextMuted,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = PreviewContainerBg),
            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(BorderSubtle))
        ) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EventRepeat,
                        contentDescription = null,
                        tint = GreenCalendar,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Previsualización del ciclo de facturación",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkHeader
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CardWhite,
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(BorderSubtle)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (cardName.isNotBlank()) cardName else "Tarjeta sin nombre",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = DarkHeader
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = InputFieldBg
                                ) {
                                    Text(
                                        text = if (selectedCurrency == "PEN") "PEN (S/)" else "USD ($)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextMuted,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (digits.isNotBlank()) "•••• $digits" else "•••• ————",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        HorizontalDivider(color = BorderSubtle, thickness = 0.8.dp)

                        Text(
                            text = buildAnnotatedString {
                                append("Compras hasta el día ")
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = DarkHeader)) {
                                    append("$cutoffDay")
                                }
                                append(" se pagan el ")
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = DarkHeader)) {
                                    append("$dueDay")
                                }
                                append(" del mes siguiente.")
                            },
                            fontSize = 12.sp,
                            color = TextMuted,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                if (isValid) {
                    onSaveCard(cardName.trim(), cutoffDay, dueDay, selectedCurrency, digits.trim())
                }
            },
            enabled = isValid,
            colors = ButtonDefaults.buttonColors(
                containerColor = TerracottaBtn,
                disabledContainerColor = TerracottaBtn.copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(
                Icons.Default.Save,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Guardar tarjeta",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Text(
            text = "Cancelar",
            fontSize = 13.sp,
            color = TextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onBack() }
                .padding(vertical = 4.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "Los datos se guardan únicamente en tu teléfono.",
                fontSize = 11.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun StepperCounter(
    value: Int,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = InputFieldBg,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(StepBtnBg)
                    .clickable { onMinus() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Remove,
                    contentDescription = "Menos",
                    tint = DarkHeader,
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = "$value",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = DarkHeader
            )

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(StepBtnBg)
                    .clickable { onPlus() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Más",
                    tint = DarkHeader,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}


package com.jeanvidaurre.alcorte.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.alcorte.app.data.Card
import com.alcorte.app.data.Expense
import com.alcorte.app.data.InterestType
import java.time.format.DateTimeFormatter
import java.util.Locale

private val CardWhite = Color(0xFFFFFFFF)
private val LightBg = Color(0xFFEDF4F0)
private val BorderSubtle = Color(0xFFE4EDE7)
private val DarkHeader = Color(0xFF1F2D26)
private val TextMuted = Color(0xFF6B8074)
private val TerracottaBtn = Color(0xFF9E3E26)
private val GreenBadgeBg = Color(0xFFD4EFE1)
private val GreenBadgeText = Color(0xFF1B6E4A)
private val DeleteBtnBg = Color(0xFFFDE8E8)
private val DeleteBtnText = Color(0xFFB83232)

@Composable
fun DetalleGastoDialog(
    expense: Expense,
    card: Card?,
    onDismiss: () -> Unit,
    onDelete: (Expense) -> Unit
) {
    val esFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM, yyyy", Locale.forLanguageTag("es-PE"))
    val currencySymbol = if (card?.currency?.name?.contains("USD") == true) "$" else "S/"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { onDismiss() }
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {}
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(BorderSubtle))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = TerracottaBtn,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Detalle del asiento",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkHeader
                            )
                        }

                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextMuted)
                        }
                    }

                    HorizontalDivider(color = BorderSubtle, thickness = 0.8.dp)

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = LightBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("MONTO TOTAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 0.5.sp)
                                Surface(shape = RoundedCornerShape(4.dp), color = GreenBadgeBg) {
                                    Text(
                                        text = "${expense.installments} CUOTA${if (expense.installments > 1) "S" else ""}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GreenBadgeText,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = "$currencySymbol ${String.format(Locale.US, "%.2f", expense.totalAmount)}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkHeader
                            )

                            if (expense.installments > 1) {
                                val interestLabel = when (expense.interestType) {
                                    InterestType.SIN -> "Sin intereses"
                                    InterestType.CON -> "Con intereses"
                                    null -> "Plan de cuotas"
                                }
                                Text(
                                    text = "${expense.installments} cuotas de $currencySymbol ${String.format(Locale.US, "%.2f", expense.amountPerInstallment)} ($interestLabel)",
                                    fontSize = 11.5.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DetalleRow(label = "Establecimiento", value = expense.note.ifBlank { "Sin comercio" })
                        DetalleRow(label = "Categoría", value = expense.category)
                        DetalleRow(label = "Tarjeta de cargo", value = "${card?.name ?: "Tarjeta"} (•••• 5612)")
                        DetalleRow(label = "Fecha de consumo", value = expense.date.format(esFormatter))
                        DetalleRow(label = "Corte imputado", value = expense.firstCutoffDate.format(esFormatter))
                        DetalleRow(label = "Moneda", value = "${card?.currency?.name ?: "PEN"} ($currencySymbol)")
                    }

                    Spacer(Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = DeleteBtnBg,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onDelete(expense)
                                    onDismiss()
                                }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = DeleteBtnText, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Eliminar gasto", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeleteBtnText)
                            }
                        }

                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = LightBg),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Text("Cerrar", color = DarkHeader, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetalleRow(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = LightBg,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 11.sp, color = TextMuted)
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkHeader)
        }
    }
}
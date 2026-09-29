package com.alcorte.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alcorte.app.data.Currency
import com.alcorte.app.logic.CardStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

private val esPE = Locale.forLanguageTag("es-PE")

/** Igual que fmt() en el HTML: símbolo según moneda + 2 decimales. */
fun formatMoney(amount: Double, currency: Currency): String {
    val symbol = if (currency == Currency.USD) "$" else "S/"
    val rounded = (amount * 100).roundToInt() / 100.0
    return "$symbol %,.2f".format(esPE, rounded)
}

fun formatDateLong(date: LocalDate): String {
    val month = date.month.getDisplayName(TextStyle.FULL, esPE)
    return "${date.dayOfMonth} de $month"
}

@Composable
fun StatusBadge(status: CardStatus) {
    val (label, container, content) = when (status) {
        CardStatus.OPTIMO -> Triple("Óptimo", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.tertiary)
        CardStatus.REGULAR -> Triple("Regular", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.secondary)
        CardStatus.NO_RECOMENDADO -> Triple("No recomendado", MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.error)
    }
    Surface(color = container, shape = RoundedCornerShape(20.dp)) {
        Text(
            label,
            color = content,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

/**
 * Estado vacío reutilizable — el mismo componente para Inicio/Tarjetas/Historial,
 * solo cambia el texto, igual que acordamos con Stitch (nunca una pantalla aparte).
 */
@Composable
fun EmptyState(title: String, body: String, ctaLabel: String, onCta: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(body, style = MaterialTheme.typography.bodySmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onCta) { Text(ctaLabel) }
        Spacer(Modifier.height(12.dp))
        Text("100% privado y manual. Los datos no salen de tu teléfono.", style = MaterialTheme.typography.labelSmall)
    }
}

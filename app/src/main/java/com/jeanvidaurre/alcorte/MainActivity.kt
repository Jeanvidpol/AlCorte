package com.jeanvidaurre.alcorte

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.alcorte.app.data.Card
import com.alcorte.app.data.Currency
import com.alcorte.app.data.Expense
import com.jeanvidaurre.alcorte.notifications.CutoffNotificationWorker
import com.jeanvidaurre.alcorte.ui.screens.*
import com.jeanvidaurre.alcorte.ui.theme.AlCorteTheme
import java.util.UUID
import java.util.concurrent.TimeUnit

enum class AppTab {
    INICIO,
    TARJETAS,
    AGREGAR,
    HISTORIAL
}

private val TerracottaPrimary = Color(0xFF9E3E26)
private val TextMuted = Color(0xFF6B8074)
private val BorderSubtle = Color(0xFFE4EDE7)

class MainActivity : ComponentActivity() {

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val cutoffWorkRequest = PeriodicWorkRequestBuilder<CutoffNotificationWorker>(
            repeatInterval = 24,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "AlCorteDailyCheck",
            ExistingPeriodicWorkPolicy.KEEP,
            cutoffWorkRequest
        )

        enableEdgeToEdge()
        setContent {
            AlCorteApp()
        }
    }
}

@Composable
fun AlCorteApp() {
    var showSplash by remember { mutableStateOf(true) }
    var currentTab by remember { mutableStateOf(AppTab.INICIO) }
    var showConfiguracion by remember { mutableStateOf(false) }

    // Estado global que controla si la app está en modo oscuro
    var isDarkMode by remember { mutableStateOf(false) }

    if (showSplash) {
        SplashScreen(
            onTimeout = { showSplash = false }
        )
        return
    }

    AlCorteTheme(darkTheme = isDarkMode) {
        val cards = remember { mutableStateListOf<Card>() }
        val expenses = remember { mutableStateListOf<Expense>() }

        if (showConfiguracion) {
            ConfiguracionScreen(
                isDarkMode = isDarkMode,
                onDarkModeChanged = { isDarkMode = it },
                onBack = { showConfiguracion = false },
                onClearAll = {
                    cards.clear()
                    expenses.clear()
                }
            )
        } else {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                topBar = {
                    TopAppBarCustom(
                        currentTab = currentTab,
                        onOpenConfiguracion = { showConfiguracion = true }
                    )
                },
                bottomBar = {
                    BottomNavigationCustom(
                        currentTab = currentTab,
                        onTabSelected = { currentTab = it }
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentTab) {
                        AppTab.INICIO -> {
                            InicioScreen(
                                cards = cards,
                                expenses = expenses,
                                onGoAddCard = { currentTab = AppTab.TARJETAS },
                                onGoToHistory = { currentTab = AppTab.HISTORIAL }
                            )
                        }

                        AppTab.TARJETAS -> {
                            TarjetasScreen(
                                cards = cards,
                                expenses = expenses,
                                onSaveCard = { name, cutoff, due, currencyStr, lastFour, existingId ->
                                    val parsedCurrency = if (currencyStr.contains("USD")) Currency.USD else Currency.PEN
                                    if (existingId != null) {
                                        val idx = cards.indexOfFirst { it.id == existingId }
                                        if (idx != -1) {
                                            cards[idx] = Card(
                                                id = existingId,
                                                name = name,
                                                cutoffDay = cutoff,
                                                dueDay = due,
                                                currency = parsedCurrency,
                                                lastFourDigits = lastFour
                                            )
                                        }
                                    } else {
                                        cards.add(
                                            Card(
                                                id = UUID.randomUUID().toString(),
                                                name = name,
                                                cutoffDay = cutoff,
                                                dueDay = due,
                                                currency = parsedCurrency,
                                                lastFourDigits = lastFour
                                            )
                                        )
                                    }
                                },
                                onDeleteCard = { cardToDelete ->
                                    cards.removeAll { it.id == cardToDelete.id }
                                    expenses.removeAll { it.cardId == cardToDelete.id }
                                },
                                onNavigateToExpenses = {
                                    currentTab = AppTab.AGREGAR
                                }
                            )
                        }

                        AppTab.AGREGAR -> {
                            GastosScreen(
                                cards = cards,
                                onSaveExpense = { newExpense ->
                                    expenses.add(newExpense)
                                    currentTab = AppTab.HISTORIAL
                                },
                                onNavigateToCards = { currentTab = AppTab.TARJETAS }
                            )
                        }

                        AppTab.HISTORIAL -> {
                            HistorialScreen(
                                cards = cards,
                                expenses = expenses,
                                onDeleteExpense = { expToDelete ->
                                    expenses.removeAll { it.id == expToDelete.id }
                                },
                                onUpdateExpense = { updatedExp ->
                                    val idx = expenses.indexOfFirst { it.id == updatedExp.id }
                                    if (idx != -1) {
                                        expenses[idx] = updatedExp
                                    }
                                },
                                onRegisterFirstCard = { currentTab = AppTab.TARJETAS },
                                onGoToRegisterExpense = { currentTab = AppTab.AGREGAR }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopAppBarCustom(
    currentTab: AppTab,
    onOpenConfiguracion: () -> Unit
) {
    val subtitle = when (currentTab) {
        AppTab.INICIO -> "INICIO"
        AppTab.TARJETAS -> "TARJETAS"
        AppTab.AGREGAR -> "REGISTRAR"
        AppTab.HISTORIAL -> "HISTORIAL"
    }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(TerracottaPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Al Corte",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = subtitle,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(BorderSubtle)),
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onOpenConfiguracion() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Tune,
                        contentDescription = "Configuración",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavigationCustom(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(BorderSubtle)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                label = "INICIO",
                isSelected = currentTab == AppTab.INICIO,
                onClick = { onTabSelected(AppTab.INICIO) }
            )

            NavItem(
                icon = Icons.Default.CreditCard,
                label = "TARJETAS",
                isSelected = currentTab == AppTab.TARJETAS,
                onClick = { onTabSelected(AppTab.TARJETAS) }
            )

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(TerracottaPrimary)
                    .clickable { onTabSelected(AppTab.AGREGAR) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Agregar",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            NavItem(
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                label = "HISTORIAL",
                isSelected = currentTab == AppTab.HISTORIAL,
                onClick = { onTabSelected(AppTab.HISTORIAL) }
            )
        }
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) TerracottaPrimary else TextMuted,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 9.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) TerracottaPrimary else TextMuted,
            letterSpacing = 0.5.sp
        )
    }
}
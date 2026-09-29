package com.jeanvidaurre.alcorte.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val subtitle: String, val icon: ImageVector) {
    object Inicio : Screen("inicio", "INICIO", "INICIO", Icons.AutoMirrored.Filled.MenuBook)
    object Tarjetas : Screen("tarjetas", "TARJETAS", "TARJETAS", Icons.Default.CreditCard)
    object Agregar : Screen("agregar", "AGREGAR", "REGISTRAR", Icons.Default.Add)
    object Historial : Screen("historial", "HISTORIAL", "HISTORIAL", Icons.AutoMirrored.Filled.ReceiptLong)
    object Configuracion : Screen("configuracion", "CONFIGURACIÓN", "CONFIGURACIÓN", Icons.Default.Tune)
}
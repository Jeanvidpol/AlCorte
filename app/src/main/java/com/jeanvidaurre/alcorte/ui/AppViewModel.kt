package com.alcorte.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alcorte.app.data.*
import com.alcorte.app.logic.computeCycle
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class AppUiState(val cards: List<Card> = emptyList(), val expenses: List<Expense> = emptyList())

class AppViewModel(private val repo: Repository) : ViewModel() {

    val state: StateFlow<AppUiState> = combine(repo.cards, repo.expenses) { cards, expenses ->
        AppUiState(cards, expenses)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppUiState())

    fun saveCard(card: Card) = viewModelScope.launch { repo.saveCard(card) }

    fun deleteCard(card: Card) = viewModelScope.launch { repo.deleteCard(card) }

    /** Registrar gasto: calcula firstCutoffDate según la tarjeta y la fecha elegida, igual que en el HTML. */
    fun addExpense(
        card: Card,
        amountPerInstallment: Double,
        installments: Int,
        interestType: InterestType?,
        category: String,
        note: String,
        date: LocalDate
    ) = viewModelScope.launch {
        val nextCutoff = computeCycle(card, date).nextCutoff
        repo.saveExpense(
            Expense(
                cardId = card.id,
                amountPerInstallment = amountPerInstallment,
                installments = installments,
                interestType = if (installments > 1) interestType else null,
                category = category,
                note = note,
                date = date,
                firstCutoffDate = nextCutoff
            )
        )
    }

    fun updateExpense(expense: Expense) = viewModelScope.launch { repo.saveExpense(expense) }
    fun deleteExpense(expense: Expense) = viewModelScope.launch { repo.deleteExpense(expense) }

    fun deleteAllData() = viewModelScope.launch { repo.deleteAllData() }

    suspend fun exportSnapshot() = repo.exportSnapshot()
    suspend fun importSnapshot(cards: List<Card>, expenses: List<Expense>) = repo.importSnapshot(cards, expenses)
}

package com.example.hw_3.presentation.ui.state

import com.example.hw_3.domain.model.NameDay

// Sealed class для состояний UI
sealed class NameDayUiState {
    object Loading : NameDayUiState()
    data class Success(val nameDays: List<NameDay>) : NameDayUiState()
    data class Error(val message: String) : NameDayUiState()
    object Empty : NameDayUiState()
}



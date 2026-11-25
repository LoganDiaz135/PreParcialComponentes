package com.example.preparcialcomponentes.presentation.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.preparcialcomponentes.core.common.UiState
import com.example.preparcialcomponentes.presentation.components.AppButton
import com.example.preparcialcomponentes.presentation.components.EmptyState
import com.example.preparcialcomponentes.presentation.components.ErrorMessage
import com.example.preparcialcomponentes.presentation.components.LoadingIndicator

/**
 * Pantalla principal (Home) de la aplicación base.
 *
 * Demuestra cómo una pantalla Compose observa un [StateFlow] de [UiState]
 * y reacciona a cada estado sin contener lógica de negocio ni llamadas HTTP.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToPlans: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Pre-Parcial Componentes",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { paddingValues ->
        when (val currentState = state) {
            is UiState.Loading -> {
                LoadingIndicator(modifier = Modifier.padding(paddingValues))
            }
            is UiState.Success -> {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Plantilla Base Lista",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentState.data,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Text(
                        text = "Módulos de Práctica",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    AppButton(
                        text = "HU-01: Consultar Planes de Ahorro",
                        onClick = onNavigateToPlans
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Arquitectura base: Clean Architecture + MVVM + Retrofit + Coroutines + StateFlow",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            is UiState.Empty -> {
                EmptyState(modifier = Modifier.padding(paddingValues))
            }
            is UiState.Error -> {
                ErrorMessage(
                    message = currentState.message,
                    modifier = Modifier.padding(paddingValues),
                    onRetry = { viewModel.loadInitialData() }
                )
            }
        }
    }
}

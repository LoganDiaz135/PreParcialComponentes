package com.example.preparcialcomponentes.presentation.screens.plans

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.preparcialcomponentes.core.common.UiState
import com.example.preparcialcomponentes.domain.model.SavingPlan
import com.example.preparcialcomponentes.presentation.components.EmptyState
import com.example.preparcialcomponentes.presentation.components.ErrorMessage
import com.example.preparcialcomponentes.presentation.components.ItemCard
import com.example.preparcialcomponentes.presentation.components.LoadingIndicator
import java.text.NumberFormat
import java.util.Locale

/**
 * Pantalla Compose para HU-01: Consultar y listar planes de ahorro.
 *
 * No realiza llamadas de red ni contiene lógica de negocio; únicamente observa
 * el [StateFlow] de [PlansViewModel] y delega acciones de usuario.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlansScreen(
    viewModel: PlansViewModel,
    onBack: () -> Unit,
    onPlanClick: (SavingPlan) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Planes de Ahorro (HU-01)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadPlans() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Recargar planes"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val currentState = state) {
            is UiState.Loading -> {
                LoadingIndicator(
                    modifier = modifier.padding(paddingValues),
                    message = "Cargando planes de ahorro..."
                )
            }
            is UiState.Empty -> {
                EmptyState(
                    message = "No hay planes de ahorro registrados.",
                    modifier = modifier.padding(paddingValues)
                )
            }
            is UiState.Error -> {
                ErrorMessage(
                    message = currentState.message,
                    modifier = modifier.padding(paddingValues),
                    onRetry = { viewModel.loadPlans() }
                )
            }
            is UiState.Success -> {
                PlansList(
                    plans = currentState.data,
                    onPlanClick = onPlanClick,
                    modifier = modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun PlansList(
    plans: List<SavingPlan>,
    onPlanClick: (SavingPlan) -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
        maximumFractionDigits = 0
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = plans,
            key = { plan -> plan.id }
        ) { plan ->
            ItemCard(onClick = { onPlanClick(plan) }) {
                Text(
                    text = plan.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (plan.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = plan.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Ahorrado",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = currencyFormat.format(plan.currentAmount),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column {
                        Text(
                            text = "Meta",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = currencyFormat.format(plan.targetAmount),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

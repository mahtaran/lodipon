package nl.utwente.smartspaces.lodipon.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime

class AnalyticsViewModel : ViewModel() {
	private val _uiState = MutableStateFlow(AnalyticsUiState(LocalDateTime.now()))
	val uiState = _uiState.asStateFlow()

	fun updateAccelerometer(values: FloatArray?) {
		_uiState.update { currentState ->
			currentState.copy(
				accelerometer = currentState.accelerometer.add(values)
			)
		}
	}

	fun updateLinearAcceleration(values: FloatArray?) {
		_uiState.update { currentState ->
			currentState.copy(
				linearAcceleration = currentState.linearAcceleration.add(values)
			)
		}
	}
}

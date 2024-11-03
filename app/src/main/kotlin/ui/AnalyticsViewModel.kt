package nl.utwente.smartspaces.lodipon.ui

import android.bluetooth.BluetoothManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import nl.utwente.smartspaces.lodipon.data.CHECKPOINT_THRESHOLD
import nl.utwente.smartspaces.lodipon.data.ScannedDevice
import nl.utwente.smartspaces.lodipon.data.beacons
import java.time.LocalDateTime

class AnalyticsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AnalyticsUiState(LocalDateTime.now()))
    val uiState = _uiState.asStateFlow()

    fun updateBluetoothManager(bluetoothManager: BluetoothManager) {
        _uiState.update { currentState -> currentState.copy(bluetoothManager = bluetoothManager) }
    }

    fun updateLocationManager(locationManager: LocationManager) {
        _uiState.update { currentState -> currentState.copy(locationManager = locationManager) }
    }

    fun addScannedDevice(device: ScannedDevice) {
        Log.d("Lodipon", "scanned device: $device")

        if (device.distance <= CHECKPOINT_THRESHOLD) {
            beacons
                .find { it.mac == device.mac }
                ?.let { beacon ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            checkpoints = currentState.checkpoints + (beacon to LocalDateTime.now())
                        )
                    }
                }
        }
    }

    fun updateLocation(location: Location) {
        Log.d("Lodipon", "got location update: $location")

        _uiState.update { currentState ->
            currentState.copy(
                lastUpdate = LocalDateTime.now(),
                lastLocation = location,
                speedWindow = currentState.speedWindow.add(location.speed.toDouble())
            )
        }

        val anomaly = uiState.value.speedWindow.performAnalysis()

        _uiState.update { currentState ->
            currentState.copy(
                anomalyText =
                    if (anomaly != null)
                        "Anomaly detected at index ${anomaly.index} with magnitude ${anomaly.magnitude}"
                    else "No anomalies detected"
            )
        }
    }
}

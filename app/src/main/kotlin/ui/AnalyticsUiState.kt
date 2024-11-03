package nl.utwente.smartspaces.lodipon.ui

import android.bluetooth.BluetoothManager
import android.location.Location
import android.location.LocationManager
import java.time.LocalDateTime
import nl.utwente.smartspaces.lodipon.data.Beacon
import nl.utwente.smartspaces.lodipon.data.SpeedWindow
import nl.utwente.smartspaces.lodipon.data.WINDOW_SIZE

data class AnalyticsUiState(
    val lastUpdate: LocalDateTime,
    val lastLocation: Location? = null,
    val bluetoothManager: BluetoothManager? = null,
    val locationManager: LocationManager? = null,
    val speedWindow: SpeedWindow = SpeedWindow(WINDOW_SIZE),
    val checkpoints: List<Pair<Beacon, LocalDateTime>> = emptyList(),
    val anomalyText: String = "No anomalies detected"
)

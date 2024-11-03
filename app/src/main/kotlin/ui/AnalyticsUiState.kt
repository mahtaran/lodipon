package nl.utwente.smartspaces.lodipon.ui

import android.bluetooth.BluetoothManager
import android.location.Location
import android.location.LocationManager
import nl.utwente.smartspaces.lodipon.data.GeodeticLocation
import nl.utwente.smartspaces.lodipon.data.SpeedWindow
import nl.utwente.smartspaces.lodipon.data.WINDOW_SIZE
import kotlin.time.ComparableTimeMark

data class AnalyticsUiState(
	val lastUpdate: ComparableTimeMark,
	val lastRelativeLocation: GeodeticLocation? = null,
	val lastAbsoluteLocation: Location? = null,
	val bluetoothManager: BluetoothManager? = null,
	val locationManager: LocationManager? = null,
	val beaconLocations: List<Pair<GeodeticLocation, Double>> = emptyList(),
	val speedWindow: SpeedWindow = SpeedWindow(WINDOW_SIZE),
	val anomalyText: String = "No anomalies detected"
)

package nl.utwente.smartspaces.lodipon.ui

import android.bluetooth.BluetoothManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import nl.utwente.smartspaces.lodipon.data.CartesianLocation
import nl.utwente.smartspaces.lodipon.data.GeodeticLocation
import nl.utwente.smartspaces.lodipon.data.ScannedBeacon
import nl.utwente.smartspaces.lodipon.data.beacons
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.time.TimeSource

class AnalyticsViewModel : ViewModel() {
	private val timeSource = TimeSource.Monotonic

	private val _uiState = MutableStateFlow(AnalyticsUiState(timeSource.markNow()))
	val uiState = _uiState.asStateFlow()

	fun updateBluetoothManager(bluetoothManager: BluetoothManager) {
		_uiState.update { currentState ->
			currentState.copy(
				bluetoothManager = bluetoothManager
			)
		}
	}

	fun updateLocationManager(locationManager: LocationManager) {
		_uiState.update { currentState ->
			currentState.copy(
				locationManager = locationManager
			)
		}
	}

	private fun updateRelativeLocation(location: GeodeticLocation) {
		_uiState.update { currentState ->
			currentState.copy(
				lastUpdate = timeSource.markNow(),
				lastRelativeLocation = location
			)
		}
	}

	fun addScannedBeacon(scannedBeacon: ScannedBeacon) {
		Log.d("Lodipon", "scanned beacon: $scannedBeacon")

		beacons.find { it.mac == scannedBeacon.mac }?.let { beacon ->
			_uiState.update { currentState ->
				currentState.copy(
					beaconLocations = currentState.beaconLocations
						+ Pair(beacon.location, scannedBeacon.distance)
				)
			}

			if (uiState.value.beaconLocations.size >= 3) {
				trilaterate()
				_uiState.update { currentState ->
					currentState.copy(
						beaconLocations = currentState.beaconLocations.takeLast(3)
					)
				}
			}
		}
	}

	fun resetScannedBeacons() {
		_uiState.update { currentState ->
			currentState.copy(
				beaconLocations = emptyList()
			)
		}
	}

	private fun trilaterate() {
		val beaconLocations = uiState.value.beaconLocations.takeLast(3)
		val locations = beaconLocations.map { it.first.toCartesian() }
		val distances = beaconLocations.map { it.second }

		val location = trilaterate(locations, distances)
		updateRelativeLocation(location)
	}

	private fun trilaterate(
		locations: List<CartesianLocation>,
		distances: List<Double>
	): GeodeticLocation {
		val l1 = locations[0]
		val l2 = locations[1]
		val l3 = locations[2]

		val d1 = distances[0]
		val d2 = distances[1]
		val d3 = distances[2]

		val l21 = l2 - l1
		val l31 = l3 - l1
		val d = l21.norm()

		val ex = l21 / d
		val i = ex.dot(l31)
		val ey1 = l31 - ex * i
		val ey = ey1 / ey1.norm()
		val ez = ex.cross(ey)
		val j = ey.dot(l31)

		val x = (d1.pow(2) - d2.pow(2) + d.pow(2)) / (2 * d)
		val y = (d1.pow(2) - d3.pow(2) + i.pow(2) + j.pow(2)) / (2 * j) - i / j * x
		val z = sqrt(d1.pow(2) - x.pow(2) - y.pow(2))

		val triPt = l1 + ex * x + ey * y + ez * z

		return triPt.toGeodetic()
	}

	fun updateAbsoluteLocation(location: Location) {
		Log.d("Lodipon", "got location update: $location")

		_uiState.update { currentState ->
			currentState.copy(
				lastUpdate = timeSource.markNow(),
				lastAbsoluteLocation = location,
				speedWindow = currentState.speedWindow.add(location.speed.toDouble())
			)
		}

		val anomaly = uiState.value.speedWindow.performAnalysis()

		_uiState.update { currentState ->
			currentState.copy(
				anomalyText = if (anomaly != null)
					"Anomaly detected at index ${anomaly.index} with magnitude ${anomaly.magnitude}"
				else "No anomalies detected"
			)
		}
	}
}

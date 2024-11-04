package nl.utwente.smartspaces.lodipon.ui.viewmodel

import android.content.Context
import android.location.Location
import android.media.MediaPlayer
import android.net.MacAddress
import androidx.lifecycle.ViewModel
import java.time.LocalDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import nl.utwente.smartspaces.lodipon.R
import nl.utwente.smartspaces.lodipon.data.ScannedDevice
import nl.utwente.smartspaces.lodipon.ui.state.Recommendation
import nl.utwente.smartspaces.lodipon.ui.state.RunState
import nl.utwente.smartspaces.lodipon.ui.state.SettingsState

class LodiponViewModel : ViewModel() {
    private val _settingsState = MutableStateFlow(SettingsState())
    private val _runState = MutableStateFlow(RunState(LocalDateTime.now()))

    val settingsState = _settingsState.asStateFlow()
    val runState = _runState.asStateFlow()

    fun setCheckpointThreshold(threshold: Double) {
        _settingsState.update { currentState -> currentState.copy(checkpointThreshold = threshold) }
    }

    fun setCheckpointDistance(distance: Int) {
        _settingsState.update { currentState -> currentState.copy(checkpointDistance = distance) }
    }

    fun setCheckpointCount(count: Int) {
        _settingsState.update { currentState ->
            if (count < currentState.checkpointCount) {
                currentState.copy(
                    checkpointCount = count,
                    checkpointBeacons = currentState.checkpointBeacons.filterKeys { it < count }
                )
            } else {
                currentState.copy(
                    checkpointCount = count,
                    checkpointBeacons =
                        currentState.checkpointBeacons +
                            (currentState.checkpointCount until count).associateWith { emptySet() }
                )
            }
        }
    }

    fun scanDevice(device: ScannedDevice) {
        _settingsState.update { currentState ->
            currentState.copy(scannedDevices = currentState.scannedDevices + device)
        }
    }

    fun setCheckpointBeacons(checkpointBeacons: Map<Int, Set<MacAddress>>) {
        _settingsState.update { currentState ->
            currentState.copy(checkpointBeacons = checkpointBeacons)
        }
    }

    fun passDevice(device: ScannedDevice) {
        if (device.distance < settingsState.value.checkpointDistance) {
            for ((checkpoint, beacons) in settingsState.value.checkpointBeacons) {
                if (runState.value.lastCheckpoint == checkpoint) continue

                if (beacons.contains(device.mac)) {
                    _runState.update { currentState ->
                        currentState.copy(
                            lastCheckpoint = checkpoint,
                            checkpoints =
                                currentState.checkpoints + (checkpoint to LocalDateTime.now())
                        )
                    }
                }
            }
        }
    }

    fun updateLocation(context: Context, location: Location) {
        _runState.update { currentState ->
            currentState.copy(
                lastUpdate = LocalDateTime.now(),
                lastLocation = location,
                speedWindow = currentState.speedWindow + location.speed.toDouble()
            )
        }

        runState.value.speedWindow.performAnalysis()?.let { anomaly ->
            _runState.update { currentState ->
                // TODO: Implement recommendation logic
                val recommendation = Recommendation.SLOW_DOWN

                val media =
                    MediaPlayer.create(
                        context,
                        if (recommendation == Recommendation.SLOW_DOWN) R.raw.slower
                        else R.raw.faster
                    )
                media.start()
                media.setOnCompletionListener { it.release() }

                currentState.copy(
                    anomalyText =
                        "Anomaly detected at index ${anomaly.index} with magnitude ${anomaly.magnitude}",
                    recommendation = recommendation
                )
            }
        }
            ?: _runState.update { currentState ->
                currentState.copy(
                    anomalyText = "No anomalies detected",
                    recommendation = Recommendation.NONE
                )
            }
    }
}

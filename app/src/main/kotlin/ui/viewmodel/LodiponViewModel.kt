package nl.utwente.smartspaces.lodipon.ui.viewmodel

import android.content.Context
import android.location.Location
import android.media.MediaPlayer
import android.net.MacAddress
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import java.time.LocalDateTime
import kotlin.time.TimeSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.utwente.smartspaces.lodipon.data.ANOMALY_INTERVAL
import nl.utwente.smartspaces.lodipon.data.CHART_LENGTH
import nl.utwente.smartspaces.lodipon.data.ScannedDevice
import nl.utwente.smartspaces.lodipon.ui.state.Checkpoint
import nl.utwente.smartspaces.lodipon.ui.state.Mode
import nl.utwente.smartspaces.lodipon.ui.state.Recommendation
import nl.utwente.smartspaces.lodipon.ui.state.RunState
import nl.utwente.smartspaces.lodipon.ui.state.SettingsState

class LodiponViewModel : ViewModel() {
    private val timeSource = TimeSource.Monotonic

    private val _settingsState = MutableStateFlow(SettingsState())
    private val _runState = MutableStateFlow(RunState(timeSource.markNow()))

    val settingsState = _settingsState.asStateFlow()
    val runState = _runState.asStateFlow()

    val modelProducer = CartesianChartModelProducer()

    fun clearSettings() {
        _settingsState.update { SettingsState() }
    }

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

    fun setMode(mode: Mode) {
        _settingsState.update { currentState -> currentState.copy(mode = mode) }
    }

    fun setConstantSpeed(speed: Double) {
        _settingsState.update { currentState -> currentState.copy(constantSpeed = speed) }
    }

    fun setSpeedUpThreshold(threshold: Double) {
        _settingsState.update { currentState -> currentState.copy(speedUpThreshold = threshold) }
    }

    fun setSlowDownThreshold(threshold: Double) {
        _settingsState.update { currentState -> currentState.copy(slowDownThreshold = threshold) }
    }

    fun passDevice(device: ScannedDevice) {
        if (device.distance < settingsState.value.checkpointDistance) {
            for ((checkpointIndex, beacons) in settingsState.value.checkpointBeacons) {
                if (runState.value.lastCheckpoint == checkpointIndex) continue

                if (beacons.contains(device.mac)) {
                    val checkpoint =
                        Checkpoint(
                            index = checkpointIndex,
                            time = LocalDateTime.now(),
                            distance = settingsState.value.checkpointDistance,
                            previous = runState.value.checkpoints.lastOrNull()
                        )

                    _runState.update { currentState ->
                        currentState.copy(
                            lastCheckpoint = checkpointIndex,
                            checkpoints = currentState.checkpoints.apply { add(checkpoint) }
                        )
                    }
                }
            }
        }
    }

    fun updateLocation(context: Context, location: Location) {
        _runState.update { currentState ->
            currentState.copy(
                speedWindow = currentState.speedWindow + location.speed.toDouble(),
                averageSpeedWindow =
                    currentState.averageSpeedWindow + currentState.speedWindow.average
            )
        }

        if (runState.value.lastAnomaly + ANOMALY_INTERVAL < timeSource.markNow()) {
            testAnomaly(context)
        }

        viewModelScope.launch {
            modelProducer.runTransaction {
                lineSeries { series(runState.value.speedWindow.window.takeLast(CHART_LENGTH)) }
            }
        }
    }

    private fun testAnomaly(context: Context) {
        val targetSpeed =
            when (settingsState.value.mode) {
                Mode.KEEP_AVERAGE -> runState.value.averageSpeedWindow.average
                Mode.CONSTANT_SPEED -> settingsState.value.constantSpeed
            }
        val speed = runState.value.speedWindow.average

        val recommendation =
            when {
                speed < settingsState.value.speedUpThreshold * targetSpeed ->
                    Recommendation.SPEED_UP
                speed > settingsState.value.slowDownThreshold * targetSpeed ->
                    Recommendation.SLOW_DOWN
                else -> Recommendation.NONE
            }

        recommendation.sample?.let { sample ->
            MediaPlayer.create(context, sample).run {
                start()
                setOnCompletionListener(MediaPlayer::release)
            }
        }

        _runState.update { currentState ->
            currentState.copy(lastAnomaly = timeSource.markNow(), recommendation = recommendation)
        }
    }
}

package nl.utwente.smartspaces.lodipon.ui.state

import android.net.MacAddress
import nl.utwente.smartspaces.lodipon.data.ScannedDevice

data class SettingsState(
    val checkpointThreshold: Double = 3.0,
    val checkpointDistance: Int = 0,
    val checkpointCount: Int = 0,
    val scannedDevices: Set<ScannedDevice> = emptySet(),
    val checkpointBeacons: Map<Int, Set<MacAddress>> = emptyMap(),
    val mode: Mode = Mode.KEEP_AVERAGE,
    val constantSpeed: Double = 0.0,
    val speedUpThreshold: Double = 1 - 0.025,
    val slowDownThreshold: Double = 1 + 0.035
)

enum class Mode(val description: String) {
    KEEP_AVERAGE("Stick to your average speed"),
    CONSTANT_SPEED("Keep a constant pace"),
}

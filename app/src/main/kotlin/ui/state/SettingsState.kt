package nl.utwente.smartspaces.lodipon.ui.state

import android.net.MacAddress
import nl.utwente.smartspaces.lodipon.data.ScannedDevice

data class SettingsState(
    val checkpointThreshold: Double = 3.0,
    val checkpointDistance: Int = 0,
    val checkpointCount: Int = 0,
    val scannedDevices: Set<ScannedDevice> = emptySet(),
    val checkpointBeacons: Map<Int, Set<MacAddress>> = emptyMap()
)

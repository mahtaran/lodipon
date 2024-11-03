package nl.utwente.smartspaces.lodipon.data

import android.net.MacAddress
import kotlin.math.pow

data class ScannedBeacon(
	val mac: MacAddress,
	val rssi: Int
) {
	val distance: Double = calculateDistance(rssi)

	/**
	 * Function to calculate distance from RSSI using the path loss model.
	 * Note that this function uses some assumptions like the txPower.
	 * This method calculates the approximate distance to a BLE device using the RSSI and the logarithmic path loss formula.
	 * The txPower is the reference RSSI value (usually around -59 dBm) at 1 meter.
	 * The n value (path loss exponent) is set to 2.0, assuming free space, but you can adjust it based on the environment (e.g., indoor environments will have higher values).
	 *
	 * @param rssi The received signal strength indicator.
	 * @param txPower The reference RSSI value at 1 meter.
	 * @return The approximate distance to the BLE device.
	 */
	private fun calculateDistance(rssi: Int, txPower: Int = -59): Double {
		// Path loss exponent (free space)
		val n = 2.0

		return 10.0.pow((txPower - rssi) / (10 * n))
	}
}

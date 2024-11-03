package nl.utwente.smartspaces.lodipon.data

import android.net.MacAddress

data class Beacon(
	val mac: MacAddress,
	val location: GeodeticLocation
)

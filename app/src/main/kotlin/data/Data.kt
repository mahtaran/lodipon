package nl.utwente.smartspaces.lodipon.data

import android.net.MacAddress

val beacons = listOf(
	Beacon(
		MacAddress.fromString("D9:9F:68:0A:E5:A1"),
		GeodeticLocation(52.2442175, 6.8531765)
	),
	Beacon(
		MacAddress.fromString("D2:FD:9D:13:ED:6B"),
		GeodeticLocation(52.244334, 6.853291),
	),
	Beacon(
		MacAddress.fromString("D3:D8:22:EE:40:90"),
		GeodeticLocation(52.2444687467, 6.853291)
	),
	Beacon(
		MacAddress.fromString("F8:1E:0F:31:85:21"),
		GeodeticLocation(52.244561, 6.853464)
	)
)

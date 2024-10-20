package nl.utwente.smartspaces.lodipon.ui

import nl.utwente.smartspaces.lodipon.data.SlidingWindow
import nl.utwente.smartspaces.lodipon.data.WINDOW_SIZE
import java.time.LocalDateTime

data class AnalyticsUiState(
	val lastUpdate: LocalDateTime,
	val accelerometer: SlidingWindow = SlidingWindow(WINDOW_SIZE),
	val linearAcceleration: SlidingWindow = SlidingWindow(WINDOW_SIZE)
)

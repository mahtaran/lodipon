package nl.utwente.smartspaces.lodipon.ui.state

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.LinkedList
import kotlin.time.ComparableTimeMark
import nl.utwente.smartspaces.lodipon.R
import nl.utwente.smartspaces.lodipon.data.LARGE_WINDOW_SIZE
import nl.utwente.smartspaces.lodipon.data.SMALL_WINDOW_SIZE
import nl.utwente.smartspaces.lodipon.data.SlidingWindow

data class RunState(
    val lastAnomaly: ComparableTimeMark,
    val recommendation: Recommendation = Recommendation.NONE,
    val speedWindow: SlidingWindow = SlidingWindow(SMALL_WINDOW_SIZE),
    val averageSpeedWindow: SlidingWindow = SlidingWindow(LARGE_WINDOW_SIZE),
    val lastCheckpoint: Int? = null,
    val checkpoints: LinkedList<Checkpoint> = LinkedList(),
)

data class Checkpoint(
    val index: Int,
    val time: LocalDateTime,
    val distance: Int,
    val previous: Checkpoint? = null,
) {
    val duration: Double =
        (previous?.let { ChronoUnit.MILLIS.between(it.time, time) } ?: 0) / 1000.0
    val speed: Double = distance / duration
}

enum class Recommendation(val sample: Int? = null) {
    NONE,
    SLOW_DOWN(R.raw.slower),
    SPEED_UP(R.raw.faster)
}

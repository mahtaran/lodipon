package nl.utwente.smartspaces.lodipon.ui.state

import android.location.Location
import java.time.LocalDateTime
import nl.utwente.smartspaces.lodipon.data.SpeedWindow
import nl.utwente.smartspaces.lodipon.data.WINDOW_SIZE

data class RunState(
    val lastUpdate: LocalDateTime,
    val lastLocation: Location? = null,
    val lastCheckpoint: Int? = null,
    val speedWindow: SpeedWindow = SpeedWindow(WINDOW_SIZE),
    val checkpoints: List<Pair<Int, LocalDateTime>> = emptyList(),
    val recommendation: Recommendation = Recommendation.NONE,
    val anomalyText: String = "No anomalies detected"
)

enum class Recommendation {
    NONE,
    SLOW_DOWN,
    SPEED_UP
}

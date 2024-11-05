package nl.utwente.smartspaces.lodipon.data

import kotlin.time.ComparableTimeMark
import kotlin.time.TimeSource

class SlidingWindow(private val windowSize: Int) {
    private val timeSource = TimeSource.Monotonic

    private val _window: MutableList<Double> = ArrayList(windowSize)

    private var lastUpdate: ComparableTimeMark? = null

    val window: List<Double>
        get() = _window.toList()

    val average: Double
        get() = _window.average()

    private fun add(value: Double): SlidingWindow {
        lastUpdate?.let { lastUpdate ->
            if (lastUpdate + MEASURE_INTERVAL > timeSource.markNow()) {
                // Short circuit
                return this
            }
        }

        if (_window.size == windowSize) {
            _window.removeAt(0)
        }
        _window.add(value)

        lastUpdate = timeSource.markNow()

        return this
    }

    operator fun plus(value: Double): SlidingWindow {
        return add(value)
    }
}

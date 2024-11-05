package nl.utwente.smartspaces.lodipon.data

import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.time.ComparableTimeMark
import kotlin.time.TimeSource
import org.jtransforms.fft.DoubleFFT_1D

class SpeedWindow(private val windowSize: Int) {
    data class Anomaly(val index: Int, val magnitude: Double)

    private val timeSource = TimeSource.Monotonic

    private val _window: MutableList<Double> = ArrayList(windowSize)
    private val _history: MutableList<Double> = ArrayList()

    private var lastUpdate: ComparableTimeMark? = null
    private var lastAnomaly: ComparableTimeMark? = null

    val window: List<Double>
        get() = _window.toList()

    val history: List<Double>
        get() = _history.toList()

    private fun add(value: Double): SpeedWindow {
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
        _history.add(value)

        lastUpdate = timeSource.markNow()

        return this
    }

    operator fun plus(value: Double): SpeedWindow {
        return add(value)
    }

    fun performAnalysis(): Anomaly? {
        if (_window.size < windowSize) {
            // Not enough data to perform analysis
            return null
        }

        lastAnomaly?.let { lastAnomaly ->
            if (lastAnomaly + ANOMALY_INTERVAL > timeSource.markNow()) {
                // Short circuit
                return null
            }
        }

        val data = DoubleArray(windowSize * 2)
        _window.forEachIndexed { index, value -> data[index * 2] = value }

        val fft = DoubleFFT_1D(windowSize.toLong())
        fft.realForwardFull(data)

        val magnitudes =
            data
                .asSequence()
                .chunked(2)
                .map { (real, imaginary) -> sqrt(real.pow(2) + imaginary.pow(2)) }
                .toList()

        val maxIndex = magnitudes.indices.maxBy { magnitudes[it] }
        val maxMagnitude = magnitudes[maxIndex]

        return if (maxMagnitude > 5.0) {
            lastAnomaly = timeSource.markNow()
            Anomaly(maxIndex, maxMagnitude)
        } else {
            null
        }
    }
}

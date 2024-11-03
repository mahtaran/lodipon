package nl.utwente.smartspaces.lodipon.data

import org.jtransforms.fft.DoubleFFT_1D
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.time.TimeSource

class SpeedWindow(private val windowSize: Int) {
	data class Anomaly(val index: Int, val magnitude: Double)

	private val window: MutableList<Double> = ArrayList(windowSize)
	private val timeSource = TimeSource.Monotonic
	private var lastUpdate = timeSource.markNow()

	fun add(value: Double): SpeedWindow {
		if (lastUpdate + MEASURE_INTERVAL > timeSource.markNow()) {
			// Short circuit
			return this
		}

		if (window.size == windowSize) {
			window.removeAt(0)
		}
		window.add(value)
		lastUpdate = timeSource.markNow()

		return this
	}

	fun performAnalysis(): Anomaly? {
		if (window.size < windowSize) {
			return null
		}

		val data = DoubleArray(windowSize * 2)
		window.forEachIndexed { index, value -> data[index * 2] = value }

		val fft = DoubleFFT_1D(windowSize.toLong())
		fft.realForwardFull(data)

		val magnitudes = data
			.asSequence()
			.chunked(2)
			.map { (real, imaginary) -> sqrt(real.pow(2) + imaginary.pow(2)) }
			.toList()

		val maxIndex = magnitudes.indices.maxBy { magnitudes[it] } ?: -1
		val maxMagnitude = magnitudes[maxIndex]

		return if (maxMagnitude > 5.0) {
			Anomaly(maxIndex, maxMagnitude)
		} else {
			null
		}
	}
}

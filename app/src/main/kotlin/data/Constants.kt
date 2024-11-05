package nl.utwente.smartspaces.lodipon.data

import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

val SCAN_PERIOD = 10.seconds
val MEASURE_INTERVAL = 200.milliseconds
val ANOMALY_INTERVAL = 5.seconds

const val WINDOW_SIZE = 64
const val EARTH_RADIUS = 6_371_000.0

const val CHART_AXIS_Y_MIN = 0.0
const val CHART_AXIS_Y_MAX = 10.0
const val CHART_LENGTH = 100

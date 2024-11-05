package nl.utwente.smartspaces.lodipon.data

import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

val SCAN_PERIOD = 3.seconds
val MEASURE_INTERVAL = 25.milliseconds
val ANOMALY_INTERVAL = 15.seconds

const val SMALL_WINDOW_SIZE = 8
const val LARGE_WINDOW_SIZE = 128

const val CHART_AXIS_Y_MIN = 0.0
const val CHART_AXIS_Y_MAX = 10.0
const val CHART_LENGTH = 100

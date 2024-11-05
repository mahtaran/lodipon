package nl.utwente.smartspaces.lodipon.ui

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.location.LocationManager
import android.location.LocationRequest
import android.net.MacAddress
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import java.text.DecimalFormat
import nl.utwente.smartspaces.lodipon.data.CHART_AXIS_Y_MAX
import nl.utwente.smartspaces.lodipon.data.CHART_AXIS_Y_MIN
import nl.utwente.smartspaces.lodipon.data.CHART_LENGTH
import nl.utwente.smartspaces.lodipon.data.MEASURE_INTERVAL
import nl.utwente.smartspaces.lodipon.data.ScannedDevice
import nl.utwente.smartspaces.lodipon.ui.state.Recommendation
import nl.utwente.smartspaces.lodipon.ui.viewmodel.LodiponViewModel

@SuppressLint("MissingPermission")
@Composable
fun RunScreen(modifier: Modifier = Modifier, viewModel: LodiponViewModel) {
    val context = LocalContext.current
    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bluetoothLeScanner = bluetoothManager.adapter.bluetoothLeScanner
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    val runState by viewModel.runState.collectAsState()

    var running by remember { mutableStateOf(true) }

    bluetoothLeScanner.startScan(
        object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                result?.let {
                    viewModel.passDevice(
                        ScannedDevice(
                            name = it.device.name ?: "Unknown device",
                            mac = MacAddress.fromString(it.device.address),
                            rssi = it.rssi
                        )
                    )
                }
            }
        }
    )

    locationManager.requestLocationUpdates(
        LocationManager.FUSED_PROVIDER,
        LocationRequest.Builder(MEASURE_INTERVAL.inWholeMilliseconds)
            .setQuality(LocationRequest.QUALITY_HIGH_ACCURACY)
            .build(),
        Runnable::run
    ) { location ->
        if (running) viewModel.updateLocation(context, location)
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.SpaceBetween) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text =
                        "Average speed: ${DecimalFormat("0.00").format(runState.averageSpeedWindow.average)} m/s"
                )
                Text(
                    text =
                        "Speed: ${DecimalFormat("0.00").format(runState.speedWindow.average)} m/s"
                )
                Text(
                    text =
                        when (runState.recommendation) {
                            Recommendation.NONE -> "Keep going"
                            Recommendation.SLOW_DOWN -> "Slow down"
                            Recommendation.SPEED_UP -> "Speed up"
                        }
                )
                CartesianChartHost(
                    chart =
                        rememberCartesianChart(
                            rememberLineCartesianLayer(
                                rangeProvider =
                                    CartesianLayerRangeProvider.fixed(
                                        minX = 0.0,
                                        maxX = CHART_LENGTH.toDouble(),
                                        minY = CHART_AXIS_Y_MIN,
                                        maxY = CHART_AXIS_Y_MAX
                                    )
                            )
                        ),
                    modelProducer = viewModel.modelProducer
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth().height(200.dp).padding(16.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(Modifier.background(color = MaterialTheme.colorScheme.surface)) {
                        Text(text = "Checkpoint", modifier = Modifier.weight(1f).padding(8.dp))
                        Text(text = "Duration", modifier = Modifier.weight(1f).padding(8.dp))
                        Text(text = "Speed", modifier = Modifier.weight(1f).padding(8.dp))
                    }
                }
                items(runState.checkpoints) { checkpoint ->
                    Row {
                        Text(
                            text = "${checkpoint.index + 1}",
                            modifier = Modifier.weight(1f).padding(8.dp)
                        )
                        Text(
                            text = "${DecimalFormat("0.00").format(checkpoint.duration)} s",
                            modifier = Modifier.weight(1f).padding(8.dp)
                        )
                        Text(
                            text = "${DecimalFormat("0.00").format(checkpoint.speed)} m/s",
                            modifier = Modifier.weight(1f).padding(8.dp)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.Bottom
        ) {
            IconButton(onClick = { running = !running }, modifier = Modifier.scale(3f)) {
                if (running)
                    Icon(
                        imageVector = Icons.Filled.Pause,
                        contentDescription = "Pause",
                    )
                else Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = "Resume")
            }
        }
    }
}

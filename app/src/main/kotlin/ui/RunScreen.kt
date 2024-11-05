package nl.utwente.smartspaces.lodipon.ui

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.location.LocationManager
import android.net.MacAddress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import nl.utwente.smartspaces.lodipon.data.CHART_AXIS_Y_MAX
import nl.utwente.smartspaces.lodipon.data.CHART_AXIS_Y_MIN
import nl.utwente.smartspaces.lodipon.data.ScannedDevice
import nl.utwente.smartspaces.lodipon.ui.theme.LodiponTheme
import nl.utwente.smartspaces.lodipon.ui.viewmodel.LodiponViewModel

@SuppressLint("MissingPermission")
@Composable
fun RunScreen(modifier: Modifier = Modifier, viewModel: LodiponViewModel = viewModel()) {
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

    locationManager.requestLocationUpdates(LocationManager.FUSED_PROVIDER, 1000L, 1f) { location ->
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
                Text(text = "Speed: ${runState.lastLocation?.speed} m/s")
                Text(text = runState.anomalyText)
                CartesianChartHost(
                    chart =
                        rememberCartesianChart(
                            rememberLineCartesianLayer(
                                rangeProvider =
                                    CartesianLayerRangeProvider.fixed(
                                        minY = CHART_AXIS_Y_MIN,
                                        maxY = CHART_AXIS_Y_MAX
                                    )
                            )
                        ),
                    modelProducer = viewModel.modelProducer
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                runState.checkpoints.forEach { (checkpoint, time) ->
                    Text(text = "Passed checkpoint $checkpoint at $time")
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

@Preview
@Composable
fun RunScreenPreview() {
    LodiponTheme { RunScreen(modifier = Modifier.fillMaxHeight()) }
}

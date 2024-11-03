package nl.utwente.smartspaces.lodipon.ui

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.res.Configuration
import android.location.LocationListener
import android.location.LocationManager
import android.net.MacAddress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import nl.utwente.smartspaces.lodipon.data.ScannedDevice

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Analytics(padding: PaddingValues, viewModel: AnalyticsViewModel = viewModel()) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    val permissionState =
        rememberMultiplePermissionsState(
            listOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_SCAN
            )
        )

    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    viewModel.updateBluetoothManager(bluetoothManager)
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    viewModel.updateLocationManager(locationManager)

    if (permissionState.allPermissionsGranted) {
        when (configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    Scanner(modifier = Modifier.fillMaxWidth().requiredHeight(96.dp))
                    Speedometer(modifier = Modifier.fillMaxWidth().requiredHeight(96.dp))
                    Visualisation(modifier = Modifier.fillMaxWidth().fillMaxHeight())
                }
            }
            else -> {
                Text(text = "Unsupported orientation", textAlign = TextAlign.Center)
            }
        }
    } else {
        Column(
            modifier = Modifier.padding(32.dp).fillMaxSize().wrapContentHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Location and Bluetooth permissions are needed to localise your device",
                textAlign = TextAlign.Center
            )
            Button(onClick = { permissionState.launchMultiplePermissionRequest() }) {
                Text("Request permission")
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun Scanner(modifier: Modifier = Modifier, viewModel: AnalyticsViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    var scanning by remember { mutableStateOf(false) }

    val scanCallback =
        object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                result?.let {
                    viewModel.addScannedDevice(
                        ScannedDevice(
                            mac = MacAddress.fromString(it.device.address),
                            rssi = it.rssi
                        )
                    )
                }
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                results?.forEach {
                    viewModel.addScannedDevice(
                        ScannedDevice(
                            mac = MacAddress.fromString(it.device.address),
                            rssi = it.rssi
                        )
                    )
                }
            }
        }

    val bluetoothLeScanner = uiState.bluetoothManager?.adapter?.bluetoothLeScanner

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Button(
            onClick = {
                if (!scanning) {
                    bluetoothLeScanner?.startScan(scanCallback)
                    scanning = true
                } else {
                    bluetoothLeScanner?.stopScan(scanCallback)
                    scanning = false
                }
            }
        ) {
            Text(text = if (scanning) "Stop scanning" else "Start scanning")
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun Speedometer(modifier: Modifier = Modifier, viewModel: AnalyticsViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    val locationListener = LocationListener { location -> viewModel.updateLocation(location) }

    uiState.locationManager?.requestLocationUpdates(
        LocationManager.FUSED_PROVIDER,
        1000L,
        1f,
        locationListener
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(text = "Speed: ${uiState.lastLocation?.speed} m/s")
        Text(text = uiState.anomalyText)
    }
}

@Composable
fun Visualisation(modifier: Modifier = Modifier, viewModel: AnalyticsViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        uiState.checkpoints.forEach { (beacon, time) ->
            Row(modifier = Modifier.padding(8.dp)) {
                Text(text = beacon.mac.toString())
                Text(text = time.toString())
            }
        }
    }
}

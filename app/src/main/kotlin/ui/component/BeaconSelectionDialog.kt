package nl.utwente.smartspaces.lodipon.ui.component

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.net.MacAddress
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.utwente.smartspaces.lodipon.data.SCAN_PERIOD
import nl.utwente.smartspaces.lodipon.data.ScannedDevice
import nl.utwente.smartspaces.lodipon.ui.viewmodel.LodiponViewModel

@SuppressLint("MissingPermission")
@Composable
fun BeaconSelectionDialog(
    onDismiss: () -> Unit,
    onConfirmation: (Map<Int, Set<MacAddress>>) -> Unit,
    viewModel: LodiponViewModel
) {
    val context = LocalContext.current
    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bluetoothLeScanner = bluetoothManager.adapter.bluetoothLeScanner

    val scanCallback =
        object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                result?.let {
                    viewModel.scanDevice(
                        ScannedDevice(
                            name = it.device.name ?: "Unknown device",
                            mac = MacAddress.fromString(it.device.address),
                            rssi = it.rssi
                        )
                    )
                }
            }
        }

    Dialog(onDismissRequest = onDismiss) {
        BeaconSelectionCard(
            startScan = { bluetoothLeScanner.startScan(scanCallback) },
            stopScan = { bluetoothLeScanner.stopScan(scanCallback) },
            onDismiss = onDismiss,
            onConfirmation = onConfirmation,
            viewModel = viewModel
        )
    }
}

@Composable
private fun BeaconSelectionCard(
    startScan: () -> Unit,
    stopScan: () -> Unit,
    onDismiss: () -> Unit,
    onConfirmation: (Map<Int, Set<MacAddress>>) -> Unit,
    viewModel: LodiponViewModel
) {
    val settingsState by viewModel.settingsState.collectAsState()

    val scope = rememberCoroutineScope()
    var scanning by remember { mutableStateOf(false) }

    val checkpointBeacons: MutableMap<MacAddress, Int> = remember {
        settingsState.checkpointBeacons
            .flatMap { it.value.map { beacon -> Pair(beacon, it.key) } }
            .toMutableStateMap()
    }

    Card(
        modifier = Modifier.fillMaxWidth().height(450.dp).padding(16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Select beacons", style = MaterialTheme.typography.titleLarge)

                    IconButton(
                        onClick = {
                            if (!scanning) {
                                startScan()
                                scanning = true

                                scope.launch {
                                    delay(SCAN_PERIOD)
                                    stopScan()
                                    scanning = false
                                }
                            } else {
                                stopScan()
                                scanning = false
                            }
                        }
                    ) {
                        if (!scanning) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Start scanning"
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Stop,
                                contentDescription = "Stop scanning"
                            )
                        }
                    }
                }

                Column(
                    modifier =
                        Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    settingsState.scannedDevices
                        .sortedByDescending { it.rssi }
                        .forEach {
                            var expanded by remember { mutableStateOf(false) }

                            Column(
                                modifier =
                                    Modifier.fillMaxWidth().clickable { expanded = !expanded },
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = it.name,
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    checkpointBeacons[it.mac]?.let { checkpoint ->
                                        Text(
                                            text = "#${checkpoint + 1}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                        ?: run {
                                            Text(
                                                text = "N/A",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = it.mac.toString(),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "${it.rssi} dBm",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("None") },
                                        onClick = {
                                            checkpointBeacons.remove(it.mac)
                                            expanded = false
                                        }
                                    )

                                    (0 until settingsState.checkpointCount).forEach { beacon ->
                                        DropdownMenuItem(
                                            text = { Text("Checkpoint ${beacon + 1}") },
                                            onClick = {
                                                checkpointBeacons[it.mac] = beacon
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                TextButton(onClick = onDismiss, modifier = Modifier.padding(8.dp)) {
                    Text("Cancel")
                }
                TextButton(
                    onClick = {
                        onConfirmation(
                            checkpointBeacons.entries.groupBy({ it.value }, { it.key }).mapValues {
                                it.value.toSet()
                            }
                        )
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Confirm")
                }
            }
        }
    }
}

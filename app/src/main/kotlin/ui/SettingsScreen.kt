package nl.utwente.smartspaces.lodipon.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import nl.utwente.smartspaces.lodipon.ui.component.BeaconSelectionDialog
import nl.utwente.smartspaces.lodipon.ui.state.Mode
import nl.utwente.smartspaces.lodipon.ui.viewmodel.LodiponViewModel
import java.text.DecimalFormat
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: LodiponViewModel,
    onStartButtonClicked: () -> Unit = {}
) {
    val settingsState by viewModel.settingsState.collectAsState()

    val openBeaconSelectionDialog = remember { mutableStateOf(false) }

    Column(modifier = modifier, verticalArrangement = Arrangement.SpaceBetween) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Text(
                        text =
                            "Checkpoint threshold: ${DecimalFormat("0.00").format(settingsState.checkpointThreshold)} m"
                    )
                    Slider(
                        value = settingsState.checkpointThreshold.toFloat(),
                        onValueChange = {
                            viewModel.setCheckpointThreshold((it * 100).roundToInt() / 100.0)
                        },
                        valueRange = 0.5f..10f
                    )
                    if (settingsState.checkpointThreshold < 0.5) {
                        Text(
                            text = "Warning: checkpoint threshold needs to be at least 0.50 m",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                Column {
                    Text(
                        text = "Distance between checkpoints: ${settingsState.checkpointDistance} m"
                    )
                    Slider(
                        value = settingsState.checkpointDistance.toFloat(),
                        onValueChange = { viewModel.setCheckpointDistance(it.roundToInt()) },
                        valueRange = 0f..50f
                    )
                    if (settingsState.checkpointDistance < 4 * settingsState.checkpointThreshold) {
                        Text(
                            text =
                                "Warning: distance between checkpoints needs to be at least 4 times the checkpoint threshold",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                Column {
                    Text(text = "Number of checkpoints: ${settingsState.checkpointCount}")
                    Slider(
                        value = settingsState.checkpointCount.toFloat(),
                        onValueChange = { viewModel.setCheckpointCount(it.roundToInt()) },
                        valueRange = 0f..10f
                    )
                    if (settingsState.checkpointCount < 2) {
                        Text(
                            text = "Warning: at least 2 checkpoints are needed",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            HorizontalDivider(thickness = 2.dp)

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Button(onClick = { openBeaconSelectionDialog.value = true }) {
                        Text("Select beacons")
                    }
                    if (settingsState.checkpointBeacons.any { it.value.isEmpty() }) {
                        Text(
                            text =
                                "Warning: at least one beacon needs to be selected for every checkpoint",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                if (openBeaconSelectionDialog.value) {
                    BeaconSelectionDialog(
                        onDismiss = { openBeaconSelectionDialog.value = false },
                        onConfirmation = { selectedBeacons ->
                            openBeaconSelectionDialog.value = false
                            viewModel.setCheckpointBeacons(selectedBeacons)
                        },
                        viewModel = viewModel
                    )
                }

                if (settingsState.checkpointCount > 0) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        (0 until settingsState.checkpointCount).forEach { checkpoint ->
                            Column {
                                Text(
                                    text = "Checkpoint ${checkpoint + 1}",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                settingsState.checkpointBeacons[checkpoint]?.forEach { beacon ->
                                    Text(
                                        text = beacon.toString(),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(thickness = 2.dp)

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Text(text = "Mode")
                    Column(modifier = Modifier.selectableGroup()) {
                        Mode.entries.forEach { mode ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                RadioButton(
                                    selected = settingsState.mode == mode,
                                    onClick = { viewModel.setMode(mode) },
                                    modifier =
                                        Modifier.semantics { contentDescription = mode.description }
                                )
                                Text(
                                    text = mode.description,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }

                if (settingsState.mode == Mode.CONSTANT_SPEED) {
                    Column {
                        Text(
                            text =
                                "Constant speed: ${DecimalFormat("0.00").format(settingsState.constantSpeed)} m/s"
                        )
                        Slider(
                            value = settingsState.constantSpeed.toFloat(),
                            onValueChange = { viewModel.setConstantSpeed(it.toDouble()) },
                            valueRange = 0f..10f
                        )
                        if (settingsState.constantSpeed < 0.1) {
                            Text(
                                text = "Warning: constant speed needs to be at least 0.10 m/s",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }

                Column {
                    Text(
                        text =
                            "Speed thresholds: between ${DecimalFormat("0.0%").format(settingsState.speedUpThreshold)} and ${DecimalFormat("0.0%").format(settingsState.slowDownThreshold)}"
                    )
                    RangeSlider(
                        value =
                            settingsState.speedUpThreshold.toFloat()..settingsState
                                    .slowDownThreshold
                                    .toFloat(),
                        onValueChange = {
                            viewModel.setSpeedUpThreshold(it.start.toDouble())
                            viewModel.setSlowDownThreshold(it.endInclusive.toDouble())
                        },
                        valueRange = 0.9f..1.1f
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = { viewModel.clearSettings() }
            ) {
                Text("Clear")
            }
            Button(
                modifier = Modifier.weight(1f),
                enabled =
                    settingsState.checkpointDistance >= 4 * settingsState.checkpointThreshold &&
                        settingsState.checkpointCount >= 2 &&
                        settingsState.checkpointBeacons.none { it.value.isEmpty() } &&
                        (settingsState.mode != Mode.CONSTANT_SPEED ||
                            settingsState.constantSpeed >= 0.1),
                onClick = onStartButtonClicked
            ) {
                Text("Start")
            }
        }
    }
}

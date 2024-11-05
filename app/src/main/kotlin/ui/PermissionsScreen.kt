package nl.utwente.smartspaces.lodipon.ui

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import nl.utwente.smartspaces.lodipon.ui.theme.LodiponTheme

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionsScreen(permissionsState: MultiplePermissionsState, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "Location and Bluetooth permissions are needed to localise your device")

            Button(onClick = { permissionsState.launchMultiplePermissionRequest() }) {
                Text("Request permission")
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Preview
@Composable
fun PermissionsScreenPreview() {
    val permissionsState =
        rememberMultiplePermissionsState(
            listOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_SCAN
            )
        )

    LodiponTheme {
        PermissionsScreen(permissionsState = permissionsState, modifier = Modifier.fillMaxHeight())
    }
}
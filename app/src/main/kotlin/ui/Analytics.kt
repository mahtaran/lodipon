package nl.utwente.smartspaces.lodipon.ui

import android.Manifest
import android.content.Context
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import nl.utwente.smartspaces.lodipon.data.DEFAULT_LOCATION

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Analytics(
	padding: PaddingValues,
	viewModel: AnalyticsViewModel = viewModel()
) {
	val context = LocalContext.current
	val configuration = LocalConfiguration.current

	val permissionState = rememberMultiplePermissionsState(
		listOf(
			Manifest.permission.ACCESS_COARSE_LOCATION,
			Manifest.permission.ACCESS_FINE_LOCATION
		)
	)

	if (permissionState.allPermissionsGranted) {
		when (configuration.orientation) {
			Configuration.ORIENTATION_PORTRAIT -> {
				Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
					Visualisation(padding)
				}
			}

			Configuration.ORIENTATION_LANDSCAPE -> {
				Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
					Visualisation(padding)
				}
			}

			else -> {
				Text(
					text = "Unsupported orientation",
					textAlign = TextAlign.Center
				)
			}
		}
	} else {
		Column(
			modifier = Modifier
				.padding(32.dp)
				.fillMaxSize()
				.wrapContentHeight(),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.spacedBy(16.dp)
		) {
			Text(
				text = "Location permissions are needed to localise your device",
				textAlign = TextAlign.Center
			)
			Button(
				onClick = { permissionState.launchMultiplePermissionRequest() }
			) {
				Text("Request permission")
			}
		}
	}
}

@Composable
fun Visualisation(
	padding: PaddingValues,
	viewModel: AnalyticsViewModel = viewModel()
) {
	val context = LocalContext.current
	val uiState by viewModel.uiState.collectAsState()

	val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

	val sensorListener = remember {
		object : SensorEventListener {
			override fun onSensorChanged(event: SensorEvent?) {
				event?.let {
					when (it.sensor.type) {
						Sensor.TYPE_ACCELEROMETER -> {
							viewModel.updateAccelerometer(it.values)
						}

						Sensor.TYPE_LINEAR_ACCELERATION -> {
							viewModel.updateLinearAcceleration(it.values)
						}
					}
				}
			}

			override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
				// Do nothing
			}
		}
	}

	DisposableEffect(Unit) {
		sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
			sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_NORMAL)
		}

		sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)?.let {
			sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_NORMAL)
		}

		onDispose {
			sensorManager.unregisterListener(sensorListener)
		}
	}

	val cameraPositionState = rememberCameraPositionState {
		position = CameraPosition.fromLatLngZoom(DEFAULT_LOCATION, 15f)
	}

	GoogleMap(
		modifier = Modifier.fillMaxSize(),
		cameraPositionState = cameraPositionState,
		contentPadding = padding,
		properties = MapProperties(isMyLocationEnabled = true)
	) {
		Marker(
			state = rememberMarkerState(position = LatLng(52.2383, 6.8507)),
			title = "University of Twente",
			snippet = "The most beautiful campus in the Netherlands",
		)
	}
}

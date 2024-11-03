package nl.utwente.smartspaces.lodipon

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import nl.utwente.smartspaces.lodipon.ui.Analytics
import nl.utwente.smartspaces.lodipon.ui.theme.LodiponTheme

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		@SuppressLint("SourceLockedOrientationActivity")
		requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

		setContent {
			LodiponTheme {
				Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
					Analytics(padding)
				}
			}
		}
	}
}

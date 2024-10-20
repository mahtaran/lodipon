package nl.utwente.smartspaces.lodipon

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

		setContent {
			LodiponTheme {
				Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
					Analytics(padding)
				}
			}
		}
	}
}

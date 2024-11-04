package nl.utwente.smartspaces.lodipon

import android.Manifest
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import nl.utwente.smartspaces.lodipon.ui.PermissionsScreen
import nl.utwente.smartspaces.lodipon.ui.RunScreen
import nl.utwente.smartspaces.lodipon.ui.SettingsScreen

enum class LodiponScreen(val title: String) {
    Permissions(title = "Permissions"),
    Settings(title = "Settings"),
    Run(title = "Run")
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LodiponApp(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()

    val permissionsState =
        rememberMultiplePermissionsState(
            listOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.POST_NOTIFICATIONS
            )
        )

    val currentScreen =
        backStackEntry?.destination?.route?.let { route -> LodiponScreen.valueOf(route) }
            ?: if (permissionsState.allPermissionsGranted) {
                LodiponScreen.Settings
            } else {
                LodiponScreen.Permissions
            }

    Scaffold(
        topBar = {
            LodiponAppBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination =
                if (permissionsState.allPermissionsGranted) {
                    LodiponScreen.Settings.name
                } else {
                    LodiponScreen.Permissions.name
                },
            modifier =
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(innerPadding)
        ) {
            composable(route = LodiponScreen.Permissions.name) {
                PermissionsScreen(
                    permissionsState = permissionsState,
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                )
            }

            composable(route = LodiponScreen.Settings.name) {
                SettingsScreen(
                    onStartButtonClicked = { navController.navigate(LodiponScreen.Run.name) },
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                )
            }

            composable(route = LodiponScreen.Run.name) {
                RunScreen(modifier = Modifier.fillMaxSize().padding(16.dp))
            }
        }
    }
}

@Composable
fun LodiponAppBar(
    currentScreen: LodiponScreen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = "Lodipon | " + currentScreen.title,
                color = MaterialTheme.colorScheme.primary
            )
        },
        colors =
            TopAppBarDefaults.mediumTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            ),
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        }
    )
}

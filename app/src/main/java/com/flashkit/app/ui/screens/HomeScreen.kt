package com.flashkit.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.flashkit.app.data.model.JobStatus
import com.flashkit.app.ui.Routes
import com.flashkit.app.ui.components.*
import com.flashkit.app.ui.screens.viewmodel.FlashKitViewModel
import com.flashkit.app.ui.screens.viewmodel.FlashKitViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: FlashKitViewModel = viewModel(
        factory = FlashKitViewModelFactory(LocalContext.current.applicationContext)
    ),
) {
    val deviceState by viewModel.deviceState.collectAsState()
    val permissionTier by viewModel.permissionTier.collectAsState()
    val jobStatus by viewModel.jobStatus.collectAsState()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            "FlashKit",
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "Flash, sideload, and manage devices over OTG",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.SETTINGS) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Device status card
            DeviceStatusCard(
                deviceState = deviceState,
                permissionTier = permissionTier,
                onRefresh = { viewModel.refreshDevice() },
            )

            // Job status (if running)
            AnimatedVisibility(
                visible = jobStatus !is JobStatus.Idle,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                JobOutputView(
                    jobStatus = jobStatus,
                    onCancel = { viewModel.cancelJob() },
                    onDismiss = { viewModel.clearJob() },
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Section: Tools
            Text(
                text = "Tools",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp),
            )

            // Flash partition
            ActionCard(
                icon = Icons.Default.FlashOn,
                title = "Flash Partition",
                subtitle = "Flash an image file to a partition via fastboot",
                onClick = { navController.navigate(Routes.FLASH) },
                enabled = deviceState.isConnected,
            )

            // Sideload
            ActionCard(
                icon = Icons.Default.SystemUpdate,
                title = "Sideload ROM / ZIP",
                subtitle = "Push a ROM or update zip via ADB sideload",
                onClick = { navController.navigate(Routes.SIDELOAD) },
                enabled = deviceState.isConnected,
            )

            // Reboot
            ActionCard(
                icon = Icons.Default.RestartAlt,
                title = "Reboot Device",
                subtitle = "Reboot to system, bootloader, recovery, or fastbootd",
                onClick = { navController.navigate(Routes.REBOOT) },
                enabled = deviceState.isConnected,
            )

            // Raw commands
            ActionCard(
                icon = Icons.Default.Terminal,
                title = "ADB / Fastboot Commands",
                subtitle = "Run preset or custom adb/fastboot commands",
                onClick = { navController.navigate(Routes.COMMANDS) },
                enabled = deviceState.isConnected,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Credits
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "FlashKit v1.0.0-mvp",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "Made by @Shivraj_editx",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

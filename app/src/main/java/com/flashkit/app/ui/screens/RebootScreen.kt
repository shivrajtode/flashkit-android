package com.flashkit.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.flashkit.app.data.model.ConnectionMode
import com.flashkit.app.ui.components.ActionCard
import com.flashkit.app.ui.screens.viewmodel.FlashKitViewModel
import com.flashkit.app.ui.screens.viewmodel.FlashKitViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RebootScreen(
    navController: NavController,
    viewModel: FlashKitViewModel = viewModel(
        factory = FlashKitViewModelFactory(LocalContext.current.applicationContext)
    ),
) {
    val deviceState by viewModel.deviceState.collectAsState()
    var showConfirmTarget by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reboot Target", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Select Reboot Destination",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            ActionCard(
                icon = Icons.Default.RestartAlt,
                title = "System",
                subtitle = "Reboot into normal Android system",
                onClick = { showConfirmTarget = "system" },
                enabled = deviceState.isConnected,
            )

            ActionCard(
                icon = Icons.Default.DeveloperMode,
                title = "Bootloader / Fastboot",
                subtitle = "Reboot into bootloader fastboot mode",
                onClick = { showConfirmTarget = "bootloader" },
                enabled = deviceState.isConnected,
            )

            ActionCard(
                icon = Icons.Default.Build,
                title = "Recovery",
                subtitle = "Reboot into custom or stock recovery",
                onClick = { showConfirmTarget = "recovery" },
                enabled = deviceState.isConnected,
            )

            if (deviceState.connectionMode == ConnectionMode.ADB || deviceState.connectionMode == ConnectionMode.ADB_RECOVERY) {
                ActionCard(
                    icon = Icons.Default.SystemUpdate,
                    title = "Sideload",
                    subtitle = "Reboot directly into ADB sideload mode",
                    onClick = { showConfirmTarget = "sideload" },
                    enabled = deviceState.isConnected,
                )
            }

            ActionCard(
                icon = Icons.Default.FlashOn,
                title = "Fastbootd (Userspace)",
                subtitle = "Reboot into userspace fastbootd for dynamic partitions",
                onClick = { showConfirmTarget = "fastboot" },
                enabled = deviceState.isConnected,
            )
        }
    }

    showConfirmTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { showConfirmTarget = null },
            icon = { Icon(Icons.Default.RestartAlt, contentDescription = null) },
            title = { Text("Confirm Reboot") },
            text = { Text("Reboot the target device to $target?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.rebootDevice(target)
                        showConfirmTarget = null
                        navController.popBackStack()
                    }
                ) {
                    Text("Reboot")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmTarget = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

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
import com.flashkit.app.data.model.JobStatus
import com.flashkit.app.ui.components.JobOutputView
import com.flashkit.app.ui.screens.viewmodel.FlashKitViewModel
import com.flashkit.app.ui.screens.viewmodel.FlashKitViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandsScreen(
    navController: NavController,
    viewModel: FlashKitViewModel = viewModel(
        factory = FlashKitViewModelFactory(LocalContext.current.applicationContext)
    ),
) {
    val deviceState by viewModel.deviceState.collectAsState()
    val jobStatus by viewModel.jobStatus.collectAsState()

    var selectedTool by remember {
        mutableStateOf(
            if (deviceState.connectionMode in listOf(ConnectionMode.FASTBOOT, ConnectionMode.FASTBOOTD)) "fastboot" else "adb"
        )
    }
    var customCommand by remember { mutableStateOf("") }

    val adbPresets = listOf(
        "devices -l" to "List connected devices with details",
        "get-state" to "Print device connection state",
        "shell getprop ro.product.model" to "Check device model",
        "shell getprop ro.build.version.release" to "Check Android version",
        "shell getprop ro.boot.slot_suffix" to "Check current active slot",
        "shell df -h" to "Check device storage partitions",
        "shell mount -t ext4 /dev/block/mapper/vendor /vendor" to "Fix black screen: mount vendor",
        "shell tail -n 100 /tmp/recovery.log" to "Read recovery log",
    )

    val fastbootPresets = listOf(
        "devices" to "List connected fastboot devices",
        "getvar all" to "Dump all bootloader variables",
        "getvar current-slot" to "Check active A/B slot",
        "getvar is-userspace" to "Check if in fastbootd",
        "oem device-info" to "Check bootloader unlock status",
        "flashing get_unlock_ability" to "Check if unlocking is permitted",
        "set_active a" to "Switch active slot to A",
        "set_active b" to "Switch active slot to B",
        "continue" to "Resume normal boot",
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ADB / Fastboot Commands", fontWeight = FontWeight.SemiBold) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Tool selector
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = selectedTool == "adb",
                    onClick = { selectedTool = "adb" },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text("ADB")
                }
                SegmentedButton(
                    selected = selectedTool == "fastboot",
                    onClick = { selectedTool = "fastboot" },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text("Fastboot")
                }
            }

            // Custom command runner
            Text(
                text = "Run Custom Command",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = customCommand,
                    onValueChange = { customCommand = it },
                    label = { Text("Command (without '$selectedTool')") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
                Button(
                    onClick = {
                        if (customCommand.isNotBlank()) {
                            viewModel.runCommand(selectedTool, customCommand.trim())
                        }
                    },
                    modifier = Modifier.height(56.dp),
                    shape = MaterialTheme.shapes.medium,
                    enabled = customCommand.isNotBlank() && jobStatus is JobStatus.Idle
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Run")
                }
            }

            // Presets
            Text(
                text = "Preset Commands",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            val presets = if (selectedTool == "adb") adbPresets else fastbootPresets
            presets.forEach { (cmd, desc) ->
                Card(
                    onClick = { viewModel.runCommand(selectedTool, cmd) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    enabled = jobStatus is JobStatus.Idle,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "$selectedTool $cmd",
                            style = MaterialTheme.typography.titleSmall,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (jobStatus !is JobStatus.Idle) {
                JobOutputView(
                    jobStatus = jobStatus,
                    onCancel = { viewModel.cancelJob() },
                    onDismiss = { viewModel.clearJob() }
                )
            }
        }
    }
}

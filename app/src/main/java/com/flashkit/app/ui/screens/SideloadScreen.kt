package com.flashkit.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.flashkit.app.data.model.ConnectionMode
import com.flashkit.app.data.model.JobStatus
import com.flashkit.app.ui.components.JobOutputView
import com.flashkit.app.ui.screens.viewmodel.FlashKitViewModel
import com.flashkit.app.ui.screens.viewmodel.FlashKitViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SideloadScreen(
    navController: NavController,
    viewModel: FlashKitViewModel = viewModel(
        factory = FlashKitViewModelFactory(LocalContext.current.applicationContext)
    ),
) {
    val context = LocalContext.current
    val jobStatus by viewModel.jobStatus.collectAsState()
    val deviceState by viewModel.deviceState.collectAsState()

    var selectedFilePath by remember { mutableStateOf("") }
    var selectedFileName by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedFilePath = it.toString()
            selectedFileName = it.lastPathSegment ?: "selected file"
        }
    }

    val isSideloadReady = deviceState.connectionMode in listOf(
        ConnectionMode.ADB_SIDELOAD,
        ConnectionMode.ADB,
        ConnectionMode.ADB_RECOVERY,
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sideload ROM / ZIP", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Info card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
                shape = MaterialTheme.shapes.large,
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Column {
                        Text(
                            text = "How ADB Sideload Works",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "The target device must be in sideload mode (Recovery → Apply update → Apply from ADB). " +
                                    "If it's booted normally, FlashKit will reboot it automatically.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
            }

            // Device mode indicator
            val modeText = when (deviceState.connectionMode) {
                ConnectionMode.ADB_SIDELOAD -> "✅ Ready for sideload"
                ConnectionMode.ADB -> "📱 Booted normally — will reboot to sideload"
                ConnectionMode.ADB_RECOVERY -> "🔧 In recovery — select 'Apply from ADB'"
                else -> "❌ Not in ADB mode"
            }

            Text(
                text = modeText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )

            // File picker
            Text(
                text = "Select ZIP File",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            FilledTonalButton(
                onClick = { filePicker.launch("application/zip") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
            ) {
                Icon(Icons.Default.FileOpen, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (selectedFileName.isNotEmpty()) selectedFileName else "Choose ZIP file...")
            }

            if (selectedFilePath.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        text = selectedFilePath,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp),
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    )
                }
            }

            // Sideload button
            val canSideload = selectedFilePath.isNotEmpty() && isSideloadReady
                    && jobStatus is JobStatus.Idle

            Button(
                onClick = { showConfirmDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = canSideload,
                shape = MaterialTheme.shapes.medium,
            ) {
                Icon(Icons.Default.SystemUpdate, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sideload", fontWeight = FontWeight.SemiBold)
            }

            // Job output
            if (jobStatus !is JobStatus.Idle) {
                JobOutputView(
                    jobStatus = jobStatus,
                    onCancel = { viewModel.cancelJob() },
                    onDismiss = { viewModel.clearJob() },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Confirm dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            icon = { Icon(Icons.Default.SystemUpdate, contentDescription = null) },
            title = { Text("Confirm Sideload") },
            text = { Text("Sideload '$selectedFileName' to the target device?") },
            confirmButton = {
                Button(onClick = {
                    showConfirmDialog = false
                    viewModel.sideloadZip(selectedFilePath)
                }) {
                    Text("Sideload")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

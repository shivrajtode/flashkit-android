package com.flashkit.app.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.flashkit.app.data.model.JobStatus
import com.flashkit.app.data.repository.DeviceRepository
import com.flashkit.app.ui.components.*
import com.flashkit.app.ui.screens.viewmodel.FlashKitViewModel
import com.flashkit.app.ui.screens.viewmodel.FlashKitViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashScreen(
    navController: NavController,
    viewModel: FlashKitViewModel = viewModel(
        factory = FlashKitViewModelFactory(LocalContext.current.applicationContext)
    ),
) {
    val context = LocalContext.current
    val jobStatus by viewModel.jobStatus.collectAsState()
    val deviceState by viewModel.deviceState.collectAsState()

    var selectedPartition by remember { mutableStateOf("") }
    var customPartition by remember { mutableStateOf("") }
    var selectedFilePath by remember { mutableStateOf("") }
    var selectedFileName by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedFilePath = getPathFromUri(context, it) ?: it.toString()
            selectedFileName = it.lastPathSegment ?: "selected file"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Flash Partition", fontWeight = FontWeight.SemiBold) },
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
            // Step 1: Select partition
            Text(
                text = "1. Select Partition",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(DeviceRepository.FLASH_PARTITIONS) { partition ->
                    PartitionChip(
                        partition = partition,
                        selected = selectedPartition == partition.name,
                        onSelect = {
                            selectedPartition = partition.name
                            customPartition = ""
                        },
                    )
                }
            }

            // Custom partition input
            OutlinedTextField(
                value = customPartition,
                onValueChange = {
                    customPartition = it
                    if (it.isNotEmpty()) selectedPartition = ""
                },
                label = { Text("Or type a custom partition name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
            )

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            // Step 2: Pick image file
            Text(
                text = "2. Select Image File",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            FilledTonalButton(
                onClick = { filePicker.launch("*/*") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
            ) {
                Icon(Icons.Default.FileOpen, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (selectedFileName.isNotEmpty()) selectedFileName else "Choose file...")
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

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            // Step 3: Flash
            val partition = customPartition.ifEmpty { selectedPartition }
            val isRisky = DeviceRepository.RISKY_PARTITIONS.contains(partition)
            val canFlash = partition.isNotEmpty() && selectedFilePath.isNotEmpty()
                    && jobStatus is JobStatus.Idle

            if (isRisky && partition.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                        )
                        Text(
                            text = "⚠ '$partition' is a critical partition. Flashing it incorrectly can permanently brick the device.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
            }

            Button(
                onClick = { showConfirmDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = canFlash,
                shape = MaterialTheme.shapes.medium,
                colors = if (isRisky) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    )
                } else {
                    ButtonDefaults.buttonColors()
                },
            ) {
                Icon(Icons.Default.FlashOn, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Flash $partition", fontWeight = FontWeight.SemiBold)
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

    // Confirmation dialog
    if (showConfirmDialog) {
        val partition = customPartition.ifEmpty { selectedPartition }
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null) },
            title = { Text("Confirm Flash") },
            text = {
                Text("Flash '$selectedFileName' to partition '$partition'?\n\nThis cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        viewModel.flashPartition(partition, selectedFilePath)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text("Flash")
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

private fun getPathFromUri(context: android.content.Context, uri: Uri): String? {
    // Try to get real file path from content URI
    return try {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex("_data")
                if (idx >= 0) it.getString(idx) else null
            } else null
        }
    } catch (e: Exception) {
        null
    }
}

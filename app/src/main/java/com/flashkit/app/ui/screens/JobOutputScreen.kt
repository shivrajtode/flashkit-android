package com.flashkit.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.flashkit.app.ui.components.JobOutputView
import com.flashkit.app.ui.screens.viewmodel.FlashKitViewModel
import com.flashkit.app.ui.screens.viewmodel.FlashKitViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobOutputScreen(
    navController: NavController,
    viewModel: FlashKitViewModel = viewModel(
        factory = FlashKitViewModelFactory(LocalContext.current.applicationContext)
    ),
) {
    val jobStatus by viewModel.jobStatus.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Console", fontWeight = FontWeight.SemiBold) },
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
        ) {
            JobOutputView(
                jobStatus = jobStatus,
                onCancel = { viewModel.cancelJob() },
                onDismiss = {
                    viewModel.clearJob()
                    navController.popBackStack()
                }
            )
        }
    }
}

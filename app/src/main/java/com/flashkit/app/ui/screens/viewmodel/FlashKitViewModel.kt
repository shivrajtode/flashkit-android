package com.flashkit.app.ui.screens.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.flashkit.app.data.model.*
import com.flashkit.app.data.repository.DeviceRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FlashKitViewModel(context: Context) : ViewModel() {

    private val repository = DeviceRepository(context)

    val deviceState: StateFlow<DeviceState> = repository.deviceState
    val permissionTier: StateFlow<PermissionTier> = repository.permissionTier
    val jobStatus: StateFlow<JobStatus> = repository.jobStatus

    init {
        repository.startDeviceMonitoring(viewModelScope)
    }

    fun refreshDevice() {
        viewModelScope.launch {
            repository.probeDevice()
        }
    }

    fun refreshPermissions() {
        repository.refreshPermissionTier()
    }

    fun flashPartition(partition: String, imagePath: String) {
        repository.flashPartition(partition, imagePath, viewModelScope)
    }

    fun sideloadZip(zipPath: String) {
        repository.sideloadZip(zipPath, viewModelScope)
    }

    fun rebootDevice(target: String) {
        repository.rebootDevice(target, viewModelScope)
    }

    fun runCommand(tool: String, command: String) {
        repository.runCustomCommand(tool, command, viewModelScope)
    }

    fun cancelJob() {
        repository.cancelCurrentJob()
    }

    fun clearJob() {
        repository.clearJobStatus()
    }

    override fun onCleared() {
        super.onCleared()
        repository.stopDeviceMonitoring()
    }
}

class FlashKitViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FlashKitViewModel(context) as T
    }
}

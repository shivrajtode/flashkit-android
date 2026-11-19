package com.flashkit.app.data.model

/** Represents the current state of a connected device */
data class DeviceState(
    val connectionMode: ConnectionMode = ConnectionMode.NONE,
    val serial: String = "",
    val model: String = "",
    val status: String = "",
    val isConnected: Boolean = false,
    val usbDeviceInfo: UsbDeviceInfo? = null,
)

enum class ConnectionMode {
    NONE,
    ADB,
    ADB_RECOVERY,
    ADB_SIDELOAD,
    FASTBOOT,
    FASTBOOTD,
}

data class UsbDeviceInfo(
    val vendorId: Int,
    val productId: Int,
    val manufacturerName: String?,
    val productName: String?,
    val deviceName: String,
)

/** Represents a partition that can be flashed */
data class FlashPartition(
    val name: String,
    val displayName: String,
    val isRisky: Boolean = false,
)

/** Permission tier the app is operating with */
enum class PermissionTier {
    ROOT,
    SHIZUKU,
    ROOTLESS,
}

/** Job status for flash/sideload operations */
sealed class JobStatus {
    data object Idle : JobStatus()
    data class Running(val title: String, val progress: Int = -1, val output: String = "") : JobStatus()
    data class Success(val title: String, val output: String) : JobStatus()
    data class Error(val title: String, val output: String, val exitCode: Int) : JobStatus()
    data class Cancelled(val title: String) : JobStatus()
}

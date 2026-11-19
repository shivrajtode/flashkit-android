package com.flashkit.app.data.repository

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import com.flashkit.app.data.model.*
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Core repository handling device detection and command execution.
 * Supports three tiers: Root, Shizuku, and Rootless (USB Host API).
 */
class DeviceRepository(private val context: Context) {

    private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager

    private val _deviceState = MutableStateFlow(DeviceState())
    val deviceState: StateFlow<DeviceState> = _deviceState.asStateFlow()

    private val _permissionTier = MutableStateFlow(detectPermissionTier())
    val permissionTier: StateFlow<PermissionTier> = _permissionTier.asStateFlow()

    private val _jobStatus = MutableStateFlow<JobStatus>(JobStatus.Idle)
    val jobStatus: StateFlow<JobStatus> = _jobStatus.asStateFlow()

    private var monitorJob: Job? = null
    private var currentJob: Job? = null

    // ─── Permission Detection ─────────────────────────────────────────

    private fun detectPermissionTier(): PermissionTier {
        return when {
            isRooted() -> PermissionTier.ROOT
            isShizukuAvailable() -> PermissionTier.SHIZUKU
            else -> PermissionTier.ROOTLESS
        }
    }

    fun refreshPermissionTier() {
        _permissionTier.value = detectPermissionTier()
    }

    private fun isRooted(): Boolean {
        return try {
            Shell.isAppGrantedRoot() == true
        } catch (e: Exception) {
            false
        }
    }

    private fun isShizukuAvailable(): Boolean {
        return try {
            // Check if Shizuku service is running
            val shizukuClass = Class.forName("rikka.shizuku.Shizuku")
            val pingMethod = shizukuClass.getMethod("pingBinder")
            pingMethod.invoke(null) as Boolean
        } catch (e: Exception) {
            false
        }
    }

    // ─── USB Device Detection ─────────────────────────────────────────

    fun getConnectedUsbDevices(): List<UsbDeviceInfo> {
        return usbManager.deviceList.values.map { device ->
            UsbDeviceInfo(
                vendorId = device.vendorId,
                productId = device.productId,
                manufacturerName = device.manufacturerName,
                productName = device.productName,
                deviceName = device.deviceName,
            )
        }
    }

    fun startDeviceMonitoring(scope: CoroutineScope) {
        monitorJob?.cancel()
        monitorJob = scope.launch {
            while (isActive) {
                probeDevice()
                delay(3000L)
            }
        }
    }

    fun stopDeviceMonitoring() {
        monitorJob?.cancel()
    }

    suspend fun probeDevice() {
        withContext(Dispatchers.IO) {
            val tier = _permissionTier.value
            val state = when (tier) {
                PermissionTier.ROOT -> probeWithRoot()
                PermissionTier.SHIZUKU -> probeWithShizuku()
                PermissionTier.ROOTLESS -> probeRootless()
            }
            _deviceState.value = state
        }
    }

    private fun probeWithRoot(): DeviceState {
        // Check ADB devices
        val adbResult = Shell.cmd("adb devices 2>&1").exec()
        val adbOutput = adbResult.out.joinToString("\n")

        val adbDevice = parseAdbDevices(adbOutput)
        if (adbDevice != null) return adbDevice

        // Check fastboot devices
        val fbResult = Shell.cmd("fastboot devices 2>&1").exec()
        val fbOutput = fbResult.out.joinToString("\n")

        val fbDevice = parseFastbootDevices(fbOutput)
        if (fbDevice != null) return fbDevice

        // Check USB
        val usbDevices = getConnectedUsbDevices()
        if (usbDevices.isNotEmpty()) {
            val usb = usbDevices.first()
            return DeviceState(
                connectionMode = ConnectionMode.NONE,
                status = "USB device detected but not responding to adb/fastboot",
                isConnected = false,
                usbDeviceInfo = usb,
            )
        }

        return DeviceState()
    }

    private fun probeWithShizuku(): DeviceState {
        // Shizuku gives us ADB-level shell access
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", "adb devices 2>&1"))
            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()

            parseAdbDevices(output) ?: DeviceState()
        } catch (e: Exception) {
            DeviceState(status = "Shizuku error: ${e.message}")
        }
    }

    private fun probeRootless(): DeviceState {
        // In rootless mode, we can only detect USB devices via the USB Host API
        val usbDevices = getConnectedUsbDevices()
        if (usbDevices.isNotEmpty()) {
            val usb = usbDevices.first()
            val mode = detectUsbMode(usb)
            return DeviceState(
                connectionMode = mode,
                serial = usb.deviceName,
                model = "${usb.manufacturerName ?: "Unknown"} ${usb.productName ?: ""}".trim(),
                status = when (mode) {
                    ConnectionMode.FASTBOOT -> "Fastboot mode (USB)"
                    ConnectionMode.ADB -> "ADB mode (USB)"
                    else -> "Connected (unknown mode)"
                },
                isConnected = true,
                usbDeviceInfo = usb,
            )
        }
        return DeviceState()
    }

    private fun detectUsbMode(device: UsbDeviceInfo): ConnectionMode {
        // Common USB interface class detection
        // ADB: ff/42/01, Fastboot: ff/42/03
        return when {
            device.vendorId == 0x18d1 && device.productId == 0x4ee0 -> ConnectionMode.FASTBOOT
            device.vendorId == 0x18d1 && device.productId == 0xd001 -> ConnectionMode.ADB
            device.productName?.contains("fastboot", ignoreCase = true) == true -> ConnectionMode.FASTBOOT
            device.productName?.contains("adb", ignoreCase = true) == true -> ConnectionMode.ADB
            else -> ConnectionMode.NONE
        }
    }

    // ─── Command Parsing ──────────────────────────────────────────────

    private fun parseAdbDevices(output: String): DeviceState? {
        val regex = Regex("""^(\S+)\s+(device|offline|unauthorized|recovery|sideload|bootloader)""", RegexOption.MULTILINE)
        val match = regex.find(output) ?: return null

        val serial = match.groupValues[1]
        val state = match.groupValues[2]

        val mode = when (state) {
            "device" -> ConnectionMode.ADB
            "recovery" -> ConnectionMode.ADB_RECOVERY
            "sideload" -> ConnectionMode.ADB_SIDELOAD
            "bootloader" -> ConnectionMode.FASTBOOT
            else -> ConnectionMode.ADB
        }

        val model = if (mode == ConnectionMode.ADB) {
            Shell.cmd("adb -s $serial shell getprop ro.product.model 2>/dev/null")
                .exec().out.firstOrNull()?.trim() ?: ""
        } else ""

        return DeviceState(
            connectionMode = mode,
            serial = serial,
            model = model,
            status = state,
            isConnected = true,
        )
    }

    private fun parseFastbootDevices(output: String): DeviceState? {
        val regex = Regex("""^(\S+)\s+fastboot""", RegexOption.MULTILINE)
        val match = regex.find(output) ?: return null
        val serial = match.groupValues[1]

        // Check if it's fastbootd (userspace fastboot)
        val isFastbootd = try {
            val result = Shell.cmd("fastboot getvar is-userspace 2>&1").exec()
            result.out.any { it.contains("is-userspace: yes", ignoreCase = true) }
        } catch (e: Exception) { false }

        return DeviceState(
            connectionMode = if (isFastbootd) ConnectionMode.FASTBOOTD else ConnectionMode.FASTBOOT,
            serial = serial,
            status = if (isFastbootd) "fastbootd" else "fastboot",
            isConnected = true,
        )
    }

    // ─── Flashing Operations ──────────────────────────────────────────

    fun flashPartition(
        partition: String,
        imagePath: String,
        scope: CoroutineScope,
    ) {
        currentJob?.cancel()
        currentJob = scope.launch(Dispatchers.IO) {
            _jobStatus.value = JobStatus.Running("Flashing $partition...")

            try {
                val result = when (_permissionTier.value) {
                    PermissionTier.ROOT -> {
                        Shell.cmd("fastboot flash $partition '$imagePath' 2>&1").exec()
                    }
                    PermissionTier.SHIZUKU -> {
                        val process = Runtime.getRuntime().exec(
                            arrayOf("sh", "-c", "fastboot flash $partition '$imagePath' 2>&1")
                        )
                        val output = process.inputStream.bufferedReader().readText()
                        process.waitFor()
                        object {
                            val isSuccess = process.exitValue() == 0
                            val out = output.lines()
                            val code = process.exitValue()
                        }
                    }
                    PermissionTier.ROOTLESS -> {
                        // TODO: Implement USB Host API fastboot protocol
                        object {
                            val isSuccess = false
                            val out = listOf("Rootless fastboot flash not yet implemented. Root or Shizuku required.")
                            val code = 1
                        }
                    }
                }

                val output = when (result) {
                    is Shell.Result -> {
                        if (result.isSuccess) {
                            _jobStatus.value = JobStatus.Success(
                                "Flash $partition",
                                result.out.joinToString("\n")
                            )
                        } else {
                            _jobStatus.value = JobStatus.Error(
                                "Flash $partition",
                                result.out.joinToString("\n"),
                                result.code
                            )
                        }
                    }
                    else -> {
                        val r = result as Any
                        val isSuccess = r.javaClass.getMethod("getIsSuccess").invoke(r) as Boolean
                        @Suppress("UNCHECKED_CAST")
                        val out = (r.javaClass.getMethod("getOut").invoke(r) as List<String>).joinToString("\n")
                        val code = r.javaClass.getMethod("getCode").invoke(r) as Int
                        if (isSuccess) {
                            _jobStatus.value = JobStatus.Success("Flash $partition", out)
                        } else {
                            _jobStatus.value = JobStatus.Error("Flash $partition", out, code)
                        }
                    }
                }
            } catch (e: CancellationException) {
                _jobStatus.value = JobStatus.Cancelled("Flash $partition")
            } catch (e: Exception) {
                _jobStatus.value = JobStatus.Error(
                    "Flash $partition",
                    "Error: ${e.message}",
                    -1
                )
            }
        }
    }

    fun sideloadZip(
        zipPath: String,
        scope: CoroutineScope,
    ) {
        currentJob?.cancel()
        currentJob = scope.launch(Dispatchers.IO) {
            _jobStatus.value = JobStatus.Running("Sideloading...")

            try {
                val result = Shell.cmd("adb sideload '$zipPath' 2>&1").exec()
                if (result.isSuccess) {
                    _jobStatus.value = JobStatus.Success(
                        "Sideload",
                        result.out.joinToString("\n")
                    )
                } else {
                    _jobStatus.value = JobStatus.Error(
                        "Sideload",
                        result.out.joinToString("\n"),
                        result.code
                    )
                }
            } catch (e: CancellationException) {
                _jobStatus.value = JobStatus.Cancelled("Sideload")
            } catch (e: Exception) {
                _jobStatus.value = JobStatus.Error(
                    "Sideload",
                    "Error: ${e.message}",
                    -1
                )
            }
        }
    }

    fun rebootDevice(target: String, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            val device = _deviceState.value
            val cmd = when {
                device.connectionMode in listOf(ConnectionMode.FASTBOOT, ConnectionMode.FASTBOOTD) -> {
                    if (target == "system") "fastboot reboot" else "fastboot reboot $target"
                }
                else -> {
                    if (target == "system") "adb reboot" else "adb reboot $target"
                }
            }
            Shell.cmd("$cmd 2>&1").exec()
            delay(2000)
            probeDevice()
        }
    }

    fun runCustomCommand(tool: String, command: String, scope: CoroutineScope) {
        currentJob?.cancel()
        currentJob = scope.launch(Dispatchers.IO) {
            _jobStatus.value = JobStatus.Running("Running: $tool $command")

            try {
                val result = Shell.cmd("$tool $command 2>&1").exec()
                _jobStatus.value = if (result.isSuccess) {
                    JobStatus.Success("$tool $command", result.out.joinToString("\n"))
                } else {
                    JobStatus.Error("$tool $command", result.out.joinToString("\n"), result.code)
                }
            } catch (e: Exception) {
                _jobStatus.value = JobStatus.Error(
                    "$tool $command",
                    "Error: ${e.message}",
                    -1
                )
            }
        }
    }

    fun cancelCurrentJob() {
        currentJob?.cancel()
        currentJob = null
    }

    fun clearJobStatus() {
        _jobStatus.value = JobStatus.Idle
    }

    companion object {
        val FLASH_PARTITIONS = listOf(
            FlashPartition("boot", "Boot"),
            FlashPartition("recovery", "Recovery"),
            FlashPartition("vbmeta", "VBMeta"),
            FlashPartition("vbmeta_system", "VBMeta System"),
            FlashPartition("dtbo", "DTBO"),
            FlashPartition("vendor_boot", "Vendor Boot"),
            FlashPartition("init_boot", "Init Boot"),
            FlashPartition("system", "System"),
            FlashPartition("vendor", "Vendor"),
            FlashPartition("product", "Product"),
            FlashPartition("super", "Super"),
            FlashPartition("userdata", "Userdata", isRisky = true),
        )

        val RISKY_PARTITIONS = setOf(
            "bootloader", "aboot", "abl", "xbl", "xbl_config", "sbl1", "tz", "hyp",
            "rpm", "modem", "persist", "efs", "modemst1", "modemst2", "fsg", "fsc",
            "devinfo", "frp", "keymaster", "cmnlib", "cmnlib64", "devcfg",
        )
    }
}

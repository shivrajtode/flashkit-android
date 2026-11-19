package com.flashkit.app.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.flashkit.app.data.model.*

// ─── Device Status Card ───────────────────────────────────────────────

@Composable
fun DeviceStatusCard(
    deviceState: DeviceState,
    permissionTier: PermissionTier,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (statusText, statusColor, statusIcon) = getDeviceStatus(deviceState)
    val tierLabel = when (permissionTier) {
        PermissionTier.ROOT -> "Root"
        PermissionTier.SHIZUKU -> "Shizuku"
        PermissionTier.ROOTLESS -> "Rootless"
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(28.dp),
                    )
                    Column {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor,
                        )
                        if (deviceState.serial.isNotEmpty()) {
                            Text(
                                text = deviceState.serial,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SuggestionChip(
                        onClick = { },
                        label = { Text(tierLabel, style = MaterialTheme.typography.labelSmall) },
                        shape = MaterialTheme.shapes.small,
                    )
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            }

            if (deviceState.model.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = deviceState.model,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun getDeviceStatus(state: DeviceState): Triple<String, Color, ImageVector> {
    return when (state.connectionMode) {
        ConnectionMode.ADB -> Triple(
            "ADB Connected",
            Color(0xFF8EE2A0),
            Icons.Default.PhoneAndroid,
        )
        ConnectionMode.ADB_RECOVERY -> Triple(
            "Recovery Mode",
            Color(0xFFFFCB6B),
            Icons.Default.BuildCircle,
        )
        ConnectionMode.ADB_SIDELOAD -> Triple(
            "Sideload Mode",
            Color(0xFFFFCB6B),
            Icons.Default.SystemUpdate,
        )
        ConnectionMode.FASTBOOT -> Triple(
            "Fastboot Mode",
            Color(0xFF8EE2A0),
            Icons.Default.FlashOn,
        )
        ConnectionMode.FASTBOOTD -> Triple(
            "Fastbootd Mode",
            Color(0xFF8EE2A0),
            Icons.Default.FlashOn,
        )
        ConnectionMode.NONE -> {
            if (state.usbDeviceInfo != null) {
                Triple(
                    "USB Device Detected",
                    Color(0xFFFFCB6B),
                    Icons.Default.Usb,
                )
            } else {
                Triple(
                    "No Device Connected",
                    Color(0xFF9AA3B2),
                    Icons.Default.UsbOff,
                )
            }
        }
    }
}

// ─── Action Card ──────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.surface,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ─── Job Output View ──────────────────────────────────────────────────

@Composable
fun JobOutputView(
    jobStatus: JobStatus,
    onCancel: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (jobStatus) {
        is JobStatus.Idle -> {}
        is JobStatus.Running -> {
            ElevatedCard(
                modifier = modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = jobStatus.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 3.dp,
                        )
                    }
                    if (jobStatus.progress >= 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { jobStatus.progress / 100f },
                            modifier = Modifier.fillMaxWidth(),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                    if (jobStatus.output.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.small,
                        ) {
                            Text(
                                text = jobStatus.output.takeLast(500),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp),
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    FilledTonalButton(
                        onClick = onCancel,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        ),
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cancel")
                    }
                }
            }
        }
        is JobStatus.Success -> {
            StatusResultCard(
                title = jobStatus.title,
                output = jobStatus.output,
                isSuccess = true,
                onDismiss = onDismiss,
                modifier = modifier,
            )
        }
        is JobStatus.Error -> {
            StatusResultCard(
                title = "${jobStatus.title} (exit ${jobStatus.exitCode})",
                output = jobStatus.output,
                isSuccess = false,
                onDismiss = onDismiss,
                modifier = modifier,
            )
        }
        is JobStatus.Cancelled -> {
            StatusResultCard(
                title = "${jobStatus.title} — Cancelled",
                output = "",
                isSuccess = false,
                onDismiss = onDismiss,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun StatusResultCard(
    title: String,
    output: String,
    isSuccess: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = if (isSuccess) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }
    val contentColor = if (isSuccess) {
        MaterialTheme.colorScheme.onTertiaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = contentColor,
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (output.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = contentColor.copy(alpha = 0.08f),
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        text = output.takeLast(1000),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp),
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = contentColor,
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            FilledTonalButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Dismiss")
            }
        }
    }
}

// ─── Partition Chip ───────────────────────────────────────────────────

@Composable
fun PartitionChip(
    partition: FlashPartition,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onSelect,
        label = { Text(partition.displayName) },
        leadingIcon = if (selected) {
            {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                )
            }
        } else null,
        colors = if (partition.isRisky) {
            FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer,
            )
        } else {
            FilterChipDefaults.filterChipColors()
        },
    )
}

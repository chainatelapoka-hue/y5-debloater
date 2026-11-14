package com.example.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LinkOff
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.AppFilter
import com.example.data.AppStatus
import com.example.data.SafetyLevel
import com.example.data.SystemAppItem
import com.example.shizuku.ShizukuState
import com.example.ui.theme.LocalSemanticColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebloaterScreen(viewModel: DebloaterViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.snackbarEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    BackHandler(enabled = uiState.searchQuery.isNotEmpty()) {
        viewModel.updateSearchQuery("")
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = stringResource(R.string.app_subtitle),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.setShowAdbGuide(true) },
                        modifier = Modifier.testTag("adb_guide_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.HelpOutline,
                            contentDescription = stringResource(R.string.action_adb_guide)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.setShowLogsSheet(true) },
                        modifier = Modifier.testTag("logs_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (uiState.commandLogs.isNotEmpty()) {
                                    Badge {
                                        Text("${uiState.commandLogs.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Terminal,
                                contentDescription = stringResource(R.string.action_logs)
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            viewModel.refreshShizuku()
                            viewModel.refreshApps()
                        },
                        modifier = Modifier.testTag("refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = stringResource(R.string.action_refresh)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp)
            ) {
                ShizukuStatusBanner(
                    shizukuState = uiState.shizukuState,
                    onGrantPermission = viewModel::requestShizukuPermission,
                    onRetry = viewModel::refreshShizuku,
                    onOpenGuide = { viewModel.setShowAdbGuide(true) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                // Search bar
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::updateSearchQuery,
                    placeholder = {
                        Text(
                            text = stringResource(R.string.search_hint),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        AnimatedVisibility(
                            visible = uiState.searchQuery.isNotEmpty(),
                            enter = fadeIn(spring()),
                            exit = fadeOut(spring())
                        ) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("search_input")
                )

                // Filter chips row
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(AppFilter.entries) { filter ->
                        val count = uiState.filterCounts[filter] ?: 0
                        val selected = uiState.activeFilter == filter
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.selectFilter(filter) },
                            label = {
                                Text("${filter.labelBn} ($count)")
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("filter_chip_${filter.name.lowercase()}")
                        )
                    }
                }

                // System App List
                when {
                    uiState.isLoadingApps -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator()
                                Text(
                                    text = "সিস্টেম অ্যাপ লোড হচ্ছে...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    uiState.filteredApps.isEmpty() -> {
                        EmptyAppListState(
                            hasSearch = uiState.searchQuery.isNotBlank(),
                            onReset = {
                                viewModel.updateSearchQuery("")
                                viewModel.selectFilter(AppFilter.ALL)
                            }
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 6.dp,
                                bottom = 24.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(
                                items = uiState.filteredApps,
                                key = { it.packageName }
                            ) { app ->
                                SystemAppCard(
                                    app = app,
                                    iconLoader = viewModel::loadAppIcon,
                                    onClick = { viewModel.openAppDetail(app) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // App Detail / Action Sheet
    val selectedApp = uiState.selectedApp
    if (selectedApp != null) {
        AppDetailBottomSheet(
            app = selectedApp,
            shizukuState = uiState.shizukuState,
            isExecuting = uiState.isExecutingCommand,
            iconLoader = viewModel::loadAppIcon,
            onDismiss = viewModel::closeAppDetail,
            onUninstall = { keepData -> viewModel.uninstallApp(selectedApp, keepData) },
            onDisable = { viewModel.disableApp(selectedApp) },
            onEnable = { viewModel.enableApp(selectedApp) },
            onRestore = { viewModel.restoreApp(selectedApp) },
            onShowMessage = { msg ->
                coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
            }
        )
    }

    if (uiState.showAdbGuide) {
        AdbGuideBottomSheet(
            onDismiss = { viewModel.setShowAdbGuide(false) },
            onShowMessage = { msg ->
                coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
            }
        )
    }

    if (uiState.showLogsSheet) {
        CommandLogsBottomSheet(
            logs = uiState.commandLogs,
            onClear = viewModel::clearLogs,
            onDismiss = { viewModel.setShowLogsSheet(false) }
        )
    }
}

@Composable
private fun ShizukuStatusBanner(
    shizukuState: ShizukuState,
    onGrantPermission: () -> Unit,
    onRetry: () -> Unit,
    onOpenGuide: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current

    val (accentColor, containerBrush, statusText, actionLabel, onAction) = when (shizukuState) {
        is ShizukuState.Checking -> BannerSpec(
            accent = MaterialTheme.colorScheme.primary,
            brush = Brush.horizontalGradient(
                listOf(
                    MaterialTheme.colorScheme.surfaceContainer,
                    MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ),
            text = stringResource(R.string.status_checking),
            actionLabel = null,
            onAction = {}
        )

        is ShizukuState.Connected -> BannerSpec(
            accent = semantic.safe,
            brush = Brush.horizontalGradient(
                listOf(
                    semantic.safeContainer.copy(alpha = 0.72f),
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.48f)
                )
            ),
            text = stringResource(R.string.status_connected),
            actionLabel = null,
            onAction = {}
        )

        is ShizukuState.PermissionRequired -> BannerSpec(
            accent = semantic.warning,
            brush = Brush.horizontalGradient(
                listOf(
                    semantic.warningContainer.copy(alpha = 0.75f),
                    MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ),
            text = "Shizuku চালু আছে — কানেক্ট করতে পারমিশন দিন।",
            actionLabel = stringResource(R.string.action_grant_permission),
            onAction = onGrantPermission
        )

        is ShizukuState.PermissionDenied -> BannerSpec(
            accent = semantic.danger,
            brush = Brush.horizontalGradient(
                listOf(
                    semantic.dangerContainer.copy(alpha = 0.72f),
                    MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ),
            text = stringResource(R.string.status_permission_denied),
            actionLabel = stringResource(R.string.action_grant_permission),
            onAction = onGrantPermission
        )

        is ShizukuState.NotRunning -> BannerSpec(
            accent = semantic.danger,
            brush = Brush.horizontalGradient(
                listOf(
                    semantic.dangerContainer.copy(alpha = 0.65f),
                    MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ),
            text = stringResource(R.string.status_not_running),
            actionLabel = stringResource(R.string.action_retry),
            onAction = onRetry
        )

        is ShizukuState.NotInstalled -> BannerSpec(
            accent = MaterialTheme.colorScheme.onSurfaceVariant,
            brush = Brush.horizontalGradient(
                listOf(
                    MaterialTheme.colorScheme.surfaceContainer,
                    MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ),
            text = stringResource(R.string.status_not_installed),
            actionLabel = stringResource(R.string.action_adb_guide),
            onAction = onOpenGuide
        )
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("shizuku_status_card")
    ) {
        Row(
            modifier = Modifier
                .background(containerBrush)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                val icon = when (shizukuState) {
                    is ShizukuState.Connected -> Icons.Outlined.CheckCircle
                    is ShizukuState.PermissionRequired,
                    is ShizukuState.PermissionDenied -> Icons.Rounded.LockOpen
                    is ShizukuState.Checking -> Icons.Outlined.Info
                    else -> Icons.Outlined.LinkOff
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (shizukuState is ShizukuState.Connected) {
                    val modeText = if (shizukuState.uid == 0) "Root UID 0" else "ADB Shell UID ${shizukuState.uid}"
                    val serviceText = if (shizukuState.serviceBound) "Ready" else "Binding..."
                    Text(
                        text = "$modeText • API v${shizukuState.apiVersion} • $serviceText",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor
                    )
                }
            }

            if (actionLabel != null) {
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = MaterialTheme.colorScheme.surface
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("shizuku_action_button")
                ) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

private data class BannerSpec(
    val accent: androidx.compose.ui.graphics.Color,
    val brush: Brush,
    val text: String,
    val actionLabel: String?,
    val onAction: () -> Unit
)

@Composable
private fun SystemAppCard(
    app: SystemAppItem,
    iconLoader: suspend (String) -> Drawable?,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("app_item_${app.packageName}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppIconBadge(
                packageName = app.packageName,
                appName = app.name,
                iconLoader = iconLoader,
                size = 44.dp
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = app.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(status = app.status)
                    if (app.safetyLevel != SafetyLevel.UNKNOWN) {
                        SafetyBadge(level = app.safetyLevel)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: AppStatus) {
    val semantic = LocalSemanticColors.current
    val (label, bg, fg) = when (status) {
        AppStatus.INSTALLED -> Triple(
            "Installed",
            semantic.safeContainer,
            semantic.safe
        )
        AppStatus.DISABLED -> Triple(
            "Disabled",
            semantic.warningContainer,
            semantic.warning
        )
        AppStatus.UNINSTALLED -> Triple(
            "Removed",
            semantic.dangerContainer,
            semantic.danger
        )
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bg
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = fg,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun SafetyBadge(level: SafetyLevel) {
    val semantic = LocalSemanticColors.current
    val (label, bg, fg) = when (level) {
        SafetyLevel.SAFE_BLOAT -> Triple(
            "Safe Bloat",
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        SafetyLevel.CAUTION -> Triple(
            "Caution",
            semantic.warningContainer,
            semantic.warning
        )
        SafetyLevel.CRITICAL -> Triple(
            "System Core",
            semantic.dangerContainer,
            semantic.danger
        )
        SafetyLevel.UNKNOWN -> Triple(
            "System",
            MaterialTheme.colorScheme.surfaceContainerHigh,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (level == SafetyLevel.SAFE_BLOAT) {
                Icon(
                    imageVector = Icons.Rounded.Shield,
                    contentDescription = null,
                    tint = fg,
                    modifier = Modifier.size(12.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = fg
            )
        }
    }
}

@Composable
fun AppIconBadge(
    packageName: String,
    appName: String,
    iconLoader: suspend (String) -> Drawable?,
    size: Dp
) {
    val bitmap by produceState<ImageBitmap?>(initialValue = null, key1 = packageName) {
        value = withContext(Dispatchers.IO) {
            iconLoader(packageName)?.toImageBitmap()
        }
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center
    ) {
        val loaded = bitmap
        if (loaded != null) {
            Image(
                bitmap = loaded,
                contentDescription = "$appName icon",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Android,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(size * 0.55f)
            )
        }
    }
}

@Composable
private fun EmptyAppListState(
    hasSearch: Boolean,
    onReset: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(44.dp)
            )
            Text(
                text = if (hasSearch) {
                    "খুঁজে পাওয়া যায়নি। অন্য নাম বা প্যাকেজ নেম চেষ্টা করুন।"
                } else {
                    "এই ফিল্টারে কোনো সিস্টেম অ্যাপ নেই।"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FilledTonalButton(
                onClick = onReset,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text("সব অ্যাপ দেখুন")
            }
        }
    }
}

private fun Drawable.toImageBitmap(): ImageBitmap {
    if (this is BitmapDrawable && bitmap != null) {
        return bitmap.asImageBitmap()
    }
    val w = intrinsicWidth.coerceAtLeast(1).coerceAtMost(192)
    val h = intrinsicHeight.coerceAtLeast(1).coerceAtMost(192)
    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bmp.asImageBitmap()
}

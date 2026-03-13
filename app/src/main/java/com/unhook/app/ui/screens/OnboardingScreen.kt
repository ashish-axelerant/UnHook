// UnHook — 6-step onboarding flow (welcome, profile, pairing, app selection, permissions, confirmation)
package com.unhook.app.ui.screens

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.unhook.app.R
import com.unhook.app.ui.components.AppIcon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val avatarOptions = listOf("😊", "🦁", "🐯", "🦊", "🐺", "🦅", "🏄", "🧗")

private val popularSocialPackages = setOf(
    "com.twitter.android",
    "com.x.android",
    "com.facebook.katana",
    "com.instagram.android",
    "com.google.android.youtube",
    "com.zhiliaoapp.musically",
    "com.snapchat.android",
    "com.reddit.frontpage",
    "com.pinterest",
    "com.linkedin.android",
)

@Composable
fun OnboardingScreen(
    onComplete: (userName: String, userAvatar: String, partnerName: String, partnerAvatar: String, pairingCode: String, selectedApps: Map<String, String>) -> Unit,
) {
    var step by remember { mutableIntStateOf(0) }
    var userName by remember { mutableStateOf("") }
    var userAvatar by remember { mutableStateOf(avatarOptions[0]) }
    var partnerName by remember { mutableStateOf("") }
    var partnerAvatar by remember { mutableStateOf(avatarOptions[1]) }
    var pairingCode by remember { mutableStateOf("") }
    var isCreatingRoom by remember { mutableStateOf<Boolean?>(null) }
    val selectedApps = remember { mutableStateMapOf<String, String>() }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Back button + progress dots
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp),
                contentAlignment = Alignment.Center,
            ) {
                // Back arrow — hidden on welcome step
                if (step > 0) {
                    IconButton(
                        onClick = { step-- },
                        modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                // Progress dots centred in the row
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                repeat(6) { index ->
                    val dotSize by animateDpAsState(
                        targetValue = if (index == step) 10.dp else 8.dp,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "dot_$index",
                    )
                    val dotColor by animateColorAsState(
                        targetValue = if (index <= step) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        label = "dotColor_$index",
                    )
                    Box(
                        modifier = Modifier
                            .size(dotSize)
                            .clip(CircleShape)
                            .background(dotColor),
                    )
                    if (index < 5) Spacer(modifier = Modifier.width(8.dp))
                }
                } // Row (dots)
            } // Box (back + dots)

            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    val direction = if (targetState > initialState) 1 else -1
                    (slideInHorizontally { it * direction } + fadeIn()) togetherWith
                        (slideOutHorizontally { -it * direction } + fadeOut())
                },
                label = "onboarding",
            ) { currentStep ->
                when (currentStep) {
                0 -> WelcomeStep(onNext = { step = 1 })
                1 -> ProfileStep(
                    name = userName,
                    onNameChange = { userName = it },
                    selectedAvatar = userAvatar,
                    onAvatarChange = { userAvatar = it },
                    onNext = { step = 2 },
                )
                2 -> PairingStep(
                    isCreatingRoom = isCreatingRoom,
                    onCreateRoom = {
                        isCreatingRoom = true
                        pairingCode = (100000..999999).random().toString()
                    },
                    onHaveCode = { isCreatingRoom = false },
                    pairingCode = pairingCode,
                    onCodeChange = { pairingCode = it },
                    partnerName = partnerName,
                    onPartnerNameChange = { partnerName = it },
                    partnerAvatar = partnerAvatar,
                    onPartnerAvatarChange = { partnerAvatar = it },
                    onNext = { step = 3 },
                )
                3 -> AppSelectionStep(
                    selectedApps = selectedApps,
                    onToggle = { pkg, name ->
                        if (selectedApps.containsKey(pkg)) selectedApps.remove(pkg)
                        else selectedApps[pkg] = name
                    },
                    onNext = { step = 4 },
                )
                4 -> PermissionsStep(onNext = { step = 5 })
                5 -> ConfirmationStep(
                    partnerName = partnerName,
                    onStart = { onComplete(userName, userAvatar, partnerName, partnerAvatar, pairingCode, selectedApps.toMap()) },
                )
            }
        } // AnimatedContent
        } // Column
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppSelectionStep(
    selectedApps: Map<String, String>,
    onToggle: (packageName: String, appName: String) -> Unit,
    onNext: () -> Unit,
) {
    val context = LocalContext.current
    val view = LocalView.current
    var hasPreSelected by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    val installedApps by produceState<List<InstalledAppInfo>>(initialValue = emptyList()) {
        value = withContext(Dispatchers.IO) {
            val pm = context.packageManager
            pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { appInfo ->
                    val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    val isUpdatedSystem = (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                    (!isSystem || isUpdatedSystem) && appInfo.packageName != "com.unhook.app"
                }
                .map { appInfo ->
                    InstalledAppInfo(
                        packageName = appInfo.packageName,
                        appName = pm.getApplicationLabel(appInfo).toString(),
                    )
                }
                .sortedBy { it.appName }
        }
    }

    LaunchedEffect(installedApps) {
        if (installedApps.isNotEmpty() && !hasPreSelected) {
            hasPreSelected = true
            installedApps
                .filter { it.packageName in popularSocialPackages }
                .forEach { onToggle(it.packageName, it.appName) }
        }
    }

    // Derived lists — recomputed only when inputs change
    val selectedList = remember(installedApps, selectedApps.size) {
        installedApps.filter { selectedApps.containsKey(it.packageName) }
    }
    val unselectedList = remember(installedApps, selectedApps.size) {
        installedApps.filter { !selectedApps.containsKey(it.packageName) }
    }
    val filteredApps = remember(query, installedApps, selectedApps.size) {
        if (query.isBlank()) emptyList()
        else {
            val q = query.trim()
            installedApps
                .filter {
                    it.appName.contains(q, ignoreCase = true) ||
                        it.packageName.contains(q, ignoreCase = true)
                }
                // Selected apps float to top within search results
                .sortedWith(compareByDescending { selectedApps.containsKey(it.packageName) })
        }
    }

    val handleToggle: (InstalledAppInfo) -> Unit = { appInfo ->
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        onToggle(appInfo.packageName, appInfo.appName)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
    ) {
        // Header
        Column(
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 12.dp),
        ) {
            Text(
                text = stringResource(R.string.onboarding_apps_title),
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.onboarding_apps_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Search bar
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text(stringResource(R.string.blocked_apps_search_hint)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 4.dp),
        )

        // App list
        if (installedApps.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                if (query.isNotBlank()) {
                    // ── Search mode ──────────────────────────────────────────────
                    if (filteredApps.isEmpty()) {
                        item(key = "no_results") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "No apps match \"$query\"",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    } else {
                        items(filteredApps, key = { it.packageName }) { appInfo ->
                            AppSelectionRow(
                                appInfo = appInfo,
                                isSelected = selectedApps.containsKey(appInfo.packageName),
                                onToggle = { handleToggle(appInfo) },
                            )
                        }
                    }
                } else {
                    // ── Browse mode: selected pinned to top ───────────────────────
                    if (selectedList.isNotEmpty()) {
                        stickyHeader(key = "header_selected") {
                            AppListSectionHeader(
                                label = "Selected · ${selectedList.size}",
                                isPrimary = true,
                            )
                        }
                        items(selectedList, key = { "sel_${it.packageName}" }) { appInfo ->
                            AppSelectionRow(
                                appInfo = appInfo,
                                isSelected = true,
                                onToggle = { handleToggle(appInfo) },
                            )
                        }
                    }

                    if (unselectedList.isNotEmpty()) {
                        stickyHeader(key = "header_all") {
                            AppListSectionHeader(
                                label = if (selectedList.isEmpty()) "All Apps" else "More Apps",
                                isPrimary = false,
                            )
                        }
                        items(unselectedList, key = { it.packageName }) { appInfo ->
                            AppSelectionRow(
                                appInfo = appInfo,
                                isSelected = false,
                                onToggle = { handleToggle(appInfo) },
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }

        // Continue button
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            enabled = selectedApps.isNotEmpty(),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(text = stringResource(R.string.onboarding_apps_continue, selectedApps.size))
        }
    }
}

@Composable
private fun AppListSectionHeader(label: String, isPrimary: Boolean) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = if (isPrimary) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 8.dp),
    )
}

@Composable
private fun AppSelectionRow(
    appInfo: InstalledAppInfo,
    isSelected: Boolean,
    onToggle: () -> Unit,
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        else
            Color.Transparent,
        label = "rowBg",
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .clickable { onToggle() }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(packageName = appInfo.packageName, modifier = Modifier.size(44.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = appInfo.appName,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle() },
        )
    }
}

@Composable
private fun PermissionsStep(onNext: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasAccessibility by remember { mutableStateOf(isAccessibilityEnabled(context)) }
    var hasOverlay by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var hasUsageStats by remember { mutableStateOf(isUsageStatsGranted(context)) }
    var hasNotifications by remember { mutableStateOf(isNotificationEnabled(context)) }

    // Re-check every time user returns from a Settings screen
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            hasAccessibility = isAccessibilityEnabled(context)
            hasOverlay = Settings.canDrawOverlays(context)
            hasUsageStats = isUsageStatsGranted(context)
            hasNotifications = isNotificationEnabled(context)
        }
    }

    val grantedCount = listOf(hasAccessibility, hasOverlay, hasUsageStats, hasNotifications).count { it }
    val requiredGranted = hasAccessibility && hasOverlay

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // Header
        Column(
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 8.dp),
        ) {
            Text(
                text = stringResource(R.string.onboarding_permissions_title),
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.onboarding_permissions_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Mini progress indicator — shows how many permissions are granted
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                repeat(4) { index ->
                    val filled = index < grantedCount
                    Box(
                        modifier = Modifier
                            .size(if (filled) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (filled) MaterialTheme.colorScheme.tertiary
                                else MaterialTheme.colorScheme.surfaceVariant,
                            ),
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$grantedCount of 4 granted",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (grantedCount > 0) MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Permission cards
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OnboardingPermissionCard(
                icon = Icons.Filled.Accessibility,
                title = stringResource(R.string.settings_accessibility),
                description = stringResource(R.string.settings_accessibility_desc),
                isGranted = hasAccessibility,
                isRequired = true,
                onGrant = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
            )
            OnboardingPermissionCard(
                icon = Icons.Filled.Layers,
                title = stringResource(R.string.settings_overlay),
                description = stringResource(R.string.settings_overlay_desc),
                isGranted = hasOverlay,
                isRequired = true,
                onGrant = {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}"),
                        ),
                    )
                },
            )
            OnboardingPermissionCard(
                icon = Icons.Filled.QueryStats,
                title = stringResource(R.string.settings_usage_stats),
                description = stringResource(R.string.settings_usage_stats_desc),
                isGranted = hasUsageStats,
                isRequired = false,
                onGrant = { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) },
            )
            OnboardingPermissionCard(
                icon = Icons.Filled.Notifications,
                title = stringResource(R.string.settings_notifications),
                description = stringResource(R.string.settings_notifications_desc),
                isGranted = hasNotifications,
                isRequired = false,
                onGrant = {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                    context.startActivity(intent)
                },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Warning — only visible if required permissions are missing
        AnimatedVisibility(
            visible = !requiredGranted,
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.onboarding_permissions_warning),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(12.dp),
            colors = if (requiredGranted)
                ButtonDefaults.buttonColors()
            else
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                ),
        ) {
            Text(
                text = if (requiredGranted)
                    stringResource(R.string.onboarding_permissions_continue_ready)
                else
                    stringResource(R.string.onboarding_permissions_continue_skip),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun OnboardingPermissionCard(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    isRequired: Boolean,
    onGrant: () -> Unit,
) {
    val bgColor by animateColorAsState(
        targetValue = if (isGranted)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else
            MaterialTheme.colorScheme.surfaceVariant,
        label = "permCardBg",
    )
    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Icon in a soft circle container
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (isGranted) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = if (isGranted) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRequired)
                            stringResource(R.string.onboarding_permissions_required)
                        else
                            stringResource(R.string.onboarding_permissions_recommended),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isRequired) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .background(
                                color = if (isRequired)
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
                                else
                                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(4.dp),
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            if (isGranted) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = stringResource(R.string.settings_permission_granted),
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(28.dp),
                )
            } else {
                FilledTonalButton(
                    onClick = onGrant,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 14.dp,
                        vertical = 6.dp,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_permissions_grant),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeStep(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "🎣", fontSize = 80.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.onboarding_welcome_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.onboarding_welcome_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(text = stringResource(R.string.onboarding_get_started))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileStep(
    name: String,
    onNameChange: (String) -> Unit,
    selectedAvatar: String,
    onAvatarChange: (String) -> Unit,
    onNext: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.onboarding_profile_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.onboarding_name_hint)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.onboarding_pick_avatar),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(12.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            avatarOptions.forEach { emoji ->
                AvatarChip(
                    emoji = emoji,
                    isSelected = emoji == selectedAvatar,
                    onClick = { onAvatarChange(emoji) },
                )
            }
        }
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
            enabled = name.isNotBlank(),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(text = stringResource(R.string.onboarding_continue))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PairingStep(
    isCreatingRoom: Boolean?,
    onCreateRoom: () -> Unit,
    onHaveCode: () -> Unit,
    pairingCode: String,
    onCodeChange: (String) -> Unit,
    partnerName: String,
    onPartnerNameChange: (String) -> Unit,
    partnerAvatar: String,
    onPartnerAvatarChange: (String) -> Unit,
    onNext: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.onboarding_pairing_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(32.dp))

        when (isCreatingRoom) {
            null -> {
                Button(
                    onClick = onCreateRoom,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(text = stringResource(R.string.onboarding_create_room))
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onHaveCode,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(text = stringResource(R.string.onboarding_have_code))
                }
            }
            true -> {
                Text(
                    text = stringResource(R.string.onboarding_your_code),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = pairingCode,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 8.sp,
                )
                Spacer(modifier = Modifier.height(24.dp))
                PartnerInfoFields(
                    partnerName = partnerName,
                    onPartnerNameChange = onPartnerNameChange,
                    partnerAvatar = partnerAvatar,
                    onPartnerAvatarChange = onPartnerAvatarChange,
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = partnerName.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(text = stringResource(R.string.onboarding_continue))
                }
            }
            false -> {
                OutlinedTextField(
                    value = pairingCode,
                    onValueChange = { if (it.length <= 6) onCodeChange(it) },
                    label = { Text(stringResource(R.string.onboarding_enter_code_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(24.dp))
                PartnerInfoFields(
                    partnerName = partnerName,
                    onPartnerNameChange = onPartnerNameChange,
                    partnerAvatar = partnerAvatar,
                    onPartnerAvatarChange = onPartnerAvatarChange,
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = pairingCode.length == 6 && partnerName.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(text = stringResource(R.string.onboarding_connect))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PartnerInfoFields(
    partnerName: String,
    onPartnerNameChange: (String) -> Unit,
    partnerAvatar: String,
    onPartnerAvatarChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = partnerName,
        onValueChange = onPartnerNameChange,
        label = { Text(stringResource(R.string.onboarding_partner_name_hint)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = stringResource(R.string.onboarding_partner_avatar),
        style = MaterialTheme.typography.titleMedium,
    )
    Spacer(modifier = Modifier.height(8.dp))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        avatarOptions.forEach { emoji ->
            AvatarChip(
                emoji = emoji,
                isSelected = emoji == partnerAvatar,
                onClick = { onPartnerAvatarChange(emoji) },
            )
        }
    }
}

@Composable
private fun ConfirmationStep(
    partnerName: String,
    onStart: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "🎉", fontSize = 80.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.onboarding_all_set),
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.onboarding_paired_with, partnerName),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.onboarding_lets_go),
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            Text(text = stringResource(R.string.onboarding_start))
        }
    }
}

@Composable
private fun AvatarChip(
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val view = LocalView.current
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant,
            )
            .then(
                if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                else Modifier,
            )
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(text = emoji, fontSize = 24.sp)
    }
}

// UnHook — Screen to manage which apps are blocked/monitored
package com.unhook.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unhook.app.R
import com.unhook.app.data.db.BlockedAppDao
import com.unhook.app.data.model.BlockedApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private val defaultAppEmojis = mapOf(
    "com.twitter.android" to "🐦",
    "com.facebook.katana" to "📘",
    "com.instagram.android" to "📷",
    "com.google.android.youtube" to "▶️",
    "com.zhiliaoapp.musically" to "🎵",
    "com.snapchat.android" to "👻",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockedAppsScreen(
    blockedAppDao: BlockedAppDao,
    onBack: () -> Unit,
) {
    val apps by blockedAppDao.getAll().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.blocked_apps_title)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
            },
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.blocked_apps_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(apps, key = { it.packageName }) { app ->
                BlockedAppItem(
                    app = app,
                    emoji = defaultAppEmojis[app.packageName] ?: "📱",
                    onToggle = { enabled ->
                        scope.launch(Dispatchers.IO) {
                            blockedAppDao.update(app.copy(isEnabled = enabled))
                        }
                    },
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun BlockedAppItem(
    app: BlockedApp,
    emoji: String,
    onToggle: (Boolean) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = emoji, fontSize = 28.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = app.isEnabled,
                onCheckedChange = onToggle,
            )
        }
    }
}

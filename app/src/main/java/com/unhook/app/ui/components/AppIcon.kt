// UnHook — Shared composable that renders the launcher icon for a given package name
package com.unhook.app.ui.components

import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap

/**
 * Loads and displays the real launcher icon for [packageName].
 * Falls back to a generic Apps icon if the package is not installed.
 */
@Composable
fun AppIcon(packageName: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap = remember(packageName) {
        try {
            context.packageManager
                .getApplicationIcon(packageName)
                .toBitmap()
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = modifier,
        )
    } else {
        Icon(
            imageVector = Icons.Filled.Apps,
            contentDescription = null,
            modifier = modifier,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// UnHook — Transparent activity that shows the intervention overlay on top of blocked apps
package com.unhook.app.ui.overlay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.unhook.app.ui.theme.UnHookTheme
import kotlinx.coroutines.launch

class InterventionActivity : ComponentActivity() {

    private lateinit var viewModel: InterventionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[InterventionViewModel::class.java]

        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: run {
            finish()
            return
        }

        viewModel.showIntervention(packageName)

        setContent {
            UnHookTheme {
                InterventionOverlay(viewModel = viewModel)
            }
        }

        // Auto-close when intervention is dismissed
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                if (!state.isVisible && state.packageName.isNotEmpty()) {
                    finish()
                }
            }
        }
    }

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
    }
}

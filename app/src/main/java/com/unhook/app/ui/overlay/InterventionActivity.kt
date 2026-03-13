// UnHook — Transparent activity that shows the intervention overlay on top of blocked apps
package com.unhook.app.ui.overlay

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.unhook.app.ui.theme.UnHookTheme
import kotlinx.coroutines.launch

class InterventionActivity : ComponentActivity() {

    private lateinit var viewModel: InterventionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Intercept back button — always go Home instead of back into the blocked app
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                startActivity(
                    Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_HOME)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    },
                )
                finish()
            }
        })

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
                    if (state.navigateHome) {
                        // User resisted — send to Home so they don't land back in the blocked app
                        startActivity(
                            Intent(Intent.ACTION_MAIN).apply {
                                addCategory(Intent.CATEGORY_HOME)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            },
                        )
                    }
                    finish()
                }
            }
        }
    }

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
    }
}

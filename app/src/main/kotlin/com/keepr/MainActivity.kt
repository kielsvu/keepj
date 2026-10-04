package com.keepr

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.keepr.ui.KeeprNavHost
import com.keepr.ui.theme.Background
import com.keepr.ui.theme.KeeprTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var app: KeeprApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        app = application as KeeprApplication

        lifecycleScope.launch {
            val screenshotProtection = app.preferencesRepository.screenshotProtection.first()
            if (screenshotProtection) {
                window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                app.preferencesRepository.screenshotProtection.collect { enabled ->
                    if (enabled) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    }
                }
            }
        }

        setContent {
            KeeprTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Background
                ) {
                    KeeprNavHost(app = app)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        app.autoLockManager.onAppForegrounded()
    }

    override fun onPause() {
        super.onPause()
        app.autoLockManager.onAppBackgrounded()
    }
}

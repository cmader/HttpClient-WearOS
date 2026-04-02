package net.christianmader.apps.wearos.httpclient.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import net.christianmader.apps.wearos.httpclient.config.SecurePersistentConfigService
import net.christianmader.apps.wearos.httpclient.ui.theme.HttpClientTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            HttpClientTheme {
                val context = LocalContext.current
                val configService = SecurePersistentConfigService(context)
                HttpClientApp(configService)
            }
        }
    }
}


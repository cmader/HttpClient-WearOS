package net.christianmader.apps.wearos.httpclient.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import kotlinx.serialization.Serializable
import net.christianmader.apps.wearos.httpclient.config.ConfigService

@Serializable
object Config

@Serializable
data class Endpoints(val forceRetrieveFromNetwork: Boolean = false)

@Serializable
data class Dialog(val messageId: Int? = null,
                  val text: String = "",
                  val indicatesError: Boolean = false,
                  val isInitError: Boolean = false)

@Composable
fun HttpClientApp(configService: ConfigService) {
    val httpClientAppViewModelFactory = viewModelFactory {
        initializer {
            HttpClientAppViewModel(configService)
        }
    }

    val httpClientAppViewModel: HttpClientAppViewModel = viewModel(factory = httpClientAppViewModelFactory)
    val uiState by httpClientAppViewModel.uiState.collectAsState()

    if (uiState.isInitialized) {
        val navController = rememberSwipeDismissableNavController()
        NavHostAndScreens(uiState.isConfigured, navController, configService)
    }
    else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun NavHostAndScreens(isConfigured: Boolean,
                              navController: NavHostController,
                              configService: ConfigService)
{
    val startDestination = if (isConfigured) Endpoints() else Config
    NavHost(navController = navController, startDestination = startDestination) {
        composable<Config> {
            ConfigScreen(configService,
                onDoneClicked = {navController.navigate(Endpoints(true))},
                onCancelClicked = {navController.popBackStack()},
                onError = {userMessage -> navController.navigate(Dialog(userMessage.messageId,
                    userMessage.text, true))})
        }

        composable<Dialog> { backstackEntry ->
            val dialog: Dialog = backstackEntry.toRoute()
            DialogScreen(dialog.messageId, dialog.text, dialog.indicatesError, onBackClick = {
                if (dialog.isInitError) {
                    navController.navigate(Config)
                }
                else {
                    navController.popBackStack()
                }
            })
        }

        composable<Endpoints> { backstackEntry ->
            val endpoints: Endpoints = backstackEntry.toRoute()
            EndpointsScreen(configService,
                endpoints.forceRetrieveFromNetwork,
                onConfigureClick = {navController.navigate(Config)},
                onUserMessage = {userMessage -> navController.navigate(Dialog(userMessage.messageId,
                    userMessage.text, userMessage.indicatesError, userMessage.isInitError))})
        }
    }
}

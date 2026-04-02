package net.christianmader.apps.wearos.httpclient.ui

import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.OutlinedButton
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.FilledTonalButton
import kotlinx.coroutines.flow.receiveAsFlow
import net.christianmader.apps.wearos.httpclient.R
import net.christianmader.apps.wearos.httpclient.config.ConfigService
import net.christianmader.apps.wearos.httpclient.data.DeviceCapabilities
import net.christianmader.apps.wearos.httpclient.data.EndpointLine

@Composable
fun EndpointsScreen(configService: ConfigService,
                    forceRetrieveEndpointsDefinitionFromNetwork: Boolean,
                    onConfigureClick: () -> Unit,
                    onUserMessage: (UserMessage) -> Unit)
{
    val context = LocalContext.current
    val factory = viewModelFactory {
        initializer {
            EndpointsViewModel(configService, context.filesDir, forceRetrieveEndpointsDefinitionFromNetwork)
        }
    }

    val viewModel: EndpointsViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.userMessageEvents.receiveAsFlow().collect {
            onUserMessage(it)
        }
    }

    if (uiState.isRootEndpointLoaded) {
        EndpointsList(
            uiState,
            viewModel,
            onEndpointClick = { endpoint ->
                val vibrator: Vibrator? = DeviceCapabilities(context).getVibrator()
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(
                        100,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
                if (endpoint.url.isEmpty()) {
                    viewModel.openEndpoint(endpoint)
                }
                else {
                    viewModel.performRequest(endpoint, uiState.currentLevelIsRecentlyUsedEndpoints)
                }
            },
            onConfigureClick = onConfigureClick,
            onBackClick = {viewModel.navigateBack()})

        if (!uiState.currentLevelIsRoot) {
            BackHandler {
                viewModel.navigateBack()
            }
        }
    }
    else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun EndpointsList(uiState: EndpointsUiState,
                  viewModel: EndpointsViewModel,
                  onEndpointClick: (endpoint : EndpointLine) -> Unit,
                  onConfigureClick: () -> Unit,
                  onBackClick : () -> Unit)
{
    val listState = rememberScalingLazyListState()
    Scaffold(
        modifier = Modifier.background(MaterialTheme.colors.background),
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) })
    {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(5.dp))
            FilledTonalButton(
                modifier = Modifier.height(28.dp).widthIn(max=135.dp),
                onClick = onBackClick)
            {
                Row(modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically)
                {
                    if (!uiState.currentLevelIsRoot) {
                        Icon(
                            painter = painterResource(id = R.drawable.back_24dp),
                            contentDescription = "back",
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                    }
                    Text(text = uiState.currentLevelLabel, overflow = TextOverflow.Ellipsis, fontSize = 13.sp)
                }
            }
            Spacer(modifier = Modifier.height(5.dp))

            val recentlyUsedLabel = stringResource(R.string.recently_used)
            ScalingLazyColumn(state = listState) {
                if (uiState.currentLevelIsRoot && uiState.showRecentlyUsedEndpoints && uiState.recentlyUsedEndpointPaths.isNotEmpty()) {
                    item {
                        RecentlyUsedEndpointsChip(onClick = {
                            viewModel.openRecentlyUsedEndpoints(
                                recentlyUsedLabel
                            )
                        })
                    }
                }
                var endpointPaths = uiState.currentLevelChildrenPaths
                if (uiState.currentLevelIsRecentlyUsedEndpoints) {
                    endpointPaths = uiState.recentlyUsedEndpointPaths
                }
                for (endpointPath in endpointPaths) {
                    item {
                        val isBusy = uiState.busyEndpointPath == endpointPath
                        val endpoint = viewModel.getEndpoint(endpointPath)
                        val parentLabel = if (uiState.currentLevelIsRecentlyUsedEndpoints) {
                            viewModel.getParentLabel(endpoint)
                        } else ""
                        EndpointButton(endpoint, isBusy, parentLabel, onEndpointClick)
                    }
                }
                item {
                    Buttons(
                        uiState.currentLevelIsRoot,
                        onConfigureClick,
                        onReloadClick = { viewModel.reset() }
                    )
                }
            }
        }
    }
}

@Composable
fun RecentlyUsedEndpointsChip(onClick: () -> Unit) {
    Button(
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(MaterialTheme.colors.primary),
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.history_24dp),
                contentDescription = "configure",
                tint = MaterialTheme.colors.onSurface
            )
        },
        onClick = onClick,
        label = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.recently_used),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    )
}

@Composable
fun EndpointButton(endpoint : EndpointLine,
                   isBusy : Boolean = false,
                   parentLabel: String = "",
                   onEndpointClick: (endpoint : EndpointLine) -> Unit)
{
    var labelColor = MaterialTheme.colors.onSurface
    var backgroundColor = MaterialTheme.colors.primary
    if (isBusy) {
        backgroundColor = MaterialTheme.colors.primaryVariant
    }
    else if (endpoint.color != null) {
        backgroundColor = endpoint.color.background
        labelColor = endpoint.color.foreground
    }

    val label = listOf(parentLabel, endpoint.label).filter { it.isNotEmpty() }.joinToString("/")
    Button(modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(backgroundColor),
        onClick = { onEndpointClick(endpoint) },
        label = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = label,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.StartEllipsis,
                color = labelColor
            )
        }
    )
}

@Composable
fun Buttons(currentLevelIsRoot: Boolean,
            onConfigureClick : () -> Unit,
            onReloadClick : () -> Unit)
{
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    )
    {
        if (currentLevelIsRoot) {
            Spacer(modifier = Modifier.width(10.dp))
            OutlinedButton(onClick = onConfigureClick)
            {
                Icon(
                    painter = painterResource(id = R.drawable.settings_24dp),
                    contentDescription = "configure",
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            OutlinedButton(onClick = onReloadClick) {
                Icon(
                    painter = painterResource(id = R.drawable.refresh_24dp),
                    contentDescription = "reload",
                )
            }
        }
    }
}

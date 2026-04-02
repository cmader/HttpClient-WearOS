package net.christianmader.apps.wearos.httpclient.ui


import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.OutlinedButton
import androidx.wear.compose.material3.SplitSwitchButton
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.Text
import androidx.wear.input.RemoteInputIntentHelper
import androidx.wear.input.wearableExtender
import net.christianmader.apps.wearos.httpclient.R
import net.christianmader.apps.wearos.httpclient.config.ConfigService
import net.christianmader.apps.wearos.httpclient.data.DeviceCapabilities
import java.net.MalformedURLException
import java.net.URL

private const val URL_INPUT_KEY = "url_input"
private const val X_API_KEY_INPUT_KEY = "x_api_key_input"

@Composable
fun ConfigScreen(configService: ConfigService,
                 onDoneClicked: () -> Unit,
                 onCancelClicked: () -> Unit,
                 onError: (UserMessage) -> Unit) {
    val factory = viewModelFactory {
        initializer {
            ConfigViewModel(configService)
        }
    }

    val viewModel: ConfigViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isConfigLoaded) {
        ConfigOptions(uiState, viewModel, onDoneClicked, onCancelClicked, onError)
    }
    else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun ConfigOptions(uiState: ConfigUiState,
                  viewModel: ConfigViewModel,
                  onDoneClicked: () -> Unit,
                  onCancelClicked: () -> Unit,
                  onError: (UserMessage) -> Unit)
{
    val listState = rememberScalingLazyListState()
    val vibrationSupported = DeviceCapabilities(LocalContext.current).getVibrator() != null

    Column(modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally)
    {
        Spacer(modifier = Modifier.height(15.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        )
        {
            ScalingLazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
                item {
                    TextInputButton(
                        stringResource(R.string.endpoints_base_url),
                        uiState.endpointsBaseUrl,
                        URL_INPUT_KEY,
                        onResult = { endpointsBaseUrl ->
                            try {
                                viewModel.setEndpointsBaseUrl(URL(endpointsBaseUrl))
                            }
                            catch (e: MalformedURLException) {
                                onError(UserMessage(messageId = R.string.err_invalid_base_url,
                                    text = e.message ?: "", indicatesError = true))
                            }
                        })
                }
                item {
                    ToggleButton(stringResource(R.string.show_recently_used_endpoints),
                        uiState.showRecentlyUsedEndpoints) { checked ->
                        viewModel.setShowRecentlyUsedEndpoints(checked)
                    }
                }
                if (vibrationSupported) {
                    item {
                        ToggleButton(
                            stringResource(R.string.vibration_toggle),
                            uiState.vibrateOnEndpointSelection
                        ) { checked -> viewModel.setVibrateOnEndpointSelection(checked) }
                    }
                }
                item {
                    TextInputSwitchButton(
                        stringResource(R.string.x_api_key_toggle),
                        uiState.xApiKeyHeader.ifEmpty { stringResource(R.string.no_value_set) },
                        stringResource(R.string.x_api_key_toggle_description),
                        uiState.useXApiKeyHeader,
                        X_API_KEY_INPUT_KEY,
                        onCheckedChange = { checked -> viewModel.setUseXApiKeyHeader(checked) },
                        onResult = { xApiKeyHeader -> viewModel.setXApiKeyHeader(xApiKeyHeader) }
                    )
                }
                item {
                    OkCancelButtons(
                        doneButtonEnabled = isValidUrl(uiState.endpointsBaseUrl), onDoneClicked = {
                            viewModel.saveConfig()
                            onDoneClicked()
                        }, onCancelClicked = onCancelClicked
                    )
                }
            }
        }
    }
}

private fun isValidUrl(url: String): Boolean {
    try {
        URL(url)
        return true
    }
    catch (_: MalformedURLException) {
        return false
    }
}

@Composable
fun TextInputButton(label: String,
                    value: String,
                    inputKey: String,
                    onResult: (String) -> Unit)
{
    val textInputLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val textInput = onLauncherResult(result, inputKey)
            onResult(textInput)
        }
    }

    FilledTonalButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            val intent: Intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
            val remoteInputs: List<RemoteInput> = listOf(
                RemoteInput.Builder(inputKey)
                    .setLabel(label)
                    .wearableExtender {
                        setEmojisAllowed(false)
                        setInputActionType(EditorInfo.IME_ACTION_DONE)
                    }.build()
            )
            RemoteInputIntentHelper.putRemoteInputsExtra(intent, remoteInputs)
            textInputLauncher.launch(intent)
        },
        label = { Text(label) },
        secondaryLabel = { if (value.isNotEmpty()) Text(value) else Text(stringResource(R.string.no_value_set)) })
}

fun onLauncherResult(result: ActivityResult, inputKey: String) : String {
    if (result.data != null) {
        val results: Bundle = RemoteInput.getResultsFromIntent(result.data)
        val chars: CharSequence? = results.getCharSequence(inputKey)
        if (chars != null) {
            return chars.toString()
        }
    }
    return ""
}

@Composable
fun ToggleButton(labelText: String, checked : Boolean, onCheckedChange : (Boolean) -> Unit) {
    SwitchButton(
        label = { Text(labelText,
            maxLines = 3,
            textAlign = TextAlign.Left,
            overflow = TextOverflow.Ellipsis)
        },
        checked = checked,
        onCheckedChange = { value ->
            onCheckedChange(value)
        },
    )
}

@Composable
fun TextInputSwitchButton(label: String,
                          containerText: String,
                          toggleContentDescription: String,
                          checked: Boolean,
                          inputKey: String,
                          onCheckedChange: (Boolean) -> Unit,
                          onResult: (String) -> Unit)
{
    val textInputLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val textInput = onLauncherResult(result, inputKey)
            onResult(textInput)
        }
    }

    SplitSwitchButton(
        label = {
            Text(modifier = Modifier.fillMaxWidth(),
                text = label,
                textAlign = TextAlign.Left,
                overflow = TextOverflow.Ellipsis,
            )
        },
        secondaryLabel = {
            Text(containerText, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        checked = checked,
        onContainerClick = {
            val intent: Intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
            val remoteInputs: List<RemoteInput> = listOf(
                RemoteInput.Builder(inputKey)
                    .setLabel(label)
                    .wearableExtender {
                        setEmojisAllowed(false)
                        setInputActionType(EditorInfo.IME_ACTION_DONE)
                    }.build()
            )
            RemoteInputIntentHelper.putRemoteInputsExtra(intent, remoteInputs)
            textInputLauncher.launch(intent)
        },
        onCheckedChange = { value ->
            onCheckedChange(value)
        },
        toggleContentDescription = toggleContentDescription
    )
}

@Composable
fun OkCancelButtons(doneButtonEnabled: Boolean = true,
                    onDoneClicked: () -> Unit,
                    onCancelClicked: () -> Unit)
{
    Row {
        OutlinedButton(
            onClick = { onCancelClicked() },
            colors = androidx.wear.compose.material3.ButtonDefaults.outlinedButtonColors(),
            border = androidx.wear.compose.material3.ButtonDefaults.outlinedButtonBorder(enabled = true)) {
            Icon(
                painter = painterResource(id = R.drawable.cancel_24dp),
                contentDescription = "done"
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Button(
            colors = androidx.wear.compose.material3.ButtonDefaults.filledTonalButtonColors(),
            onClick = { onDoneClicked() },
            enabled = doneButtonEnabled)
        {
            Icon(
                painter = painterResource(id = R.drawable.ok_button_24dp),
                contentDescription = "done"
            )
        }
    }
}
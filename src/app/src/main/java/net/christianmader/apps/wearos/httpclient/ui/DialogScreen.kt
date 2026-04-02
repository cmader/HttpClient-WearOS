package net.christianmader.apps.wearos.httpclient.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.OutlinedButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.Text
import androidx.wear.tooling.preview.devices.WearDevices
import net.christianmader.apps.wearos.httpclient.R

@Composable
fun DialogScreen(messageId: Int? = null,
                 text: String = "",
                 indicatesError: Boolean = false,
                 onBackClick : () -> Unit)
{
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        Spacer(modifier = Modifier.height(2.dp))
        if (indicatesError) {
            Icon(
                painter = painterResource(id = R.drawable.error_24dp),
                contentDescription = "Error"
            )
        } else {
            Icon(
                painter = painterResource(id = R.drawable.ok_24dp),
                contentDescription = "ok"
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Box(modifier = Modifier.fillMaxWidth(.8f).fillMaxHeight(.75f), contentAlignment = Alignment.TopCenter)  {
            Message(messageId = messageId, text = text)
        }
        Spacer(modifier = Modifier.height(5.dp))
        OutlinedButton(
            modifier = Modifier.height(30.dp),
            onClick = onBackClick)
        {
            androidx.wear.compose.material.Icon(
                painter = painterResource(id = R.drawable.back_24dp),
                contentDescription = "back",
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        BackHandler {
            onBackClick()
        }
    }
}

@Composable
private fun Message(messageId: Int?, text: String) {
    val predefinedMessageText = if (messageId != null) stringResource(messageId)
    else if (text.isEmpty()) stringResource(R.string.unknown_error) else ""
    val messageText = listOf(predefinedMessageText, text).filter { it.isNotEmpty() }.joinToString(": ")
    Text(text = messageText, textAlign = TextAlign.Center)
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DialogScreenPreview() {
    DialogScreen(messageId = R.string.request_error, text = "test message", false) {}
}


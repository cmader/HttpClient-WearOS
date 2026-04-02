package net.christianmader.apps.wearos.httpclient.ui

data class UserMessage(val messageId: Int? = null,
                       val text: String = "",
                       val indicatesError: Boolean = false,
                       val isInitError: Boolean = false)
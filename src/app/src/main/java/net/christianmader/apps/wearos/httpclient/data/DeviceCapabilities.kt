package net.christianmader.apps.wearos.httpclient.data

import android.content.Context
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager

class DeviceCapabilities(val context : Context) {

    fun getVibrator() : Vibrator? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val vibratorMgr = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            return vibratorMgr.defaultVibrator
        }
        return null
    }

}

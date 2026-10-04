package com.keepr.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object ClipboardUtils {

    fun copyToClipboard(context: Context, label: String, value: String, clearAfterSeconds: Int = 30) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, value)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            clip.description.extras?.apply { }
        }

        clipboard.setPrimaryClip(clip)

        if (clearAfterSeconds > 0) {
            CoroutineScope(Dispatchers.Main).launch {
                delay(clearAfterSeconds * 1000L)
                val current = clipboard.primaryClip
                if (current != null && current.itemCount > 0) {
                    val currentText = current.getItemAt(0).text?.toString()
                    if (currentText == value) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            clipboard.clearPrimaryClip()
                        } else {
                            val clearClip = ClipData.newPlainText("", "")
                            clipboard.setPrimaryClip(clearClip)
                        }
                    }
                }
            }
        }
    }
}

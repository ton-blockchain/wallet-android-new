package org.ton.wallet.coreui.ext

import android.widget.EditText
import org.ton.wallet.lib.log.L

fun EditText.setSelectionSafe(position: Int) {
    try {
        setSelection(position)
    } catch (e: Exception) {
        L.e(e)
    }
}

fun EditText.setTextWithSelection(text: CharSequence, selection: Int = text.length) {
    setText(text)
    setSelectionSafe(selection)
}
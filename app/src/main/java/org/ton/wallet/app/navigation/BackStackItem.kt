package org.ton.wallet.app.navigation

import android.os.Bundle
import com.bluelinelabs.conductor.ControllerChangeHandler

class BackStackItem(
    val screen: String,
    val arguments: Bundle?,
    val pushChangeHandler: ControllerChangeHandler? = null,
    val popChangeHandler: ControllerChangeHandler? = null
)
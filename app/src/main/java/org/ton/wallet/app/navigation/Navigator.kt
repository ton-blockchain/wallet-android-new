package org.ton.wallet.app.navigation

import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.ControllerChangeHandler
import org.ton.wallet.screen.ScreenArguments

interface Navigator {

    val topScreenTag: String?

    @AnyThread
    fun push(
        screen: String,
        isReplace: Boolean = false,
        isRoot: Boolean = false,
        isAnimated: Boolean = true,
        pushChangeHandler: ControllerChangeHandler? = null,
        popChangeHandler: ControllerChangeHandler? = null
    ) {
        push(screen, null, isReplace, isRoot, isAnimated, pushChangeHandler, popChangeHandler)
    }

    @AnyThread
    fun push(
        arguments: ScreenArguments,
        isReplace: Boolean = false,
        isRoot: Boolean = false,
        isAnimated: Boolean = true,
        pushChangeHandler: ControllerChangeHandler? = null,
        popChangeHandler: ControllerChangeHandler? = null
    ) {
        push(arguments.screen, arguments, isReplace, isRoot, isAnimated, pushChangeHandler, popChangeHandler)
    }

    @AnyThread
    fun push(
        screen: String,
        arguments: ScreenArguments?,
        isReplace: Boolean = false,
        isRoot: Boolean = false,
        isAnimated: Boolean = true,
        pushChangeHandler: ControllerChangeHandler? = null,
        popChangeHandler: ControllerChangeHandler? = null
    )

    @AnyThread
    fun popTo(screenTag: String, keepTopScreen: Boolean = false)

    @AnyThread
    fun pop(toRoot: Boolean)

    @MainThread
    fun forEachScreen(action: (Controller) -> Unit)

    @MainThread
    fun getBackStack(): List<BackStackItem>

    @MainThread
    fun setBackStack(backStack: List<BackStackItem>, changeHandler: ControllerChangeHandler? = null)
}
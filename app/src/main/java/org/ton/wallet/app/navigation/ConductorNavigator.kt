package org.ton.wallet.app.navigation

import android.app.Activity
import android.os.*
import android.view.ViewGroup
import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import com.bluelinelabs.conductor.*
import org.ton.wallet.app.navigation.changehandler.SlideChangeHandler
import org.ton.wallet.screen.ScreenArguments

internal class ConductorNavigator : Navigator {

    private val controllerFactory = ControllerFactory()
    private val commandBuffer = mutableListOf<NavigatorCommand>()
    private val handler = Handler(Looper.getMainLooper())

    private var router: Router? = null

    override val topScreenTag: String?
        get() = router?.backstack?.lastOrNull()?.tag()

    @MainThread
    fun attach(activity: Activity, container: ViewGroup, savedInstanceState: Bundle?) {
        controllerFactory.setActivity(activity)
        router = Conductor.attachRouter(activity, container, savedInstanceState).apply {
            setOnBackPressedDispatcherEnabled(true)
        }
        commandBuffer.forEach(::executeCommand)
        commandBuffer.clear()
    }

    @MainThread
    fun detach() {
        router = null
    }

    @AnyThread
    override fun push(
        screen: String,
        arguments: ScreenArguments?,
        isReplace: Boolean,
        isRoot: Boolean,
        isAnimated: Boolean,
        pushChangeHandler: ControllerChangeHandler?,
        popChangeHandler: ControllerChangeHandler?
    ) {
        executeCommand(NavigatorCommand.Push(screen, arguments, isReplace, isRoot, isAnimated, pushChangeHandler, popChangeHandler))
    }

    @AnyThread
    override fun popTo(screenTag: String, keepTopScreen: Boolean) {
        executeCommand(NavigatorCommand.PopTo(screenTag, keepTopScreen))
    }

    @AnyThread
    override fun pop(toRoot: Boolean) {
        executeCommand(NavigatorCommand.Pop(toRoot))
    }

    override fun getBackStack(): List<BackStackItem> {
        return router?.backstack?.map { transaction ->
            BackStackItem(
                screen = transaction.tag()!!,
                arguments = transaction.controller.args,
                pushChangeHandler = transaction.pushChangeHandler(),
                popChangeHandler = transaction.popChangeHandler()
            )
        } ?: emptyList()
    }

    override fun setBackStack(backStack: List<BackStackItem>, changeHandler: ControllerChangeHandler?) {
        val transactions = mutableListOf<RouterTransaction>()
        backStack.forEach { backStackItem ->
            val controller = controllerFactory.getController(backStackItem.screen, backStackItem.arguments)
            controller.targetController = transactions.lastOrNull()?.controller
            val transaction = getTransaction(backStackItem.screen, controller)
            val pushChangeHandler = backStackItem.pushChangeHandler?.copy()
            (pushChangeHandler as? SlideChangeHandler)?.withAnimation = false
            transaction.pushChangeHandler(pushChangeHandler)
            transaction.popChangeHandler(backStackItem.popChangeHandler)
            transactions.add(transaction)
        }
        router?.setBackstack(transactions, changeHandler)
    }

    override fun forEachScreen(action: (Controller) -> Unit) {
        router?.backstack?.forEach { transaction ->
            action.invoke(transaction.controller)
        }
    }

    private fun executeCommand(command: NavigatorCommand) {
        val action = Runnable {
            val router = router
            if (router == null) {
                commandBuffer.add(command)
            } else {
                when (command) {
                    is NavigatorCommand.Push -> executePush(command)
                    is NavigatorCommand.PopTo -> executePopTo(command)
                    is NavigatorCommand.Pop -> executePop(command)
                }
            }
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action.run()
        } else {
            handler.post(action)
        }
    }

    @MainThread
    private fun executePush(command: NavigatorCommand.Push) {
        val router = router ?: return
        val bundle = if (command.arguments == null) {
            null
        } else {
            val bundle = Bundle()
            bundle.putParcelable(ScreenArguments.BUNDLE_KEY_ARGUMENTS, command.arguments)
            bundle
        }
        val controller = controllerFactory.getController(command.screen, bundle)

        val topController = router.backstack.lastOrNull()?.controller
        if (controller.targetController == null && topController != null) {
            controller.targetController =
                if (command.isReplace) topController.targetController
                else topController
        }

        val transaction = getTransaction(command.screen, controller)
        if (command.isAnimated) {
            transaction.pushChangeHandler(command.pushChangeHandler ?: controllerFactory.getDefaultPushChangeHandler(controller))
            transaction.popChangeHandler(command.popChangeHandler ?: controllerFactory.getDefaultPopChangeHandler(controller))
        }

        if (router.hasRootController() && !command.isRoot) {
            if (command.isReplace) {
                router.replaceTopController(transaction)
            } else {
                router.pushController(transaction)
            }
        } else {
            router.setRoot(transaction)
        }
    }

    @MainThread
    private fun executePopTo(command: NavigatorCommand.PopTo) {
        val router = router ?: return
        val currentBackStack = router.backstack
        if (command.keepTopScreen && currentBackStack.size >= 2) {
            val newBackStack = ArrayList<RouterTransaction>()
            for (i in 0 until currentBackStack.size - 2) {
                newBackStack.add(currentBackStack[i])
                if (currentBackStack[i].tag() == command.screenTag) {
                    break
                }
            }
            newBackStack.add(currentBackStack.last())
            router.setBackstack(newBackStack, null)
        } else {
            router.popToTag(command.screenTag)
        }
    }

    @MainThread
    private fun executePop(command: NavigatorCommand.Pop) {
        if (command.toRoot) {
            router?.popToRoot()
        } else {
            router?.handleBack()
        }
    }

    private fun getTransaction(screenTag: String, controller: Controller): RouterTransaction {
        val transaction = RouterTransaction.with(controller)
        transaction.tag(screenTag)
        return transaction
    }


    private sealed interface NavigatorCommand {

        class Push(
            val screen: String,
            val arguments: ScreenArguments?,
            val isReplace: Boolean,
            val isRoot: Boolean,
            val isAnimated: Boolean,
            val pushChangeHandler: ControllerChangeHandler?,
            val popChangeHandler: ControllerChangeHandler?
        ) : NavigatorCommand

        class PopTo(
            val screenTag: String,
            val keepTopScreen: Boolean
        ) : NavigatorCommand

        class Pop(val toRoot: Boolean) : NavigatorCommand
    }
}
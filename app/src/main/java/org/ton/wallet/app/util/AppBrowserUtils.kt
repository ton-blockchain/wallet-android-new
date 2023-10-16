package org.ton.wallet.app.util

import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import androidx.browser.customtabs.*
import org.ton.wallet.core.Res
import org.ton.wallet.uikit.RUiKitColor

object AppBrowserUtils {

    private const val CHROME_PACKAGE_NAME = "com.android.chrome"
    private const val PACKAGE_TEST_URL = "http://www.google.com"

    private var customTabsSession: CustomTabsSession? = null

    var isShowing: Boolean = false
        private set

    fun init(context: Context) {
        val packages = getCustomTabsPackages(context)
        val clientPackage = packages.firstOrNull { it.activityInfo.packageName == CHROME_PACKAGE_NAME }
            ?: packages.firstOrNull()
        clientPackage?.activityInfo?.packageName?.let { packageName ->
            CustomTabsClient.bindCustomTabsService(context, packageName, customTabsConnection)
        }
    }

    fun mayLaunchUrl(uri: String) {
        val uriObj = try {
            Uri.parse(uri)
        } catch (e: Exception) {
            null
        } ?: return
        customTabsSession?.mayLaunchUrl(uriObj, null, null)
    }

    fun open(activity: Activity, uri: String) {
        open(activity, Uri.parse(uri))
    }

    fun open(activity: Activity, uri: Uri) {
        val preparedUri = prepareUri(uri) ?: return
        if (hasCustomTabsIntentResolver(activity)) {
            val toolbarColor = Res.color(RUiKitColor.common_black)
            val colors = CustomTabColorSchemeParams.Builder()
                .setToolbarColor(toolbarColor)
                .setNavigationBarColor(toolbarColor)
                .build()
            val colorScheme = CustomTabsIntent.COLOR_SCHEME_DARK
            val customTabsIntent = CustomTabsIntent.Builder(customTabsSession)
                .setShowTitle(true)
                .setUrlBarHidingEnabled(true)
                .setColorScheme(colorScheme)
                .setDefaultColorSchemeParams(colors)
                .setStartAnimations(activity, 0, 0)
                .setExitAnimations(activity, 0, 0)
                .build()
            customTabsIntent.intent.flags = customTabsIntent.intent.flags or Intent.FLAG_ACTIVITY_NO_HISTORY
            try {
                customTabsIntent.launchUrl(activity, preparedUri)
            } catch (e: Exception) {
                openInBrowser(activity, preparedUri)
            }
        } else {
            openInBrowser(activity, preparedUri)
        }
    }

    fun openInBrowser(activity: Activity, uri: String) {
        openInBrowser(activity, Uri.parse(uri))
    }

    fun openInBrowser(activity: Activity, uri: Uri) {
        val preparedUri = prepareUri(uri) ?: return
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, preparedUri)
            activity.startActivity(browserIntent)
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun hasCustomTabsIntentResolver(context: Context): Boolean {
        return getCustomTabsPackages(context).isNotEmpty()
    }

    private fun getCustomTabsPackages(context: Context): ArrayList<ResolveInfo> {
        // Get default VIEW intent handler.
        val activityIntent = Intent()
            .setAction(Intent.ACTION_VIEW)
            .addCategory(Intent.CATEGORY_BROWSABLE)
            .setData(Uri.parse(PACKAGE_TEST_URL))

        // Get all apps that can handle VIEW intents.
        val resolvedActivityList = context.packageManager.queryIntentActivities(activityIntent, PackageManager.MATCH_ALL)
        val packagesSupportingCustomTabs: ArrayList<ResolveInfo> = ArrayList()
        for (info in resolvedActivityList) {
            val serviceIntent = Intent(CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION)
                .setPackage(info.activityInfo.packageName)
            if (context.packageManager.resolveService(serviceIntent, 0) != null) {
                packagesSupportingCustomTabs.add(info)
            }
        }
        return packagesSupportingCustomTabs
    }

    private fun prepareUri(uri: Uri): Uri? {
        return if (uri.scheme?.startsWith("http") == true) {
            uri
        } else {
            try {
                val uriString = "https://" + uri.toString().removePrefix(uri.scheme ?: "")
                Uri.parse(uriString)
            } catch (e: Exception) {
                null
            }
        }
    }

    private val customTabsCallback by lazy {
        object : CustomTabsCallback() {
            override fun onNavigationEvent(navigationEvent: Int, extras: Bundle?) {
                super.onNavigationEvent(navigationEvent, extras)
                if (navigationEvent == TAB_SHOWN || navigationEvent == NAVIGATION_FINISHED) {
                    isShowing = true
                } else if (navigationEvent == TAB_HIDDEN) {
                    isShowing = false
                }
            }
        }
    }

    private val customTabsConnection by lazy {
        object : CustomTabsServiceConnection() {
            override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
                client.warmup(0)
                customTabsSession = client.newSession(customTabsCallback)
            }
            override fun onServiceDisconnected(name: ComponentName?) = Unit
        }
    }
}
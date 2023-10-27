package org.ton.wallet.app

import android.app.Application
import org.ton.wallet.app.util.*
import org.ton.wallet.core.Res
import org.ton.wallet.lib.log.L

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        L.init(this, BuildConfig.DEBUG, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
        Res.init(this, resources.configuration)
        AppFiles.init(this)
        AppKeystoreUtils.init()
        Injector.setApplication(this)
        registerActivityLifecycleCallbacks(AppLifecycleDetector)
    }
}
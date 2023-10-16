package org.ton.wallet.app

import android.app.Application
import org.ton.wallet.app.util.*
import org.ton.wallet.core.Res

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Res.init(this, resources.configuration)
        AppFiles.init(this)
        AppKeystoreUtils.init()
        Injector.setApplication(this)
        registerActivityLifecycleCallbacks(AppLifecycleDetector)
    }
}
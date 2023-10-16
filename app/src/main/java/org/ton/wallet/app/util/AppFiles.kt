package org.ton.wallet.app.util

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import java.io.File

@SuppressLint("StaticFieldLeak")
object AppFiles {

    private lateinit var context: Context

    fun init(application: Application) {
        context = application
    }

    fun getFilesDir(): File {
        val dir: File = context.filesDir ?: File(context.applicationInfo.dataDir, "files")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
}
package org.ton.wallet.lib.log

import android.content.Context
import android.util.Log
import org.ton.wallet.lib.log.target.*
import java.io.*

object L {

    private val stringBuilder = object : ThreadLocal<StringBuilder>() {
        override fun initialValue(): StringBuilder {
            return StringBuilder()
        }
    }
    private val targets = arrayListOf<LogTarget>()

    private val logcatTarget = LogcatTarget()
    private lateinit var fileTarget: FileLogTarget

    fun init(context: Context, withFileLog: Boolean, versionName: String, versionCode: Int) {
        logcatTarget.isEnabled = true
        targets.add(logcatTarget)

        val fileLogDir = getLogsDir(context)
        fileTarget = FileLogTarget(fileLogDir, versionName, versionCode)
        if (withFileLog) {
            fileTarget.isEnabled = true
            targets.add(fileTarget)
        }
    }

    fun getArchive(): File {
        return fileTarget.getArchive()
    }

    fun v(vararg args: Any) {
        v(null, *args)
    }

    fun v(throwable: Throwable?, vararg args: Any) {
        log(Log.VERBOSE, null, throwable, *args)
    }

    fun d(vararg args: Any) {
        d(null, *args)
    }

    fun d(throwable: Throwable?, vararg args: Any) {
        log(Log.DEBUG, null, throwable, *args)
    }

    fun i(vararg args: Any) {
        i(null, *args)
    }

    fun i(throwable: Throwable?, vararg args: Any) {
        log(Log.INFO, null, throwable, *args)
    }

    fun w(vararg args: Any?) {
        w(null, *args)
    }

    fun w(throwable: Throwable?, vararg args: Any?) {
        log(Log.WARN, null, throwable, *args)
    }

    fun e(message: String) {
        e(null, message)
    }

    fun e(throwable: Throwable?) {
        e(throwable, null)
    }

    fun e(tag: String?, throwable: Throwable?) {
        e(tag, throwable, null)
    }

    fun e(throwable: Throwable?, vararg args: Any?) {
        e(null as? String?, throwable, *args)
    }

    fun e(tag: String?, throwable: Throwable?, vararg args: Any?) {
        log(Log.ERROR, tag, throwable, *args)
    }


    // --- private ---
    private fun log(priority: Int, tag: String?, throwable: Throwable?, vararg args: Any?) {
        var isNeedLogs = false
        for (i in 0 until targets.size) {
            isNeedLogs = targets[i].isEnabled
            if (isNeedLogs) {
                break
            }
        }
        if (!isNeedLogs) {
            return
        }

        val sb = stringBuilder.get() ?: StringBuilder()

        // prepare tag
        val resultTag = if (tag == null) {
            val loggerClassName = L::class.java.name
            val thread = Thread.currentThread()
            val element = traceThread(thread, loggerClassName)
            val methodName = element?.methodName ?: "unknown"
            val lineNumber = element?.lineNumber ?: 0
            sb.clear()
                .append(element?.className?.substringAfterLast('.') ?: loggerClassName)
                .append('.')
                .append(methodName)
                .append(':')
                .append(lineNumber)
                .toString()
        } else {
            tag
        }

        // prepare message
        sb.clear()
        for (arg in args) {
            sb.append(arg ?: "null").append(' ')
        }
        if (throwable != null) {
            val stackTraceString = getStackTraceString(throwable)
            if (!stackTraceString.isNullOrEmpty()) {
                sb.append('\n').append(stackTraceString)
            }
        }
        val msg = sb.toString()
        for (i in 0 until targets.size) {
            targets[i].log(priority, resultTag, msg)
        }
    }

    private fun getStackTraceString(throwable: Throwable?): String? {
        if (throwable == null) {
            return null
        }
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        pw.flush()
        return sw.toString()
    }

    private fun traceThread(thread: Thread, className: String): StackTraceElement? {
        val elements = thread.stackTrace
        var isFound = false
        for (i in elements.indices) {
            if (elements[i].className == className) {
                isFound = true
            }
            if (isFound && elements[i].className != className) {
                return elements[i]
            }
        }
        return null
    }

    private fun getLogsDir(context: Context): File {
        val dir = File(context.filesDir, "l")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
}
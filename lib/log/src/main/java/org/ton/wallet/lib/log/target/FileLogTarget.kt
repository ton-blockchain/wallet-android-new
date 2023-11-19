package org.ton.wallet.lib.log.target

import android.os.Build
import android.util.Log
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

internal class FileLogTarget(
    private var logsDirectory: File,
    private val versionName: String,
    private val versionCode: Int
) : LogTarget {

    override var isEnabled: Boolean = false

    private val executor = Executors.newSingleThreadExecutor()
    private val calendarInstance = object : ThreadLocal<Calendar>() {
        override fun initialValue() = Calendar.getInstance()
    }
    private val stringBuilder = object : ThreadLocal<StringBuilder>() {
        override fun initialValue() = StringBuilder()
    }
    private val fileOutputStream: FileOutputStream?

    init {
        val calendar = calendarInstance.get() ?: Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        val dateString = getCurrentDateString(calendar, FILE_DATE_FORMAT_PATTERN)

        val file = File(logsDirectory, "l-${dateString}.log")
        val fileExists = file.exists()
        if (!fileExists) {
            file.createNewFile()
        }

        fileOutputStream = runCatching { FileOutputStream(file, true) }.getOrNull()
        if (fileOutputStream != null) {
            if (fileExists) {
                writeAsync(fileOutputStream, "\n")
            } else {
                writeAsync(fileOutputStream, getHeaderString())
            }
        }

        executor.execute {
            logsDirectory.listFiles { file -> file.extension == "zip" }
                ?.forEach { it.delete() }
            val minModifiedDate = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
            logsDirectory.listFiles { file -> file.extension == "log" }
                ?.filter { it.lastModified() < minModifiedDate }
                ?.forEach { it.delete() }
        }
    }

    override fun log(priority: Int, tag: String, msg: String) {
        if (!isEnabled || fileOutputStream == null) {
            return
        }

        val sb = stringBuilder.get() ?: StringBuilder()
        val calendar = calendarInstance.get() ?: Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        val date = getCurrentDateString(calendar, LOG_DATE_FORMAT_PATTERN)

        val timeSec = calendar.timeInMillis % 1000
        val level = toLevel(priority)
        val dateTimeString = sb.clear()
            .append(date).append(" ")
            .append(String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY))).append(":")
            .append(String.format("%02d", calendar.get(Calendar.MINUTE))).append(":")
            .append(String.format("%02d", calendar.get(Calendar.SECOND))).append(":")
            .append(String.format("%03d", timeSec))
            .append('\t').append(level)
            .append('/').append(tag)
            .toString()

        val lines = msg.split("\n")
            .dropLastWhile { it.isEmpty() }
        for (line in lines) {
            val message = sb.clear()
                .append(dateTimeString)
                .append(' ')
                .append(line)
                .append('\n')
                .toString()
            writeAsync(fileOutputStream, message)
        }
    }

    fun getArchive(): File {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH-mm-ss", Locale.US)
        val fileName = "logcat_${dateFormat.format(Date())}.zip"
        val archiveFile = File(logsDirectory, fileName)
        val buffer = ByteArray(4096)
        FileOutputStream(archiveFile).use { fos -> ZipOutputStream(BufferedOutputStream(fos)).use { zos ->
            logsDirectory.listFiles { file -> file.extension == "log" }
                ?.forEach { file ->
                    val fileInputStream = FileInputStream(file)
                    val zipEntry = ZipEntry(file.name)
                    zos.putNextEntry(zipEntry)

                    var length = fileInputStream.read(buffer)
                    while (length >= 0) {
                        zos.write(buffer, 0, length)
                        length = fileInputStream.read(buffer)
                    }

                    fileInputStream.close()
                }
            zos.close()
        } }
        return archiveFile
    }

    private fun writeAsync(stream: OutputStream, msg: String) {
        executor.execute {
            try {
                stream.write(msg.toByteArray(charset("UTF-8")))
                stream.flush()
            } catch (ignored: Throwable) {}
        }
    }

    private fun getHeaderString(): String {
        return stringBuilder.get()!!.clear()
            .append("sdk: ").append(Build.VERSION.SDK_INT).append('\n')
            .append("manufacturer: ").append(Build.MANUFACTURER).append('\n')
            .append("model: ").append(Build.MODEL).append('\n')
            .append("brand: ").append(Build.BRAND).append('\n')
            .append("device: ").append(Build.DEVICE).append('\n')
            .append("hardware: ").append(Build.HARDWARE).append('\n')
            .append("display: ").append(Build.DISPLAY).append('\n')
            .append("app version: ").append(versionName).append(" (").append(versionCode).append(")")
            .append('\n')
            .toString()
    }

    private fun getCurrentDateString(calendar: Calendar, pattern: String): String {
        return String.format(
            Locale.getDefault(), pattern,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    private fun toLevel(priority: Int): String {
        return when (priority) {
            Log.DEBUG -> "D"
            Log.VERBOSE -> "V"
            Log.INFO -> "I"
            Log.WARN -> "W"
            Log.ERROR -> "E"
            else -> ""
        }
    }

    private companion object {

        private const val LOG_DATE_FORMAT_PATTERN = "%d.%02d.%02d"
        private const val FILE_DATE_FORMAT_PATTERN = "%d-%02d-%02d"
    }
}
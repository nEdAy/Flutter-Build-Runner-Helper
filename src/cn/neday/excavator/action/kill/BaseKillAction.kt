package cn.neday.excavator.action.kill

import cn.neday.excavator.action.BaseAnAction
import cn.neday.excavator.checker.ProjectChecker
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*


abstract class BaseKillAction : BaseAnAction() {
    abstract val killWhat: String
    abstract val processNameOnUnix: String
    abstract val processWhereOnUnixOnWindows: String

    override fun update(event: AnActionEvent) {
        val project = event.getData(PlatformDataKeys.PROJECT)
        val projectPath = project?.basePath
        event.presentation.isEnabledAndVisible = ProjectChecker().check(projectPath)
    }

    override fun actionPerformed(event: AnActionEvent) {
        val pids = try {
            pids
        } catch (e: UnsupportedOperationException) {
            showErrorMessage("Could not get process data!")
            return
        }
        if (pids.isEmpty()) {
            showErrorMessage("No $killWhat process is running!")
        } else {
            var result = true
            for (pid in pids) {
                result = result and killProcess(pid)
            }
            if (result) {
                showInfo("$killWhat was killed! Your IDE may show you some other dialogs, it's safe to ignore them.")
            } else {
                showErrorMessage("Could not kill $killWhat! Check that your system supports killing processes!")
            }
        }
    }

    @get:Throws(UnsupportedOperationException::class)
    private val pids: Array<String>
        get() = if (isWindowsOS()) {
            pidsOnWindows
        } else {
            pidsOnUnix
        }


    /**
     * @param pid The PID of the process to kill
     * @return true if killed, false if not
     */
    private fun killProcess(pid: String): Boolean {
        return if (isWindowsOS()) {
            killProcessOnWindows(pid)
        } else {
            killProcessOnUnix(pid)
        }
    }

    @get:Throws(UnsupportedOperationException::class)
    private val pidsOnWindows: Array<String>
        get() {
            val pids = ArrayList<String>()
            val r = Runtime.getRuntime()
            val p: Process
            try {
                p = r.exec("wmic process where \"$processWhereOnUnixOnWindows\" get processid")
                p.waitFor()
                val b = BufferedReader(InputStreamReader(p.inputStream))
                var line: String? = null
                while (b.readLine()?.also { line = it } != null) {
                    try {
                        line?.trim { it <= ' ' }?.toInt()
                        line?.trim { it <= ' ' }?.let { pids.add(it) }
                    } catch (e: NumberFormatException) {
                        //Don't add it, it's a string!
                    }
                }
                b.close()
            } catch (e: IOException) {
                e.printStackTrace()
                throw UnsupportedOperationException("wmic parsing failed!")
            } catch (e: InterruptedException) {
                e.printStackTrace()
                throw UnsupportedOperationException("wmic parsing failed!")
            }
            return pids.toTypedArray()
        }

    @get:Throws(UnsupportedOperationException::class)
    private val pidsOnUnix: Array<String>
        get() {
            val pids = ArrayList<String>()
            val r = Runtime.getRuntime()
            val p: Process
            try {
                p = r.exec("pgrep -f $processNameOnUnix")
                p.waitFor()
                if (p.exitValue() != 0 && p.exitValue() != 1) { //OK found, OK not found
                    throw UnsupportedOperationException("pgrep returned error value!")
                } else {
                    val b = BufferedReader(InputStreamReader(p.inputStream))
                    var line: String?
                    while (b.readLine().also { line = it } != null) {
                        line?.let { pids.add(it) }
                    }
                    b.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                throw UnsupportedOperationException("pgrep parsing failed!")
            } catch (e: InterruptedException) {
                e.printStackTrace()
                throw UnsupportedOperationException("pgrep parsing failed!")
            }
            return pids.toTypedArray()
        }

    private fun killProcessOnWindows(pid: String): Boolean {
        val r = Runtime.getRuntime()
        val p: Process
        return try {
            p = r.exec("taskkill /F /PID $pid")
            p.waitFor()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        } catch (e: InterruptedException) {
            e.printStackTrace()
            false
        }
    }

    private fun killProcessOnUnix(pid: String): Boolean {
        val r = Runtime.getRuntime()
        val p: Process
        return try {
            p = r.exec("kill -9 $pid")
            p.waitFor()
            p.exitValue() == 0
        } catch (e: IOException) {
            e.printStackTrace()
            false
        } catch (e: InterruptedException) {
            e.printStackTrace()
            false
        }
    }
}
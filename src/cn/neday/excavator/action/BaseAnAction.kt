package cn.neday.excavator.action

import cn.neday.excavator.checker.ProjectChecker
import cn.neday.excavator.setting.Setting.FLUTTER_PATH_KEY
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.progress.PerformInBackgroundOption
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import javax.swing.JScrollBar
import javax.swing.JScrollPane
import javax.swing.JTextArea


abstract class BaseAnAction : AnAction() {

    abstract val cmd: String
    abstract val title: String
    abstract val successMessage: String
    abstract val errorMessage: String

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabledAndVisible = true
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.getData(PlatformDataKeys.PROJECT)
        project?.let {
            val propertiesComponent = PropertiesComponent.getInstance()
            var flutterPath = propertiesComponent.getValue(FLUTTER_PATH_KEY)
            if (flutterPath.isNullOrEmpty()) {
                val isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows")
                val flutterName = if (isWindows) "flutter.bat" else "flutter"
                showInfo("flutter path not know, where is your flutter? Please choose your flutter, maybe it path is '../../flutter/bin/$flutterName'.")
                flutterPath = chooseFlutterPath(project, flutterName)
                if (flutterPath == CANCEL_SIGNAL) {
                    return
                }
                propertiesComponent.setValue(FLUTTER_PATH_KEY, flutterPath)
            }
            val projectPath = project.basePath
            if (!ProjectChecker().check(projectPath)) {
                showErrorMessage("Current directory does not seem to be a valid Flutter project directory.")
            } else {
                projectPath?.let { execCommand(project, flutterPath, it) }
            }
        } ?: showErrorMessage("Current directory does not seem to be a project directory.")
    }

    private fun chooseFlutterPath(project: Project, flutterName: String): String {
        val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor()
                .withFileFilter { virtualFile ->
                    val name = virtualFile.name.toLowerCase()
                    name == flutterName
                }
        val selectedFile = FileChooser.chooseFiles(descriptor, project, null)
        if (selectedFile.isEmpty()) {
            showErrorMessage("You didn't choose any files. Cancel the action.")
            return CANCEL_SIGNAL
        }
        val file = selectedFile[0]
        return if (file.name != flutterName) {
            showErrorMessage("The file you choose is not flutter. Please choose again.")
            chooseFlutterPath(project, flutterName)
        } else {
            file.path
        }
    }

    private fun execCommand(project: Project, flutterPath: String, dirPath: String) {
        var isBuildRunnerSuccess = false
        // 将项目对象，ToolWindow的id传入，获取控件对象
        val toolWindow: ToolWindow = ToolWindowManager.getInstance(project).getToolWindow("Flutter Build Runner Console Tool")
        // 无论当前状态为关闭/打开，进行强制打开ToolWindow
        toolWindow.show {}
        val jScrollPane = toolWindow.contentManager.getContent(0)?.component?.getComponent(0) as JScrollPane
        val verticalBar: JScrollBar = jScrollPane.verticalScrollBar
        val jTextArea = jScrollPane.viewport.getComponent(0) as JTextArea
        project.asyncTask(title = title, runAction = {
            val fillCmd = "$flutterPath $cmd"
            log(jTextArea, verticalBar, "\$ $fillCmd")
            val process = Runtime.getRuntime().exec(fillCmd, null, File(dirPath))
            val bufferedInputStream = BufferedInputStream(process.inputStream)
            val bufferedReader = BufferedReader(InputStreamReader(bufferedInputStream, "GBK"))
            var lineStr: String?
            while (bufferedReader.readLine().also { lineStr = it } != null) {
                log(jTextArea, verticalBar, lineStr)
            }
            val exitVal = process.waitFor()
            bufferedReader.close()
            bufferedInputStream.close()
            isBuildRunnerSuccess = if (exitVal == 0) {
                log(jTextArea, verticalBar, "build_runner Success!")
                true
            } else {
                log(jTextArea, verticalBar, "build_runner Error!")
                false
            }
        }, successAction = {
            if (isBuildRunnerSuccess) {
                showInfo(successMessage)
            } else {
                showErrorMessage("An exception error occurred during build_runner execution. Please manually execute and resolve the error before using this plugin.")
            }
        }, failAction = {
            showErrorMessage(errorMessage + ", message:" + it.localizedMessage)
        })
    }

    private fun showInfo(message: String) {
        Messages.showMessageDialog(message, "Flutter Build Runner Helper", Messages.getInformationIcon())
    }

    private fun showErrorMessage(message: String) {
        Messages.showMessageDialog(message, "Flutter Build Runner Helper", Messages.getErrorIcon())
    }

    private fun log(jTextArea: JTextArea, verticalBar: JScrollBar, message: String?) {
        if (!message.isNullOrEmpty()) {
            println(message)
            jTextArea.append("\n" + message)
            verticalBar.value = verticalBar.maximum
        }
    }

    companion object {

        const val CANCEL_SIGNAL = "CANCEL_SIGNAL"
    }
}

// 创建后台异步任务的Project的扩展函数asyncTask
private fun Project.asyncTask(
        title: String,
        runAction: (ProgressIndicator) -> Unit,
        successAction: (() -> Unit)? = null,
        failAction: ((Throwable) -> Unit)? = null,
        finishAction: (() -> Unit)? = null
) {
    object : Task.Backgroundable(this, title, true, PerformInBackgroundOption.ALWAYS_BACKGROUND) {
        override fun run(p0: ProgressIndicator) {
            return runAction.invoke(p0)
        }

        override fun onSuccess() {
            successAction?.invoke()
        }

        override fun onThrowable(error: Throwable) {
            failAction?.invoke(error)
        }

        override fun onFinished() {
            finishAction?.invoke()
        }
    }.queue()
}
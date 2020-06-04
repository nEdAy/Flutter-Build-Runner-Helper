package cn.neday.excavator.action

import cn.neday.excavator.checker.ProjectChecker
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.progress.PerformInBackgroundOption
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader


abstract class BaseAnAction : AnAction() {

    abstract val cmd: String
    abstract val title: String
    abstract val successMessage: String
    abstract val errorMessage: String

    var isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows")

    companion object {

        const val FLUTTER_PATH_KEY = "flutter_path"
        const val CANCEL_SIGNAL = "Cancel_Signal"
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabledAndVisible = true
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.getData(PlatformDataKeys.PROJECT)
        project?.let {
            val propertiesComponent = PropertiesComponent.getInstance()
            var flutterPath = propertiesComponent.getValue(FLUTTER_PATH_KEY)
            if (flutterPath.isNullOrEmpty()) {
                showInfo("flutter path not know, where is your flutter? Please choose your flutter, maybe it path is 'flutterDir/bin/flutter'.")
                flutterPath = chooseFlutterPath(project)
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

    private fun chooseFlutterPath(project: Project): String {
        val descriptor = FileChooserDescriptor(true, false, false, false, false, false)
        val selectedFile = FileChooser.chooseFiles(descriptor, project, null)
        if (selectedFile.isEmpty()) {
            showErrorMessage("You didn't choose any files. Cancel the action.")
            return CANCEL_SIGNAL
        }
        val file = selectedFile[0]
        return if (file.name != "flutter") {
            showErrorMessage("The file you choose is not flutter. Please choose again.")
            chooseFlutterPath(project)
        } else {
            file.path
        }
    }

    private fun execCommand(project: Project, flutterPath: String, dirPath: String) {
        var isBuildRunnerSuccess = false
        project.asyncTask(title = title, runAction = {
            val commandArray = if (isWindows) {
                arrayOf("cmd.exe", "-c", "$flutterPath $cmd")
            } else {
                arrayOf("bash", "-c", "$flutterPath $cmd")
            }
            val process = Runtime.getRuntime().exec(commandArray, null, File(dirPath))
            val bufferedInputStream = BufferedInputStream(process.inputStream)
            val bufferedReader = BufferedReader(InputStreamReader(bufferedInputStream, "GBK"))
            var lineStr: String?
            while (bufferedReader.readLine().also { lineStr = it } != null) {
                println(lineStr)
            }
            val exitVal = process.waitFor()
            bufferedReader.close()
            bufferedInputStream.close()
            if (exitVal == 0) {
                println("Success!")
                isBuildRunnerSuccess = true
            } else {
                println("Error!")
                isBuildRunnerSuccess = false
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
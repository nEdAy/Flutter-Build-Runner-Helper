package cn.neday.excavator.action

import cn.neday.excavator.checker.ProjectChecker
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
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

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabledAndVisible = true
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.getData(PlatformDataKeys.PROJECT)
        project?.let {
            val path = project.basePath
            if (!ProjectChecker().check(path)) {
                showErrorMessage("Current directory does not seem to be a valid Flutter project directory.")
            } else {
                path?.let { execCommand(project, it) }
            }
        } ?: showErrorMessage("Current directory does not seem to be a project directory.")
    }

    private fun execCommand(project: Project, path: String) {
        project.asyncTask(title = title, runAction = {
            val runtime = Runtime.getRuntime()
            val process = runtime.exec(cmd, null, File(path))
            val bufferedInputStream = BufferedInputStream(process.errorStream)
            val bufferedReader = BufferedReader(InputStreamReader(bufferedInputStream))
            var lineStr: String?
            while (bufferedReader.readLine().also { lineStr = it } != null) {
                println(lineStr)
            }
            process.waitFor()
            bufferedReader.close()
            bufferedInputStream.close()
        }, successAction = {
            showSuccessInfo(successMessage)
        }, failAction = {
            showErrorMessage(errorMessage)
        })
    }

    private fun showSuccessInfo(message: String) {
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
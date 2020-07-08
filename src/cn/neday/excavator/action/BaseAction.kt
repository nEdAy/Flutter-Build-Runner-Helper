package cn.neday.excavator.action

import cn.neday.excavator.checker.ProjectChecker
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.ui.Messages


abstract class BaseAnAction : AnAction() {

    override fun update(event: AnActionEvent) {
        val project = event.getData(PlatformDataKeys.PROJECT)
        val projectPath = project?.basePath
        event.presentation.isEnabledAndVisible = ProjectChecker().check(projectPath)
    }

    protected fun isWindowsOS(): Boolean {
        return System.getProperty("os.name").toLowerCase().startsWith("windows")
    }

    protected fun showInfo(message: String) {
        Messages.showMessageDialog(message, "Flutter Build Runner Helper", Messages.getInformationIcon())
    }

    protected fun showErrorMessage(message: String) {
        Messages.showMessageDialog(message, "Flutter Build Runner Helper", Messages.getErrorIcon())
    }
}
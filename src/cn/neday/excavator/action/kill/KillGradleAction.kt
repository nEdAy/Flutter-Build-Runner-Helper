package cn.neday.excavator.action.kill

class KillGradleAction : BaseKillAction() {
    override val killWhat = "Gradle"
    override val processNameOnUnix = "gradle-launcher"
    override val processWhereOnUnixOnWindows = "commandline like '%gradle-launcher%' and name like '%java%'"
}
package cn.neday.excavator.action.kill

class KillFlutterAction : BaseKillAction() {
    override val killWhat = "Flutter"
    override val processNameOnUnix = "flutter"
    override val processWhereOnUnixOnWindows = "commandline name='flutter'"
}
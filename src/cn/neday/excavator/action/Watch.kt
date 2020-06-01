package cn.neday.excavator.action

class Watch : BaseAnAction() {

    override val cmd = "flutter packages pub run build_runner watch"
    override val title = "Watching"
    override val successMessage = "Complete!\nRunning watch successfully."
    override val errorMessage = "Could not running watch!"
}
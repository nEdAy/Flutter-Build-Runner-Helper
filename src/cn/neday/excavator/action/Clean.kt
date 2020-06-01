package cn.neday.excavator.action

class Clean : BaseAnAction() {

    override val cmd = "flutter packages pub run build_runner clean"
    override val title = "Cleaning"
    override val successMessage = "Complete!\nRunning clean successfully."
    override val errorMessage = "Could not running clean!"
}
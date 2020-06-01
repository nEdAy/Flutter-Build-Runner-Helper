package cn.neday.excavator.action

class Build : BaseAnAction() {

    override val cmd = "flutter packages pub run build_runner build"
    override val title = "Building"
    override val successMessage = "Complete!\nRunning build successfully."
    override val errorMessage = "Could not running build!"
}
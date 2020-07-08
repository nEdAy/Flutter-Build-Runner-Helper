package cn.neday.excavator.action.generation

class BuildAction : BaseGenerationAnAction() {
    override val cmd = "packages pub run build_runner build"
    override val title = "Building"
    override val successMessage = "Complete!\nRunning build successfully."
    override val errorMessage = "Could not running build!"
}
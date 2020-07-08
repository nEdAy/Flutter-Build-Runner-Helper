package cn.neday.excavator.action.generation

class RebuildAction : BaseGenerationAnAction() {
    override val cmd = "packages pub run build_runner build --delete-conflicting-outputs\n"
    override val title = "Rebuilding"
    override val successMessage = "Complete!\nRunning rebuild successfully."
    override val errorMessage = "Could not running rebuild!"
}
package cn.neday.excavator.action.generation

class CleanAction : BaseGenerationAnAction() {
    override val cmd = "packages pub run build_runner clean"
    override val title = "Cleaning"
    override val successMessage = "Complete!\nRunning clean successfully."
    override val errorMessage = "Could not running clean!"
}
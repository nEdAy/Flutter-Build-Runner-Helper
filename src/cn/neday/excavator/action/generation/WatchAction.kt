package cn.neday.excavator.action.generation

class WatchAction : BaseGenerationAnAction() {
    override val cmd = "packages pub run build_runner watch"
    override val title = "Watching"
    override val successMessage = "Complete!\nRunning watch successfully."
    override val errorMessage = "Could not running watch!"
}
package cn.neday.excavator.checker

import java.io.File
import java.util.*

class ProjectChecker : ICheck {

    private var checkFiles: ArrayList<String> = arrayListOf("lib", ".metadata", ".packages", "pubspec.lock", "pubspec.yaml")

    override fun check(path: String?): Boolean {
        if (path == null || path.isEmpty()) {
            return false
        }
        val dir = File(path)
        if (!dir.exists() || !dir.isDirectory) {
            return false
        }
        val files = Objects.requireNonNull(dir.list())
        var count = 0
        for (file in files) {
            if (checkFiles.contains(file)) {
                count++
            }
        }
        return count == checkFiles.size
    }
}
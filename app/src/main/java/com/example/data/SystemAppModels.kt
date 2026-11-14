package com.example.data

enum class AppStatus {
    INSTALLED,
    DISABLED,
    UNINSTALLED
}

enum class SafetyLevel {
    SAFE_BLOAT,
    CAUTION,
    CRITICAL,
    UNKNOWN
}

enum class AppFilter(val labelBn: String) {
    ALL("সব"),
    INSTALLED("ইনস্টলড"),
    UNINSTALLED("রিমুভড"),
    DISABLED("ডিজেবলড"),
    SAFE_BLOAT("Safe Bloat")
}

data class SystemAppItem(
    val name: String,
    val packageName: String,
    val versionName: String,
    val sourceDir: String,
    val status: AppStatus,
    val safetyLevel: SafetyLevel,
    val safetyNoteBn: String
)

data class CommandLogEntry(
    val id: Long,
    val timeLabel: String,
    val appName: String,
    val packageName: String,
    val command: String,
    val exitCode: Int,
    val output: String,
    val isSuccess: Boolean
)

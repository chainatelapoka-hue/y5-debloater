package com.example.shizuku

import android.util.Log
import androidx.annotation.Keep
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.system.exitProcess

@Keep
class ShellUserService : IShellService.Stub() {
    override fun runCommand(command: String): String {
        return try {
            val process = ProcessBuilder("sh", "-c", command)
                .redirectErrorStream(true)
                .start()
            val output = BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                reader.readText().trim()
            }
            val exitCode = process.waitFor()
            "$exitCode\n$output"
        } catch (e: Exception) {
            Log.e("ShellUserService", "Failed to execute command: $command", e)
            "-1\n${e.message ?: "Execution failed"}"
        }
    }

    override fun destroy() {
        exitProcess(0)
    }
}

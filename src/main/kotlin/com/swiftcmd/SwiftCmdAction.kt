package com.swiftcmd

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference

/**
 * Runs the configured command in the background:
 *  - Windows: powershell.exe (semicolon separators and `dir` work)
 *  - macOS/Linux: bash -lc
 * Successful runs are silent; failures show a notification with the captured output.
 */
class SwiftCmdAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        val settings = SwiftCmdSettings.getInstance()
        val command = settings.state.command.trim()

        if (command.isEmpty()) {
            notifyError(project, "Command is empty", "Set a command in Settings | Tools | SwiftCmd.")
            return
        }

        val workDir = resolveWorkDir(project)
        val script = buildScript(command)

        val process = try {
            runProcess(script, workDir, onExit = { exitCode, output ->
                handleExit(project, exitCode, output)
            })
        } catch (ex: Exception) {
            LOG.warn("Failed to start quick command process", ex)
            notifyError(project, "Failed to start command", ex.message ?: ex.toString())
            null
        }

        if (process != null) {
            // Re-triggering the shortcut kills the previous run and starts a new one.
            val previous = RUNNING.getAndSet(process)
            if (previous?.isAlive == true) {
                previous.destroyForcibly()
            }
        }
    }

    private fun resolveWorkDir(project: Project?): File {
        val base = project?.basePath ?: System.getProperty("user.home")
        return File(base)
    }

    /** Builds the shell invocation. The user command is passed as a single argument. */
    private fun buildScript(userCommand: String): List<String> {
        return if (isWindows) {
            val prefix = "chcp 65001 > ${'$'}null; [Console]::OutputEncoding = [System.Text.Encoding]::UTF8; "
            listOf("powershell.exe", "-NoProfile", "-NonInteractive", "-Command", prefix + userCommand)
        } else {
            listOf("bash", "-lc", userCommand)
        }
    }

    private fun runProcess(
        args: List<String>,
        workDir: File,
        onExit: (exitCode: Int, output: String) -> Unit,
    ): Process {
        val builder = ProcessBuilder(args)
        builder.directory(workDir)
        builder.redirectErrorStream(true)

        val process = builder.start()

        EXECUTOR.execute {
            val bytes = process.inputStream.readBytes()
            val output = String(bytes, Charsets.UTF_8)
            val exitCode = process.waitFor()
            ApplicationManager.getApplication().invokeLater { onExit(exitCode, output) }
        }
        return process
    }

    private fun handleExit(project: Project?, exitCode: Int, output: String) {
        if (exitCode == 0) return // silent on success

        val detail = output.trim().takeLast(MAX_OUTPUT_CHARS)
            .ifEmpty { "(no output)" }
        notifyError(
            project,
            "SwiftCmd failed (exit code $exitCode)",
            detail,
        )
    }

    private fun notifyError(project: Project?, title: String, content: String) {
        val group = NotificationGroupManager.getInstance().getNotificationGroup(NOTIFICATION_GROUP)
        val notification = group.createNotification(title, content, NotificationType.ERROR)
        Notifications.Bus.notify(notification, project)
    }

    companion object {
        private val LOG = Logger.getInstance(SwiftCmdAction::class.java)
        private val EXECUTOR = Executors.newCachedThreadPool()
        private val RUNNING = AtomicReference<Process?>()
        private const val NOTIFICATION_GROUP = "SwiftCmd.Notification"
        private const val MAX_OUTPUT_CHARS = 4000

        private val isWindows: Boolean
            get() = System.getProperty("os.name").lowercase().contains("win")
    }
}

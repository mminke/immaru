import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

abstract class BuildInfoTask @Inject constructor(private var execOperations: ExecOperations) :
    DefaultTask() {

    @get:OutputFile
    abstract val buildInfoOutput: RegularFileProperty

    @TaskAction
    fun action() {
        val commitHash = gitCommitHash()
        val currentBranch = gitCurrentBranch()
        val currentRef = gitCommitReference()

        val outputFile = buildInfoOutput.asFile.get()
        outputFile.writeText(
            """
                project.name=${project.name}
                project.version=${project.version}
                build.time=${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())}
                git.commit.hash=${commitHash}
                git.branch=${currentBranch ?: ""}
                git.ref=${currentRef ?: ""}
            """.trimIndent()
        )

        println("Build info written to: ${outputFile.absolutePath}")
    }

    private fun gitCommitHash(): String {
        return executeCommand("git", "rev-parse", "--short", "HEAD")!!
    }

    private fun gitCurrentBranch(): String? {
        return executeCommand("git", "symbolic-ref", "--short", "HEAD")
    }

    private fun gitCommitReference(): String? {
        return executeCommand("git", "name-rev", "--name-only", "HEAD")
    }

    private fun executeCommand(vararg args: String): String? {
        val boas = ByteArrayOutputStream()
        val execResult = execOperations.exec {
            commandLine(*args)
            standardOutput = boas
            isIgnoreExitValue = true
        }
        val output = boas.toString().trim()
        return if (execResult.exitValue == 0 && output.isNotEmpty()) {
            output
        } else {
            null
        }
    }
}

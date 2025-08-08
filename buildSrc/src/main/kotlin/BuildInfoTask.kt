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

        val outputFile = buildInfoOutput.asFile.get()
        outputFile.writeText(
            """
                project.name=${project.name}
                project.version=${project.version}
                build.time=${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())}
                git.commit.hash=${commitHash}
                git.branch=${currentBranch}
            """.trimIndent()
        )

        println("Build info written to: ${outputFile.absolutePath}")
    }

    private fun gitCommitHash(): String {
        val boas = ByteArrayOutputStream()
        execOperations.exec {
            commandLine("git", "rev-parse", "HEAD")
            standardOutput = boas
        }.toString()
        return boas.toString().trim()
    }

    private fun gitCurrentBranch(): String {
        val boas = ByteArrayOutputStream()
        execOperations.exec {
            commandLine("git", "symbolic-ref", "--short", "HEAD")
            standardOutput = boas
        }.toString()
        return boas.toString().trim()
    }
}

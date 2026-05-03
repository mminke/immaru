package com.earthrevealed.ktor.extensions.buildinfo

import kotlinx.serialization.Serializable
import java.util.Properties

@Serializable
data class BuildInfo(
    val project: Project = Project(),
    val build: Build = Build(),
    val git: Git = Git()
) {
    @Serializable
    data class Project(
        val name: String = buildInfo.getProperty("project.name").toString(),
        val version: String = buildInfo.getProperty("project.version").toString(),
        val root: Root = Root(),
    ) {
        @Serializable
        data class Root(
            val name: String = buildInfo.getProperty("project.root.name").toString()
        )
    }

    @Serializable
    data class Build(
        val time: String = buildInfo.getProperty("build.time").toString()
    )

    @Serializable
    data class Git(
        val commit: Commit = Commit()
    ) {
        @Serializable
        data class Commit(
            val hash: String = buildInfo.getProperty("git.commit.hash").toString(),
            val shortHash: String = hash.take(7)
        )
    }

    companion object {
        val buildInfo = Properties().also {
            it.load(BuildInfo::class.java.classLoader?.getResourceAsStream("build-info.properties")
                ?: throw IllegalStateException("Cannot find build-info.properties"))
        }
    }
}

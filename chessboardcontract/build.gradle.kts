import com.google.gson.Gson
import org.gradle.internal.impldep.com.google.api.client.json.Json

plugins {
    id("java-library")
    id("kotlin")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation("com.github.bhlangonijr:chesslib:1.2.5")
    implementation("junit:junit:4.13.2")
}

open class DependencyReportGenerator : DependencyReportTask() {

    private val dependencies: MutableSet<DependencyNode> = mutableSetOf()

    init {
        val newConfig = project.configurations.getByName("compileClasspath")
        println(newConfig)
        val implementationOnlyConfig = HashSet<Configuration>()
        implementationOnlyConfig.add(newConfig)
        configurations = implementationOnlyConfig
        outputFile = File("dependencies.txt")

        newConfig.resolvedConfiguration.firstLevelModuleDependencies.forEach {
            dfs(it)
        }
        val topLevelDependencies = dependencies.intersect(newConfig.resolvedConfiguration.firstLevelModuleDependencies.map {
            DependencyNode(it.name)
        }.toSet())


        println(Gson().toJson(topLevelDependencies))
    }

    private fun dfs(resolvedDependency: ResolvedDependency) {
        dependencies.add(DependencyNode(resolvedDependency.name))
        val dependency = dependencies.find { it == DependencyNode(resolvedDependency.name) }!!
        resolvedDependency.children.forEach { child ->
            dependencies.add(DependencyNode(child.name))
            dependency.children.add(dependencies.find { it == DependencyNode(child.name) }!!)
            dfs(child)
        }
    }
}

data class DependencyNode(val name: String) {
    val children: MutableSet<DependencyNode> = mutableSetOf()
}

tasks.register<DependencyReportGenerator>("generateDependencyReport")


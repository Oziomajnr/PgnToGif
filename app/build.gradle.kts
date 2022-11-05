import java.io.FileWriter
val compose_version = "1.0.5"
plugins {
    id("scabbard.gradle") version "0.5.0"
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id ("project-report")
}

android {
    compileSdkVersion(32)
    buildToolsVersion = "30.0.3"

    defaultConfig {
        applicationId = "com.chunkymonkey.imagetogifconverter"
        minSdkVersion(21)
        targetSdkVersion(32)
        versionCode = 8
        versionName = "1.0.4"
        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
        useIR = true
    }

    buildFeatures {
        dataBinding = true
        compose = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }
    composeOptions {
        kotlinCompilerExtensionVersion = compose_version
        kotlinCompilerVersion = "1.5.31"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation("androidx.compose.ui:ui:$compose_version")
    implementation("androidx.compose.material:material:$compose_version")
    implementation("androidx.compose.ui:ui-tooling-preview:$compose_version")
    implementation("androidx.activity:activity-compose:1.4.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$compose_version")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")

    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.4.0")
    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.2")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

    implementation("com.github.bumptech.glide:glide:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation(project(":chesslibrary1"))
    debugImplementation("androidx.compose.ui:ui-tooling:$compose_version")
    implementation("com.github.alorma:compose-settings-ui:0.7.2")

    implementation(platform("com.google.firebase:firebase-bom:29.0.3"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.code.gson:gson:2.9.1")
    implementation("org.web3j:core:4.8.7")
}

open class DependencyReportGenerator : DependencyReportTask() {

    private val dependencies: MutableSet<DependencyNode> = mutableSetOf()

    init {
//        val newConfig = project.configurations.getByName("default")
//        println(newConfig)
//        val implementationOnlyConfig = HashSet<Configuration>()
//        implementationOnlyConfig.add(newConfig)
//        configurations = implementationOnlyConfig
        outputFile = File("dependencies.txt")

        project.configurations.filter { it.name == "implementationDependenciesMetadata" }.forEach { config ->
            config.resolvedConfiguration.firstLevelModuleDependencies.forEach {
                dfs(it)
            }
            val topLevelDependencies =
                dependencies.intersect(config.resolvedConfiguration.firstLevelModuleDependencies.map {
                    DependencyNode(it.name, it.moduleName)
                }.toSet())
            println("Dependencies for configuration ${config.name}")
            println("↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓↓ ↓ ↓ ↓ ↓")
            val fileName =  "dependencies/${config.name}.json"
            try {
                val file = File(fileName)
                file.parentFile.mkdirs()
                val myWriter = FileWriter(file)
                myWriter.write(com.google.gson.Gson().toJson(topLevelDependencies))
                myWriter.close()
                println("Successfully wrote to the file.")
            } catch (e: java.io.IOException) {
                println("An error occurred.")
                e.printStackTrace()
            }
            println()
        }

    }

    private fun dfs(resolvedDependency: ResolvedDependency) {
        dependencies.add(DependencyNode(resolvedDependency.name, resolvedDependency.moduleName))
        val dependency = dependencies.find { it == DependencyNode(resolvedDependency.name, resolvedDependency.moduleName) }!!
        resolvedDependency.children.forEach { child ->
            dependencies.add(DependencyNode(child.name, child.moduleName))
            dependency.children.add(dependencies.find { it == DependencyNode(child.name, child.moduleName) }!!)
            dfs(child)
        }
    }
}

data class DependencyNode(val id: String, val name: String) {
    val children: MutableSet<DependencyNode> = mutableSetOf()
}

tasks.register<DependencyReportGenerator>("generateDependencyReport2")
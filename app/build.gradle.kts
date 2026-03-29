import java.util.Properties

val composeVersion = "1.4.3"
// Compose Compiler 1.5.8 matches Kotlin 1.9.22 (see compose-kotlin compatibility)
val composeCompilerVersion = "1.5.8"

val keystoreProperties = Properties()
val keystorePropertiesFile = rootProject.file("keystore.properties")
if (keystorePropertiesFile.exists()) {
    keystorePropertiesFile.inputStream().use { keystoreProperties.load(it) }
}

fun envOrKeystoreProp(envName: String, propName: String): String? =
    System.getenv(envName)?.takeIf { it.isNotBlank() }
        ?: keystoreProperties.getProperty(propName)?.takeIf { it.isNotBlank() }

/** CI: set RELEASE_KEYSTORE_* env vars. Local: optional root keystore.properties (see keystore.properties.example). */
val releaseKeystorePath: String? =
    System.getenv("RELEASE_KEYSTORE_FILE")?.takeIf { it.isNotBlank() }
        ?: keystoreProperties.getProperty("storeFile")?.takeIf { it.isNotBlank() }
            ?.let { rootProject.file(it).canonicalPath }
// Either RELEASE_KEYSTORE_PASSWORD or RELEASE_KEY_PASSWORD may supply the keystore password (CI often sets both the same).
val releaseKeyPasswordFromEnv = envOrKeystoreProp("RELEASE_KEY_PASSWORD", "keyPassword")
val releaseStorePassword =
    envOrKeystoreProp("RELEASE_KEYSTORE_PASSWORD", "storePassword") ?: releaseKeyPasswordFromEnv
val releaseKeyAlias = envOrKeystoreProp("RELEASE_KEY_ALIAS", "keyAlias").orEmpty()
val releaseKeyPassword = releaseKeyPasswordFromEnv ?: releaseStorePassword

val releaseSigningConfigured =
    !releaseKeystorePath.isNullOrBlank() && !releaseStorePassword.isNullOrBlank() && releaseKeyAlias.isNotBlank()

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.chunkymonkey.pgntogifconverter"
    compileSdk = 35

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.chunkymonkey.imagetogifconverter"
        minSdk = 21
        targetSdk = 35
        versionCode = 15
        versionName = "1.3.1"
        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        if (project.findProperty("ciInstrumented.smokeOnly") == "true") {
            // Single class smoke test (validates APK + emulator); full suite: run without this flag locally.
            testInstrumentationRunnerArguments["class"] =
                "com.example.pgntogifconverter.ExampleInstrumentedTest"
        }
        if (project.findProperty("ciInstrumented.excludeBenchmark") == "true") {
            testInstrumentationRunnerArguments["notPackage"] =
                "com.example.pgntogifconverter.benchmark"
        }
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        dataBinding = true
        compose = true
    }

    signingConfigs {
        if (releaseSigningConfigured) {
            create("release") {
                storeFile = file(releaseKeystorePath!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            if (releaseSigningConfigured) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    composeOptions {
        kotlinCompilerExtensionVersion = composeCompilerVersion
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    lint {
        lintConfig = file("$rootDir/lint-baseline.xml")
        abortOnError = false
    }
}

dependencies {

    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.material:material:1.5.0-beta01")
    implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$composeVersion")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")

    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("com.github.bumptech.glide:glide:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation(project(":chesscore"))
    debugImplementation("androidx.compose.ui:ui-tooling:$composeVersion")
    implementation("com.github.alorma:compose-settings-ui:0.7.2")
    implementation("com.google.android.play:review:2.0.1")
    implementation("com.google.android.play:app-update-ktx:2.1.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.caverock:androidsvg:1.4")

    implementation(platform("com.google.firebase:firebase-bom:29.0.3"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
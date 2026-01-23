plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.ptitsyn.playandthen"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ptitsyn.playandthen"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("Boolean", "SHOW_DEBUG_BUTTON", "true")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("Boolean", "SHOW_DEBUG_BUTTON", "false")
        }
    }
    
    buildFeatures {
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

// Task to build TypeScript games before Android build
tasks.register<Exec>("buildTypeScriptGames") {
    description = "Builds all TypeScript games (numbers, match-words)"
    group = "build"
    
    val gamesDir = file("src/main/assets/games")
    workingDir = gamesDir
    
    // Build each game
    commandLine("bash", "-c", """
        cd numbers && npm install && npm run build && cd .. &&
        cd match-words && npm install && npm run build && cd ..
    """.trimIndent())
    
    // Only run if source files changed
    inputs.dir("src/main/assets/games/numbers/src")
    inputs.dir("src/main/assets/games/match-words/src")
    inputs.file("src/main/assets/games/numbers/package.json")
    inputs.file("src/main/assets/games/match-words/package.json")
    outputs.dir("src/main/assets/games/numbers/dist")
    outputs.dir("src/main/assets/games/match-words/dist")
    
    doFirst {
        println("🎮 Building TypeScript games...")
    }
    
    doLast {
        println("✅ TypeScript games built successfully!")
    }
}

// Make preBuild depend on TypeScript compilation
tasks.named("preBuild") {
    dependsOn("buildTypeScriptGames")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation("com.google.mlkit:language-id:17.0.6")
    testImplementation(libs.junit)
    testImplementation("org.mockito:mockito-core:5.1.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

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
    
    // Exclude development files from APK assets
    @Suppress("DEPRECATION")
    aaptOptions {
        ignoreAssetsPattern = "!node_modules:!src:!*.ts:!tsconfig.json:!webpack.config.js:!package.json:!package-lock.json:!images-backup"
    }
}

// Task to build TypeScript games before Android build
tasks.register<Exec>("buildTypeScriptGames") {
    description = "Builds all TypeScript games"
    group = "build"
    
    workingDir = file("src/main/assets/games")
    
    // Build all 5 games using the existing build script
    commandLine("bash", "build-all.sh")
    
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

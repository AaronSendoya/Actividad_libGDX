plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.app_gyro"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.app_gyro"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Fuerza SOLO arm64 en el APK (tu dispositivo es 64-bit only)
        ndk {
            abiFilters += listOf("arm64-v8a")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/INDEX.LIST"
        }
        // Mantén el empaquetado por defecto; no toques jniLibs aquí
    }

    sourceSets {
        getByName("main").assets.srcDirs("src/main/assets")
    }

    splits {
        abi {
            isEnable = false
            reset()
        }
    }
}

dependencies {
    val gdxVer = "1.12.1"

    // Backend Android (contiene loader y glue)
    implementation("com.badlogicgames.gdx:gdx-backend-android:$gdxVer")
    // API core
    implementation("com.badlogicgames.gdx:gdx:$gdxVer")

    // 🔥 AÑADIMOS EXPLÍCITAMENTE la nativa arm64 (coloca libgdx.so en lib/arm64-v8a/)
    implementation("com.badlogicgames.gdx:gdx-platform:$gdxVer:natives-arm64-v8a")

    // Tus dependencias existentes
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

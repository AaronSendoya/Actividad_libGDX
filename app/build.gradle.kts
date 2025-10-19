// ==== IMPORTS (deben estar ARRIBA del archivo en Kotlin DSL) ====
import org.gradle.kotlin.dsl.register

plugins {
    alias(libs.plugins.android.application)
}

// Una sola definición de la versión de libGDX para todo el archivo
val gdxVer = "1.12.1"

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

        // Tu dispositivo es 64-bit; con arm64-v8a alcanza (puedes agregar otras si quieres)
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

    // Mantén el empaquetado "normal"
    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/INDEX.LIST"
        }
    }

    // Asegura que exista esta carpeta para libGDX (aunque esté vacía)
    sourceSets {
        getByName("main").assets.srcDirs("src/main/assets")
    }

    // Desactiva splits por ABI (por si acaso)
    splits {
        abi {
            isEnable = false
            reset()
        }
    }
}

dependencies {
    // === libGDX necesario en Android ===
    implementation("com.badlogicgames.gdx:gdx-backend-android:$gdxVer")
    implementation("com.badlogicgames.gdx:gdx:$gdxVer")

    // (Opcional pero útil) Agregamos la dependencia de nativos arm64
    // para que Gradle baje el artefacto desde el repositorio.
    implementation("com.badlogicgames.gdx:gdx-platform:$gdxVer:natives-arm64-v8a")

    // === Tus dependencias existentes ===
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

/**
 * === TAREA: copiar libgdx.so a jniLibs/arm64-v8a antes de compilar ===
 * Esto garantiza que el APK contenga lib/arm64-v8a/libgdx.so
 * y elimina el crash "Couldn't load shared library 'gdx'".
 */
tasks.register<Copy>("unpackGdxNativesArm64") {
    // Descargamos el artefacto de nativos arm64 de libGDX
    val cfg = configurations.detachedConfiguration(
        dependencies.create("com.badlogicgames.gdx:gdx-platform:$gdxVer:natives-arm64-v8a")
    )

    // Abrimos el JAR como zip y tomamos libgdx.so
    from(zipTree(cfg.singleFile)) {
        include("**/libgdx.so")
    }

    // Lo dejamos en src/main/jniLibs/arm64-v8a/
    into("$projectDir/src/main/jniLibs/arm64-v8a")
}

// Hacemos que la copia ocurra siempre antes de construir el módulo
afterEvaluate {
    tasks.named("preBuild").configure {
        dependsOn("unpackGdxNativesArm64")
    }
}

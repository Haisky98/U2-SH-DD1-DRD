plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp") version "1.9.21-1.0.15" // <-- Necesario para Room
}

android {
    namespace = "com.example.kuiper"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.kuiper"
        minSdk = 34
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        vectorDrawables {
            useSupportLibrary = true
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
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Jetpack Compose para Wear OS (Versión unificada y estable)
    implementation("androidx.wear.compose:compose-material:1.3.1")
    implementation("androidx.wear.compose:compose-foundation:1.3.1")
    implementation("androidx.wear.compose:compose-navigation:1.3.1")

    // Integración de Compose con la Activity
    implementation("androidx.activity:activity-compose:1.9.0")

    // Librería oficial de Splash Screen
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.compose.material:material-icons-core:1.6.0")

    // WorkManager (Notificaciones cada 2 horas)
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Room (Base de datos local para la gráfica semanal)
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion") 

    // DataStore (Guardar la meta diaria)
    implementation("androidx.datastore:datastore-preferences:1.1.1")
}
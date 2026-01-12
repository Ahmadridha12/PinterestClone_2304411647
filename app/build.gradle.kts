plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.hands10_firebase"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.hands10_firebase"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}
dependencies {
    // AndroidX & UI
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Firebase (Wajib UAS - CPMK 4)
    implementation(platform("com.google.firebase:firebase-bom:33.7.0")) // Gunakan BOM terbaru
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")

    // Google Sign-In (Metode Login ke-2)
    implementation("com.google.android.gms:play-services-auth:21.3.0")

    // Glide (Menampilkan Gambar Pinterest)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0") // Tambahkan ini agar Glide lancar

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("com.google.firebase:firebase-storage-ktx")
    // Tambahkan ini agar error Messaging hilang
    implementation("com.google.firebase:firebase-messaging-ktx")
    // Pastikan Firebase BoM juga ada (opsional tapi disarankan)
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
}
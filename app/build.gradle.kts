plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.zbadev.emotizone"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.zbadev.emotizone"
        minSdk = 26
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        mlModelBinding = true
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.1.0")
    implementation("org.tensorflow:tensorflow-lite-metadata:0.1.0")
    // Google Firestore Bd
    implementation("com.google.firebase:firebase-firestore:25.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))
    //Mostrar imagenes
    implementation("com.github.bumptech.glide:glide:4.13.2")
    //Uso biometrico
    implementation("androidx.biometric:biometric:1.1.0")
    //lottie
    implementation("com.airbnb.android:lottie:6.0.1")
    //Chat Bot
    // Añade la dependencia del SDK del cliente de IA de Google para Android
    implementation("com.google.ai.client.generativeai:generativeai:0.7.0")
    // Necesario para operaciones one-shot (para usar `ListenableFuture` de Guava Android)
    implementation("com.google.guava:guava:31.0.1-android")
    // Necesario para operaciones de streaming (para utilizar `Publisher` de Reactive Streams)
    implementation("org.reactivestreams:reactive-streams:1.0.4")
    //Graficas MPAndroidChart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
}
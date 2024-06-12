plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.rodrigo.deeplarva"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.rodrigo.deeplarva"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] =
                    "$projectDir/schemas"
            }
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
        viewBinding = true
        mlModelBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.3")

    implementation("com.google.android.material:material:1.11.0")

    // Tensorflow Lite dependencies
    implementation("org.tensorflow:tensorflow-lite:2.10.0")

    implementation("org.tensorflow:tensorflow-lite-support:0.4.0-rc0")
    implementation("org.tensorflow:tensorflow-lite-metadata:0.4.0-rc0")
    //Librería de GPU compatibles
    //Tensorflow Lite GPU
    //implementation("org.tensorflow:tensorflow-lite-gpu:2.10.0")
    //implementation("org.tensorflow:tensorflow-lite-gpu-api:2.10.0")
    //0.4.0-rc0
    //org.tensorflow:tensorflow-lite-gpu-api:2.10.0
    //implementation("org.tensorflow:tensorflow-lite-gpu-delegate-plugin:0.4.4")

    //implementation("com.google.android.gms:play-services-tflite-impl:16.1.0")
    //implementation("com.google.android.gms:play-services-tflite-gpu:16.2.0")

    val room_version = "2.4.0"
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    kapt("androidx.room:room-compiler:$room_version")

    val camerax_version = "1.3.3"
    implementation("androidx.camera:camera-core:${camerax_version}")
    implementation("androidx.camera:camera-camera2:${camerax_version}")
    implementation("androidx.camera:camera-lifecycle:${camerax_version}")
    implementation("androidx.camera:camera-view:${camerax_version}")

    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
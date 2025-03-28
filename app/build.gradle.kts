plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.campusbuddy"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.campusbuddy"
        minSdk = 24
        targetSdk = 35
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
    buildFeatures {
        viewBinding = true
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.database)
    implementation(libs.play.services.cast.tv)
    implementation(libs.play.services.analytics.impl)
    implementation(libs.firebase.messaging)
    implementation(libs.androidx.swiperefreshlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(platform("com.google.firebase:firebase-bom:32.2.0"))

    // Circular image dependency
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // Picasso
    implementation("com.squareup.picasso:picasso:2.8")

    // Material Design
    implementation("com.google.android.material:material:1.11.0")

    // Timeago dependency
    implementation("com.github.marlonlom:timeago:4.1.0")

    // Google Play Services Auth
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // Cloudinary for image uploads
    implementation("com.cloudinary:cloudinary-android:3.0.2")

    // Firebase Messaging
    implementation("com.google.firebase:firebase-messaging:23.1.0")

    // ViewPager2 & Circle Indicator
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("me.relex:circleindicator:2.1.6")

    // Google Play Services for Location & Maps
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    // Gson (for JSON parsing)
    implementation("com.google.code.gson:gson:2.10.1")

    // ✅ NEW DEPENDENCIES FOR RETROFIT, RECYCLERVIEW, AND VIEWMODEL ✅

    // Retrofit for API calls
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // RecyclerView (already included via androidx)
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // ViewModel (already included, but ensuring latest version)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")

    // LiveData
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")

}

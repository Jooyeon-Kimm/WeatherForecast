import com.github.javaparser.printer.lexicalpreservation.DifferenceElement.kept
import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.exclude
import java.util.Properties
val properties = Properties()
properties.load(project.rootProject.file("local.properties").inputStream())

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.bottomnavigation"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.bottomnavigation"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "API_KEY_CURRENT_WEATHER", properties.getProperty("API_KEY_CURRENT_WEATHER"))
        buildConfigField("String", "API_KEY_KAKAO_MAP", properties.getProperty("API_KEY_KAKAO_MAP"))
        manifestPlaceholders["API_KEY_CURRENT_WEATHER"] = properties.getProperty("API_KEY_CURRENT_WEATHER")
        manifestPlaceholders["API_KEY_KAKAO_MAP"] = properties.getProperty("API_KEY_KAKAO_MAP")
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
    // 원래 자바11이었는데 카카오맵API사용하려면,,,
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    // 원래 11
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }

    packagingOptions {
        resources {
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
        }
    }

}



dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.media3.common.ktx)
    implementation(libs.androidx.core)
    implementation(libs.androidx.databinding.runtime)
    implementation(libs.identity.android.legacy)
//    implementation(libs.androidx.library)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // 아래 컴파일 단계에서 warning(노란줄) 모두 권장대로 바꾸면
    // [Error] Duplicate Class Found 발생 (외부 라이브러리 implementation 할 때 발생하는 오류)
    // SplashScreen API
    implementation("androidx.core:core-splashscreen:1.0.1")

    // ViewModel Lib
    val lifecycleVersion = "2.7.0"
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-common:$lifecycleVersion")

    // Retrofit and GSON Lib
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // Google PlayService Location Lib , 구글위치제공
    implementation("com.google.android.gms:play-services-location:21.2.0")

    // Jetpack Navigation Lib
    val nav_version = "2.7.7"
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")

    // Responsive Unit Lib for resizing elements
    implementation("com.intuit.ssp:ssp-android:1.1.1")
    implementation("com.intuit.sdp:sdp-android:1.1.1")

    // Custom Switch Lib
    implementation("io.github.bitvale:switcher:1.1.2")

    // TabLayout (1시간 간격, 3시간 간격)
    // 머터리얼 라이브러리 ( 툴바와 코디네이터 레이아웃 -> 스크롤 )
    implementation("com.google.android.material:material:1.12.0")

    // 리사이클러 뷰
    // implementation ("com.android.support:recyclerview-v7:28.0.0")
    implementation ("androidx.recyclerview:recyclerview:1.3.2")       //androidx 버전

    // StickyScrollView for StickyHeader
    // https://github.com/amarjain07/StickyScrollView
    implementation("com.github.amarjain07:StickyScrollView:1.0.3")

    // ViewPager2 (상하스크롤)
    implementation("androidx.viewpager2:viewpager2:1.0.0")

    // 번역 (영어 > 한글, 날씨 설명이 영어), Google Translate API 클라이언트 라이브러리
    //implementation("com.google.cloud:google-cloud-translate:1.94.0")
    implementation("androidx.databinding:databinding-runtime:8.7.3") {
        exclude(group = "androidx.databinding", module = "library-3.2.0-alpha11")
    }

    // 오프라인 모드 (room)
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")








}

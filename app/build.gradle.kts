import com.tencent.tinker.build.util.TypedValue.TINKER_ID
import org.codehaus.groovy.runtime.ProcessGroovyMethods

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.tencent.tinker.patch")
}
android.buildFeatures.buildConfig =true
android {
    namespace = "com.example.testtinker"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.testtinker"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("String", "TINKER_ID", "\"${getTinkerIdValue()}\"")
        buildConfigField("String", "PLATFORM", "\"all\"")
    }
    signingConfigs {
        create("release") {
            try {
                storeFile = file("./key")
                storePassword ="123456"
                keyAlias ="key0"
                keyPassword ="123456"
            } catch (ex: Exception) {
                throw InvalidUserDataException(ex.toString())
            }
        }
        getByName("debug") {
            storeFile =file("./key")
            storePassword = "123456"
            keyAlias = "key0"
            keyPassword = "123456"
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
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    //optional, help to generate the final application
    //tinker's main Android lib
    implementation("com.tencent.tinker:tinker-android-lib:1.9.14.25.3")
    annotationProcessor("com.tencent.tinker:tinker-android-anno:1.9.14.25.3")
}

tinkerPatch {
    oldApk = "${bakPath()}/app-debug-0424-15-02-56.apk"
    ignoreWarning = false
    useSign = true
    tinkerEnable = true
    buildConfig {
        tinkerId = getTinkerIdValue()
        keepDexApply = false
        isProtectedApp = false
        supportHotplugComponent = false
    }


    dex {
        /**
         * optional，default 'jar'
         * only can be 'raw' or 'jar'. for raw, we would keep its original format
         * for jar, we would repack dexes with zip format.
         * if you want to support below 14, you must use jar
         * or you want to save rom or check quicker, you can use raw mode also
         */
        dexMode = "jar"

        /**
         * necessary，default '[]'
         * what dexes in apk are expected to deal with tinkerPatch
         * it support * or ? pattern.
         */
        pattern = listOf(
            "classes*.dex",
            "assets/secondary-dex-?.jar"
        )
        /**
         * necessary，default '[]'
         * Warning, it is very very important, loader classes can't change with patch.
         * thus, they will be removed from patch dexes.
         * you must put the following class into main dex.
         * Simply, you should add your own application {@code tinker.sample.android.SampleApplication}
         * own tinkerLoader, and the classes you use in them
         *
         */
        loader = listOf(
            //use sample, let BaseBuildInfo unchangeable with tinker
            "tinker.sample.android.app.BaseBuildInfo"
        )
    }

    lib {
        /**
         * optional，default '[]'
         * what library in apk are expected to deal with tinkerPatch
         * it support * or ? pattern.
         * for library in assets, we would just recover them in the patch directory
         * you can get them in TinkerLoadResult with Tinker
         */
        pattern = listOf("lib/*/*.so")
    }

    res {
        /**
         * optional，default '[]'
         * what resource in apk are expected to deal with tinkerPatch
         * it support * or ? pattern.
         * you must include all your resources in apk here,
         * otherwise, they won't repack in the new apk resources.
         */
        pattern = listOf("res/*", "assets/*", "resources.arsc", "AndroidManifest.xml")

        /**
         * optional，default '[]'
         * the resource file exclude patterns, ignore add, delete or modify resource change
         * it support * or ? pattern.
         * Warning, we can only use for files no relative with resources.arsc
         */
//        ignoreChange = ["assets/sample_meta.txt"]

        /**
         * default 100kb
         * for modify resource, if it is larger than 'largeModSize'
         * we would like to use bsdiff algorithm to reduce patch file size
         */
        largeModSize = 100
    }

}


fun getTinkerIdValue(): String {
    return if (hasProperty("TINKER_ID")) TINKER_ID else gitSha()
}

fun gitSha(): String {
    println("getting sha")
    try {
        val sha = "git rev-parse --short HEAD".execute().text?.trim() ?: ""
        println("getting sha is $sha")
        return sha
    } catch (e: Exception) {
        throw GradleException("can't get git rev, you should add git to system path or just input test value, such as 'testTinkerId'")
    }
}

fun buildWithTinker(): Boolean {
    return if (hasProperty("TINKER_ENABLE")) (property("TINKER_ENABLE") as Boolean) else true
}

fun bakPath() = file("${buildDir}/bakApk/")

fun String.execute() = ProcessGroovyMethods.execute(this)
val Process.text: String? get() = ProcessGroovyMethods.getText(this)


/**
 * you can use assembleRelease to build you base apk
 * use tinkerPatchRelease -POLD_APK=  -PAPPLY_MAPPING=  -PAPPLY_RESOURCE= to build patch
 * add apk from the build/bakApk
 */
//ext {
//    //for some reason, you may want to ignore tinkerBuild, such as instant run debug build?
//    tinkerEnabled = true
//
//    //for normal build
//    //old apk file to build patch apk
//    tinkerOldApkPath = "${bakPath}/app-debug-0424-15-02-56.apk"
//    //proguard mapping file to build patch apk
////    tinkerApplyMappingPath = "${bakPath}/app-debug-1018-17-32-47-mapping.txt"
//    //resource R.txt to build patch apk, must input if there is resource changed
////    tinkerApplyResourcePath = "${bakPath}/app-debug-0424-15-02-56-R.txt"
//
//    //only use for build all flavor, if not, just ignore this field
////    tinkerBuildFlavorDirectory = "${bakPath}/app-1018-17-32-47"
//}

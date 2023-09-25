// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.0-beta03" apply false
    id("org.jetbrains.kotlin.android") version "1.8.10" apply false
}
buildscript {
    dependencies {
        classpath ("com.tencent.tinker:tinker-patch-gradle-plugin:1.9.14.25.3")
    }
}
import org.gradle.accessors.dm.LibrariesForLibsInPluginsBlock

// build.gradle.kts (Nivel Proyecto)

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}
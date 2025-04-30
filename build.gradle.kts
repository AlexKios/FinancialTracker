plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false version "2.1.0"
    id("com.google.gms.google-services") version "4.4.2" apply false
}
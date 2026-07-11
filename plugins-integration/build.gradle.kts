plugins {
    `kotlin-dsl`
    alias(libs.plugins.ultimanexus.jvm.gradle.plugin)
}
dependencies {
    implementation(libs.ultima.nexus.jvm.core)
    implementation(libs.ultima.nexus.jvm.base)
    implementation(libs.ultima.nexus.jvm.frontend)
    implementation(libs.ultima.nexus.jvm.persistence)
}
testing.suites.named<JvmTestSuite>("test") {
    useJUnitJupiter()
    dependencies {
        implementation(libs.junit.jupiter.params)
        implementation(libs.assertj.core)
        implementation(libs.ultima.nexus.jvm.core)
    }
}

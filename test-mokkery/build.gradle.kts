plugins {
    id("mokkery-multiplatform")
    alias(libs.plugins.kotlin.allopen)
}

allOpen {
    annotation("dev.mokkery.test.OpenForMokkery")
}

configureCompilerPlugin(
    "dev.mokkery",
    "ignoreFinalMembers" to "true",
    "stubs.allowConcreteClassInstantiation" to "true",
    "stubs.allowClassInheritance" to "true",
    "annotations.copyToMock" to """all - named("dev.mokkery.test.AnnotationB"|"dev.mokkery.test.AnnotationC")"""
)

configureCompilerPlugin(
    "dev.mokkery.mockable",
    "annotations" to "dev.mokkery.test.CustomMockable",
    "annotations" to "dev.mokkery.mockable.annotations.Mockable",
)

dependencies {
    kotlinCompilerPluginClasspath(project(":mokkery-plugin"))
    kotlinCompilerPluginClasspath(project(":mokkery-mockable-plugin"))
    kotlinNativeCompilerPluginClasspath(project(":mokkery-plugin"))
    kotlinNativeCompilerPluginClasspath(project(":mokkery-mockable-plugin"))
    commonMainImplementation(project(":mokkery-runtime"))
    commonMainImplementation(project(":mokkery-coroutines"))
    commonMainImplementation(project(":mokkery-mockable-annotations"))
    commonTestImplementation(kotlin("test"))
    commonTestImplementation(libs.kotlinx.coroutines.test)
}

@file:Suppress("UnstableApiUsage")

plugins {
    id("mokkery-jvm")
}

kotlin {
    optInMokkeryDelicateAndInternals()
}

val mokkeryRuntimeClasspath = configurations.register("mokkeryRuntimeClasspath")

val testBase = sourceSets.register("testBase")

dependencies {
    val testBaseCompileOnly = configurations.named("testBaseCompileOnly")
    testBaseCompileOnly(project(":mokkery-plugin"))
    testBaseCompileOnly(project(":mokkery-core-tooling"))
    testBaseCompileOnly(libs.kotlin.compiler)
    testBaseCompileOnly(libs.kotlin.compiler.test.framework)
    mokkeryRuntimeClasspath(project(":mokkery-runtime"))
}


testVariant(
    kotlinVersion = libs.versions.kotlin.get(),
    alias = "Default"
)
testVariant(
    kotlinVersion = libs.versions.kotlinMininumSupported.get(),
    alias = "Minimum",
    enabled = libs.versions.kotlin.get() != libs.versions.kotlinMininumSupported.get(),
)

tasks.register("generateTests") {
    group = "verification"
    dependsOn(tasks.named("generateTestsDefault"))
    dependsOn(tasks.named("generateTestsMinimum"))
}
tasks.test {
    dependsOn(tasks.named("testDefault"))
    dependsOn(tasks.named("testMinimum"))
}

fun Project.testVariant(kotlinVersion: String, alias: String = "", enabled: Boolean = true) {
    val sourceSet = sourceSets.register("test$alias") {
        java.srcDir("src/test$alias/java")
        java.srcDir("src/test$alias/kotlin")
        compileClasspath += testBase.get().output
        runtimeClasspath += testBase.get().output
    }
    dependencies {
        val testImplementation = configurations.named(sourceSet.get().implementationConfigurationName)
        val testRuntimeOnly = configurations.named(sourceSet.get().runtimeOnlyConfigurationName)
        testImplementation(project(":mokkery-plugin"))
        testImplementation(project(":mokkery-core-tooling"))
        testImplementation("org.jetbrains.kotlin:kotlin-compiler:$kotlinVersion")
        testImplementation("org.jetbrains.kotlin:kotlin-compiler-internal-test-framework:$kotlinVersion")
        testImplementation(libs.kotlin.test.junit5)
        testRuntimeOnly(libs.kotlin.reflect)
        testRuntimeOnly(libs.kotlin.script.runtime)
        testRuntimeOnly(libs.kotlin.annotations.jvm)
    }
    configurations.named(sourceSet.get().runtimeClasspathConfigurationName) {
        resolutionStrategy.dependencySubstitution {
            substitute(module("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm"))
                .using(module(libs.intellij.kotlinx.coroutines.core.jvm.get().toString()))
        }
    }

    tasks.register<JavaExec>("generateTests$alias") {
        group = "verification"
        val testData = layout.projectDirectory.dir("src/${testBase.get().name}/data")
        val testsRoot = layout.projectDirectory.dir("src/test$alias/java")
        systemProperty("testDataRoot", testData.asFile.relativeTo(project.isolated.rootProject.projectDirectory.asFile))
        systemProperty("testsRoot", testsRoot.asFile.relativeTo(project.isolated.rootProject.projectDirectory.asFile))
        systemProperty("mokkery.runtimeClasspath", mokkeryRuntimeClasspath.get().asPath)
        inputs
            .dir(testData)
            .withPropertyName("testData")
            .withPathSensitivity(PathSensitivity.RELATIVE)
        outputs
            .dir(testsRoot)
            .withPropertyName("generatedTestsRoot")
        classpath += sourceSet.get().runtimeClasspath
        mainClass.set("dev.mokkery.tests.GenerateTestsKt")
        workingDir = project.isolated.rootProject.projectDirectory.asFile
    }

    tasks.register("test$alias", Test::class) {
        group = "verification"
        testClassesDirs += sourceSet.get().output.classesDirs
        classpath += sourceSet.get().runtimeClasspath
        this.enabled = enabled
        dependsOn(mokkeryRuntimeClasspath)
        inputs
            .dir(layout.projectDirectory.dir("src/${testBase.get().name}/data"))
            .withPropertyName("testData")
            .withPathSensitivity(PathSensitivity.RELATIVE)
        workingDir = project.isolated.rootProject.projectDirectory.asFile
        useJUnitPlatform()
        systemProperty("mokkery.runtimeClasspath", mokkeryRuntimeClasspath.get().asPath)
        systemProperty("idea.ignore.disabled.plugins", "true")
        systemProperty("idea.home.path", project.isolated.rootProject.projectDirectory.asFile)
    }
}

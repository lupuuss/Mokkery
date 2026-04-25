plugins {
    kotlin("jvm")
}


kotlin {
    optInMokkeryDelicateAndInternals()
}

val mokkeryRuntimeClasspath by configurations.registering

val testBase by sourceSets.registering

dependencies {
    val testBaseCompileOnly by configurations
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

val generateTests by tasks.registering {
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
        val testImplementation by configurations.named(sourceSet.get().implementationConfigurationName)
        val testRuntimeOnly by configurations.named(sourceSet.get().runtimeOnlyConfigurationName)
        testImplementation(project(":mokkery-plugin"))
        testImplementation(project(":mokkery-core-tooling"))
        testImplementation("org.jetbrains.kotlin:kotlin-compiler:$kotlinVersion")
        testImplementation("org.jetbrains.kotlin:kotlin-compiler-internal-test-framework:$kotlinVersion")
        testImplementation(libs.kotlin.test.junit5)
        testRuntimeOnly(libs.kotlin.reflect)
        testRuntimeOnly(libs.kotlin.script.runtime)
        testRuntimeOnly(libs.kotlin.annotations.jvm)
    }

    tasks.register<JavaExec>("generateTests$alias") {
        group = "verification"
        val testData = layout.projectDirectory.dir("src/${testBase.get().name}/data")
        val testsRoot = layout.projectDirectory.dir("src/test$alias/java")
        systemProperty("testDataRoot", testData.asFile.relativeTo(rootDir))
        systemProperty("testsRoot", testsRoot.asFile.relativeTo(rootDir))
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
        workingDir = rootDir
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
        workingDir = rootDir
        useJUnitPlatform()
        systemProperty("mokkery.runtimeClasspath", mokkeryRuntimeClasspath.get().asPath)
        systemProperty("idea.ignore.disabled.plugins", "true")
        systemProperty("idea.home.path", rootDir)
    }
}

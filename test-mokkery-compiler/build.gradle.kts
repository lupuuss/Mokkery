plugins {
    kotlin("jvm")
}

val mokkeryRuntimeClasspath: Configuration by configurations.creating

kotlin {
    optInMokkeryDelicateAndInternals()
}

dependencies {
    testImplementation(project(":mokkery-plugin"))

    testImplementation(libs.kotlin.testJunit5)
    testImplementation(libs.kotlin.compilerTestFramework)
    testImplementation(libs.kotlin.compiler)

    mokkeryRuntimeClasspath(project(":mokkery-runtime"))

    // Dependencies required to run the internal test framework.
    testRuntimeOnly(kotlin("test"))
    testRuntimeOnly(libs.kotlin.reflect)
    testRuntimeOnly(libs.kotlin.scriptRuntime)
    testRuntimeOnly(libs.kotlin.annotationsJvm)
}

tasks.register<JavaExec>("generateTests") {
    group = "verification"
    systemProperty("mokkeryRuntime.classpath", mokkeryRuntimeClasspath.asPath)
    inputs
        .dir(layout.projectDirectory.dir("src/test/data"))
        .withPropertyName("testData")
        .withPathSensitivity(PathSensitivity.RELATIVE)
    outputs
        .dir(layout.projectDirectory.dir("src/test/java"))
        .withPropertyName("generatedTests")
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("dev.mokkery.tests.GenerateTestsKt")
    workingDir = rootDir
}

tasks.withType<Test> {
    dependsOn(mokkeryRuntimeClasspath)
    inputs
        .dir(layout.projectDirectory.dir("src/test/data"))
        .withPropertyName("testData")
        .withPathSensitivity(PathSensitivity.RELATIVE)

    workingDir = rootDir

    useJUnitPlatform()

    systemProperty("mokkeryRuntime.classpath", mokkeryRuntimeClasspath.asPath)

    // Properties required to run the internal test framework.
    systemProperty("idea.ignore.disabled.plugins", "true")
    systemProperty("idea.home.path", rootDir)
}

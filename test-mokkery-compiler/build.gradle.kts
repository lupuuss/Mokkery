plugins {
    kotlin("jvm")
}


kotlin {
    optInMokkeryDelicateAndInternals()
}

val mokkeryRuntimeClasspath by configurations.registering

dependencies {
    testImplementation(project(":mokkery-plugin"))
    testImplementation(project(":mokkery-core-tooling"))
    testImplementation(libs.kotlin.compiler)
    testImplementation(libs.kotlin.compiler.test.framework)
    testImplementation(libs.kotlin.test.junit5)
    testRuntimeOnly(libs.kotlin.reflect)
    testRuntimeOnly(libs.kotlin.script.runtime)
    testRuntimeOnly(libs.kotlin.annotations.jvm)
    mokkeryRuntimeClasspath(project(":mokkery-runtime"))
}

tasks.register<JavaExec>("generateTests") {
    group = "verification"
    systemProperty("mokkeryRuntime.classpath", mokkeryRuntimeClasspath.get().asPath)
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

    systemProperty("mokkeryRuntime.classpath", mokkeryRuntimeClasspath.get().asPath)

    // Properties required to run the internal test framework.
    systemProperty("idea.ignore.disabled.plugins", "true")
    systemProperty("idea.home.path", rootDir)
}

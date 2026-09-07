plugins {
    id("mokkery-jvm")
    alias(libs.plugins.kotlinx.benchmark)
    alias(libs.plugins.kotlin.allopen)
}

allOpen {
    annotation("kotlinx.benchmark.State")
    annotation("org.openjdk.jmh.annotations.State")
}

kotlin {
    optInMokkeryDelicateAndInternals()
}

dependencies {
    kotlinCompilerPluginClasspath(project(":mokkery-plugin"))
    implementation(project(":mokkery-core"))
    implementation(project(":mokkery-runtime"))
    implementation(libs.kotlinx.benchmark.runtime)
}

benchmark {
    configurations {
        named("main") {
            warmups = 5
            iterations = 10
            iterationTime = 500
            iterationTimeUnit = "ms"
            advanced("jvmForks", 2)
            reportFormat = "json"
        }
        register("quick") {
            warmups = 2
            iterations = 3
            iterationTime = 300
            iterationTimeUnit = "ms"
            advanced("jvmForks", 1)
            reportFormat = "text"
        }
    }
    targets {
        register("main")
    }
}

val allocationBenchmark by tasks.registering(JavaExec::class) {
    group = "benchmark"
    description = "Runs benchmarks with the JMH gc profiler, reporting bytes allocated per operation"
    val jar = tasks.named("mainBenchmarkJar")
    dependsOn(jar)
    classpath = files(jar)
    mainClass.set("org.openjdk.jmh.Main")
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    )
    args("-prof", "gc", "-f", "1", "-wi", "3", "-i", "5", "-r", "500ms")
    providers.gradleProperty("benchmarkFilter").orNull?.let { args(it) }
    outputs.upToDateWhen { false }
}

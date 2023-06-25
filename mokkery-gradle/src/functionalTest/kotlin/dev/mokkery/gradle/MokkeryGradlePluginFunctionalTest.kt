package dev.mokkery.gradle

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import java.io.File

class MokkeryGradlePluginFunctionalTest {

    private val testProjectDir = File("../test-mokkery")

    @Test
    fun `test all unit tests build and passes`() {
        GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments( "-PisTest=true", "-PmokkeryVersion=1.0", "clean", "allTests")
            .withPluginClasspath()
            .forwardStdOutput(System.out.writer())
            .build()
    }
}

package dev.mokkery

import dev.mokkery.tests.AbstractMokkeryDiagnosticTest
import org.jetbrains.kotlin.compiler.plugin.devkit.generateDevKitTestsWithJUnit5

fun main() = generateDevKitTestsWithJUnit5 {
    testClass<AbstractMokkeryDiagnosticTest> { model("diagnostic") }
}

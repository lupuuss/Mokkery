package dev.mokkery.tests

import org.jetbrains.kotlin.generators.dsl.junit5.generateTestGroupSuiteWithJUnit5

abstract class AbstractMinimumMokkeryDiagnosticTest : BaseMokkeryDiagnosticTest(ClasspathBasedStandardLibrariesPathProvider)

fun main() {
    generateTestGroupSuiteWithJUnit5 {
        testGroup(
            testDataRoot = System.getProperty("testDataRoot"),
            testsRoot = System.getProperty("testsRoot"),
        ) {
            testClass<AbstractMinimumMokkeryDiagnosticTest> { model("diagnostic") }
        }
    }
}

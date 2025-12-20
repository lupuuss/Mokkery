package dev.mokkery.tests

import org.jetbrains.kotlin.generators.dsl.junit5.generateTestGroupSuiteWithJUnit5


fun main() {
    generateTestGroupSuiteWithJUnit5 {
        testGroup(
            testDataRoot = "test-mokkery-compiler/src/test/data",
            testsRoot = "test-mokkery-compiler/src/test/java",
        ) {
            testClass<AbstractMokkeryDiagnosticTest> { model("diagnostic") }
        }
    }
}

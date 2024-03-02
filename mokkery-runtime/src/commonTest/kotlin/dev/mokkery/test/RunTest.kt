package dev.mokkery.test

expect class TestResult

expect fun runTest(block: suspend () -> Unit): TestResult

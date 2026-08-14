@file:Suppress("unused")
package dev.mokkery.benchmark

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.BenchmarkMode
import kotlinx.benchmark.BenchmarkTimeUnit
import kotlinx.benchmark.Mode
import kotlinx.benchmark.OutputTimeUnit
import kotlinx.benchmark.Scope
import kotlinx.benchmark.State

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.NANOSECONDS)
class MockCreationBenchmark {

    @Benchmark
    fun create(): Any = mock<BenchmarkService>()

    @Benchmark
    fun createWithSetupAnswer(): Any = mock<BenchmarkService> {
        every { compute(any()) } returns 1
    }

    @Benchmark
    fun createThenSetupAnswer(): Any {
        val service = mock<BenchmarkService>()
        every { service.compute(any()) } returns 1
        return service
    }

    @Benchmark
    fun createBaseline(): Any = BaselineBenchmarkService()
}

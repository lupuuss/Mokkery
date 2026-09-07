@file:Suppress("unused")

package dev.mokkery.benchmark

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.exactly
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.BenchmarkMode
import kotlinx.benchmark.BenchmarkTimeUnit
import kotlinx.benchmark.Mode
import kotlinx.benchmark.OutputTimeUnit
import kotlinx.benchmark.Param
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State
import org.openjdk.jmh.annotations.Level

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.MICROSECONDS)
class MockVerifyBenchmark {

    @Param("1", "16", "256")
    var count: Int = 0

    private lateinit var service: BenchmarkService

    @Setup(Level.Invocation)
    fun setup() {
        service = mock {
            every { compute(any()) } returns 1
        }
        repeat(count) { service.compute(it) }
    }

    @Benchmark
    fun verifyCalls(): Int {
        verify(exactly(count)) { service.compute(any()) }
        return count
    }
}

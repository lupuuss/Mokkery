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
import kotlinx.benchmark.Param
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State
import org.openjdk.jmh.annotations.Level

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.MICROSECONDS)
class MockCallBenchmark {

    @Param("256")
    var count: Int = 0

    private lateinit var service: BenchmarkService
    private lateinit var baseline: BaselineBenchmarkService

    @Setup(Level.Invocation)
    fun setup() {
        service = mock {
            every { compute(any()) } returns 1
        }
        baseline = BaselineBenchmarkService()
    }

    @Benchmark
    fun calls(): Int {
        var acc = 0
        repeat(count) { acc += service.compute(it) }
        return acc
    }

    @Benchmark
    fun callsBaseline(): Int {
        var acc = 0
        repeat(count) { acc += baseline.compute(it) }
        return acc
    }
}

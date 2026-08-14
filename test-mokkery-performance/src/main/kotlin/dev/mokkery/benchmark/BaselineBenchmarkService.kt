package dev.mokkery.benchmark

class BaselineBenchmarkService(private val answer: Int = 1) : BenchmarkService {

    val computeCalls = mutableListOf<Int>()

    override fun compute(input: Int): Int {
        computeCalls += input
        return answer
    }

    override fun describe(input: String): String {
        throw UnsupportedOperationException()
    }

    override fun consume(value: Int) {
        throw UnsupportedOperationException()
    }
}

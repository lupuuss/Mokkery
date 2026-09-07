package dev.mokkery.benchmark

interface BenchmarkService {

    fun compute(input: Int): Int

    fun describe(input: String): String

    fun consume(value: Int)
}

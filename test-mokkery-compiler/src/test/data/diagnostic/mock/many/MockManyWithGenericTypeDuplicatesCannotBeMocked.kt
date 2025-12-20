import dev.mokkery.mockMany

fun main() {
    mockMany<List<Int>, <!DUPLICATE_TYPES_FOR_MOCK_MANY!>List<String><!>, List<Double>>()
}

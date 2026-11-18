import dev.mokkery.mockMany

fun main() {
    mockMany<AutoCloseable, <!DUPLICATE_TYPES_FOR_MOCK_MANY!>AutoCloseable<!>>()
}

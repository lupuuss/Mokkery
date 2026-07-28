import dev.mokkery.mock
import dev.mokkery.spy
import dev.mokkery.mockMany
import dev.mokkery.mockable.annotations.Mockable
import kotlin.CharSequence

@Mockable
class MockableClass private constructor()

fun test(i: MockableClass) {
    spy(i)
    mock<MockableClass>()
    mockMany<MockableClass, CharSequence>()
}

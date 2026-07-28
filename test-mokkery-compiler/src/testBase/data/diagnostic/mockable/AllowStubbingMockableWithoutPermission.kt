import dev.mokkery.mock
import dev.mokkery.spy
import dev.mokkery.mockMany
import dev.mokkery.mockable.annotations.Mockable
import kotlin.CharSequence

abstract class AbstractToStub(p: MockableClass)

@Mockable
class MockableClass private constructor()

fun test(i: AbstractToStub) {
    spy(i)
    mock<AbstractToStub>()
    mockMany<AbstractToStub, CharSequence>()
}

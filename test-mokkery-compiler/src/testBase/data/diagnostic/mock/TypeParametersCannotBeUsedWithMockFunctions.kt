import dev.mokkery.mock
import dev.mokkery.spy
import dev.mokkery.mockMany
import kotlin.CharSequence

inline fun <reified T : Any> myMock(obj: T) = mock<<!INDIRECT_INTERCEPTION!>T<!>>()

inline fun <reified T : Any> mySpy(obj: T) = spy<<!INDIRECT_INTERCEPTION!>T<!>>(obj)

inline fun <reified T : Any> myMockMany(obj: T) = mockMany<<!INDIRECT_INTERCEPTION!>T<!>, CharSequence>()

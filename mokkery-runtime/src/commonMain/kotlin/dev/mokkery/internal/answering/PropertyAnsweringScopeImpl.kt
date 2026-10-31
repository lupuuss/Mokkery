package dev.mokkery.internal.answering

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.answering.Answer
import dev.mokkery.answering.BlockingAnsweringScope
import dev.mokkery.answering.MutablePropertyAnsweringScope
import dev.mokkery.answering.PropertyAnsweringScope
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.utils.unsafeCast
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

internal val <T> PropertyAnsweringScope<T>.kProperty: KProperty<T>
    get() = this.unsafeCast<PropertyAnsweringScopeImpl<T>>().property

internal val <T> MutablePropertyAnsweringScope<T>.kProperty: KMutableProperty<T>
    get() = this.unsafeCast<MutablePropertyAnsweringScopeImpl<T>>().property

internal class PropertyAnsweringScopeImpl<T>(
    private val answering: AnsweringRegistry,
    private val getterTemplate: CallTemplate,
    val property: KProperty<T>,
) : PropertyAnsweringScope<T> {

    @DelicateMokkeryApi
    override fun answers(answer: Answer<T>) {
        answering.setup(getterTemplate, answer)
    }
}

internal class MutablePropertyAnsweringScopeImpl<T>(
    answering: AnsweringRegistry,
    getterTemplate: CallTemplate,
    setterTemplate: CallTemplate,
    val property: KMutableProperty<T>,
) : MutablePropertyAnsweringScope<T> {

    override val getter: BlockingAnsweringScope<T> = UnifiedAnsweringScope(answering, getterTemplate)

    override val setter: BlockingAnsweringScope<Unit> = UnifiedAnsweringScope(answering, setterTemplate)
}

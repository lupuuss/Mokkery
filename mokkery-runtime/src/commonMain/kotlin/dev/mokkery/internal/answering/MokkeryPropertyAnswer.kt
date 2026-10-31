package dev.mokkery.internal.answering

import dev.mokkery.MokkeryBlockingCallScope
import dev.mokkery.answering.Answer
import dev.mokkery.answering.MokkeryMutableProperty
import dev.mokkery.answering.MokkeryProperty
import dev.mokkery.call
import dev.mokkery.internal.utils.unsafeCast
import dev.mokkery.rendering.MokkeryRenderingScope
import dev.mokkery.rendering.Renderable
import dev.mokkery.rendering.renderOrToString
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

internal class MokkeryPropertyGetAnswer<T>(
    private val property: MokkeryProperty<T>,
    private val kProperty: KProperty<*>,
) : Answer.Blocking<T>, Renderable {

    override fun call(scope: MokkeryBlockingCallScope): T = property.getValue(scope, kProperty)

    context(scope: MokkeryRenderingScope)
    override fun render(): String = "by ${property.renderOrToString()}"
}

internal class MokkeryPropertySetAnswer<T>(
    private val mokkeryProperty: MokkeryMutableProperty<T>,
    private val property: KMutableProperty<*>,
) : Answer.Blocking<Unit>, Renderable {

    override fun call(scope: MokkeryBlockingCallScope) {
        mokkeryProperty.setValue(scope, property, scope.call.args[0].value.unsafeCast())
    }

    context(scope: MokkeryRenderingScope)
    override fun render(): String = "by ${mokkeryProperty.renderOrToString()}"
}

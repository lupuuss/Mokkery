package dev.mokkery.internal.answering.autofill

import dev.mokkery.answering.autofill.AutofillProvider
import net.bytebuddy.ByteBuddy
import org.objenesis.ObjenesisStd
import java.lang.reflect.Modifier
import kotlin.reflect.KClass

internal object JvmReflectionValueProvider : AutofillProvider<Any> {

    private val objenesis = ObjenesisStd()
    private val byteBuddy = ByteBuddy()

    override fun provide(type: KClass<*>) = when {
        type == Any::class -> Any()
        else -> createByReflection(type.java)
    }.asAutofillProvided()

    private fun createByReflection(cls: Class<*>): Any = when {
        cls.isAbstract -> byteBuddy
            .subclass(cls)
            .make()
            .load(cls.classLoader)
            .loaded
            .let { objenesis.newInstance(it) }
        else -> objenesis.newInstance(cls)
    }
}

private val Class<*>.isAbstract get() = Modifier.isAbstract(modifiers)

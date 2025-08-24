package dev.mokkery.internal.answering.autofill

import com.android.dx.stock.ProxyBuilder
import dev.mokkery.answering.autofill.AutofillProvider
import net.bytebuddy.ByteBuddy
import org.objenesis.ObjenesisStd
import java.lang.reflect.Modifier
import kotlin.reflect.KClass

internal object AndroidReflectionValueProvider : AutofillProvider<Any> {

    private val objenesis = ObjenesisStd()
    private val isAndroid by lazy { isAndroidEnvironment() }
    private val byteBuddy by lazy { ByteBuddy() }

    override fun provide(type: KClass<*>) = when {
        type == Any::class -> Any()
        else -> instantiate(type.java)
    }.asAutofillProvided()

    private fun instantiate(cls: Class<*>): Any = when {
        cls.isAbstract -> objenesis.newInstance(createProxyClass(cls))
        else -> objenesis.newInstance(cls)
    }

    private fun createProxyClass(cls: Class<*>): Class<*> = when {
        isAndroid -> when {
            cls.isInterface -> buildProxyImplementationOf(cls)
            else -> buildProxySubclassOf(cls)
        }
        else -> byteBuddy
            .subclass(cls)
            .make()
            .load(cls.classLoader)
            .loaded
    }

    private fun buildProxySubclassOf(cls: Class<*>): Class<*> = ProxyBuilder.forClass(cls)
        .parentClassLoader(cls.classLoader)
        .buildProxyClass()

    private fun buildProxyImplementationOf(cls: Class<*>) = ProxyBuilder.forClass(Any::class.java)
        .parentClassLoader(cls.classLoader)
        .implementing(cls)
        .buildProxyClass()

    private fun isAndroidEnvironment(): Boolean {
        if (findClass("android.content.Context") == null) return false
        val vendor = System.getProperty("java.vendor").orEmpty()
        return vendor.contains("android", ignoreCase = true)
    }

    fun findClass(name: String): Class<*>? = try {
        Class.forName(name)
    } catch (_: ClassNotFoundException) {
        null
    }
}

private val Class<*>.isAbstract get() = Modifier.isAbstract(modifiers)

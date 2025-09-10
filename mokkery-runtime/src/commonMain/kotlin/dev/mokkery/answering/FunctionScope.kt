package dev.mokkery.answering

import dev.drewhamilton.poko.Poko
import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.internal.IncorrectArgsForSuperMethodException
import dev.mokkery.internal.MissingSuperMethodException
import dev.mokkery.internal.SuperTypeMustBeSpecifiedException
import dev.mokkery.internal.utils.bestName
import dev.mokkery.internal.utils.unsafeCast
import dev.mokkery.internal.utils.unsafeCastOrNull
import kotlin.reflect.KClass


/**
 * Provides a set of mocked function related operations that might be required for implementing [Answer].
 */
@DelicateMokkeryApi
@Poko
@Deprecated(AnswerDeprecationMessage, level = DeprecationLevel.ERROR)
public class FunctionScope internal constructor(
    /**
     * Return type of mocked method.
     */
    public val returnType: KClass<*>,
    /**
     * Args passed to mocked method. If method has extension receiver it is passed at the start of this list.
     */
    public val args: List<Any?>,
    /**
     * Reference to this mock.
     */
    public val self: Any?,

    /**
     * This map contains available super calls as lambdas of type `(List<Any?>) -> Any?` or
     * `suspend (List<Any?>) -> Any?` depending on a function type.
     *
     * Use [callSuper], [callSuspendSuper], [callOriginal], [callSuspendOriginal] for convenience.
     */
    public val supers: Map<KClass<*>, Function<Any?>>,
    internal val classSupertypes: List<KClass<*>>
) {

    /**
     * Returns argument with [index] from [args] and expects that it is an instance of type [T].
     */
    public inline fun <reified T> arg(index: Int): T = args[index] as T

    /**
     * Calls super method of [superType] with given [args]
     */
    public fun callSuper(superType: KClass<*>, args: List<Any?>): Any? {
        if (this.args.size != args.size) {
            throw IncorrectArgsForSuperMethodException(this.args.size, args.size)
        }
        return supers[superType]
            .unsafeCastOrNull<(List<Any?>) -> Any?>()
            .let { it ?: throw MissingSuperMethodException(superType) }
            .invoke(args)
    }

    /**
     * Just like [callSuper] but for suspend calls.
     */
    public suspend fun callSuspendSuper(superType: KClass<*>, args: List<Any?>): Any? {
        if (this.args.size != args.size) {
            throw IncorrectArgsForSuperMethodException(this.args.size, args.size)
        }
        return supers[superType]
            .unsafeCastOrNull<suspend (List<Any?>) -> Any?>()
            .let { it ?: throw MissingSuperMethodException(superType) }
            .invoke(args)
    }

    /**
     * Calls original method implementation with given [args].
     */
    public fun callOriginal(args: List<Any?>): Any? {
        checkArgs(args)
        val superType = resolveOriginalSupertype()
        return supers
            .getValue(superType)
            .unsafeCast<(List<Any?>) -> Any?>()
            .invoke(args)
    }

    /**
     * Just like [callOriginal] but for suspend calls.
     */
    public suspend fun callSuspendOriginal(args: List<Any?>): Any? {
        checkArgs(args)
        val superType = resolveOriginalSupertype()
        return supers
            .getValue(superType)
            .unsafeCast<suspend (List<Any?>) -> Any?>()
            .invoke(args)
    }

    private fun resolveOriginalSupertype(): KClass<*> {
        val superCandidates = classSupertypes.filter(supers::contains)
        if (superCandidates.isEmpty()) throw MissingSuperMethodException(classSupertypes)
        val superType = superCandidates
            .singleOrNull()
            ?: throw SuperTypeMustBeSpecifiedException(
                "Multiple original super calls available ${superCandidates.map(KClass<*>::bestName)}!"
            )
        return superType
    }

    private fun checkArgs(args: List<Any?>) {
        if (this.args.size != args.size) {
            throw IncorrectArgsForSuperMethodException(this.args.size, args.size)
        }
    }
}

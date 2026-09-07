package dev.mokkery.internal

import dev.mokkery.internal.utils.bestName
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * Simplified [kotlin.reflect.KType] for Mokkery
 */
internal interface MType {

    val type: KClass<*>

    /**
     * `null` means star projection.
     */
    val arguments: List<KClass<*>?>
}

internal fun KType.toMType(): MType = MTypeImpl(
    type = requireClassifierClass(),
    arguments = arguments.map { it.type?.requireClassifierClass() }
)

internal fun KClass<*>.toMType(args: List<KClass<*>?>): MType = MTypeImpl(this, args)

private fun KType.requireClassifierClass(): KClass<*> = classifier as? KClass<*> ?: mokkeryRuntimeError("Only class types are supported, but $this given!")

private data class MTypeImpl(override val type: KClass<*>, override val arguments: List<KClass<*>?>) : MType {

    override fun toString(): String = buildString {
        append(type.bestName())
        if (arguments.isNotEmpty()) {
            append("<")
            append(arguments.joinToString { it?.bestName() ?: "*" })
            append(">")
        }
    }
}

package dev.mokkery.internal.options

import dev.mokkery.annotations.InternalMokkeryApi
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty

@Suppress("UNCHECKED_CAST")
@InternalMokkeryApi
public abstract class MokkeryOptionsContainer: Iterable<MokkeryOption<Any?>> {

    private val _options = mutableMapOf<String, MokkeryOption<Any?>>()
    private val _containers = mutableListOf<MokkeryOptionsContainer>()


    public override operator fun iterator(): Iterator<MokkeryOption<Any?>> = _options
        .values
        .asSequence()
        .plus(_containers.flatten())
        .iterator()

    public operator fun get(name: String): MokkeryOption<Any?>? {
        return _options[name] ?: _containers.firstNotNullOfOrNull { it[name] }
    }

    public operator fun plusAssign(option: MokkeryOption<*>) {
        _options += (option.name to option as MokkeryOption<Any?>)
    }

    public operator fun plusAssign(container: MokkeryOptionsContainer) {
        _containers += container
    }

    override fun toString(): String = "MokkeryOptionsContainer<${this::class.simpleName}>[${joinToString()}]"
}


internal interface MokkeryNamespace {

    val name: String
    fun createName(name: String): String

    companion object {

        val root = object : MokkeryNamespace {

            override val name: String = ""

            override fun createName(name: String): String = name
        }

        val named: PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, MokkeryNamespace>>
            get() = PropertyDelegateProvider { _, it ->
                ReadOnlyProperty { _, _ ->
                    object : MokkeryNamespace {
                        override val name: String = it.name
                        override fun createName(name: String): String = "${this.name}.$name"
                    }
                }
            }
    }
}

internal fun <T> MokkeryNamespace.defaultSingleOption(
    type: MokkeryOptionType<T>,
    description: String,
    defaultValue: T,
): MokkeryOptionPropertyDelegateProvider<T> = option(
    type = type,
    description = description,
    defaultValue = defaultValue,
    required = false,
    allowMultipleOccurrences = false,
)


internal fun <T> MokkeryNamespace.option(
    type: MokkeryOptionType<T>,
    description: String,
    defaultValue: T?,
    required: Boolean,
    allowMultipleOccurrences: Boolean,
): MokkeryOptionPropertyDelegateProvider<T> = PropertyDelegateProvider { container, it ->
    val option = MokkeryOption(
        name = this.createName(it.name),
        description = description,
        required = required,
        allowMultipleOccurrences = allowMultipleOccurrences,
        type = type,
        defaultValue = defaultValue,
    )
    container += option
    ReadOnlyProperty { _, _ -> option }
}

internal typealias MokkeryOptionPropertyDelegateProvider<T> = PropertyDelegateProvider<
        MokkeryOptionsContainer,
        ReadOnlyProperty<Any?, MokkeryOption<T>>
        >

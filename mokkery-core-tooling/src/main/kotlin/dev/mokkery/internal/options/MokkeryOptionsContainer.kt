package dev.mokkery.internal.options

import dev.mokkery.annotations.InternalMokkeryApi
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty

@Suppress("UNCHECKED_CAST")
@InternalMokkeryApi
public abstract class MokkeryOptionsContainer: Iterable<MokkeryOption<Any>> {

    private val _options = mutableMapOf<String, MokkeryOption<Any>>()
    private val _containers = mutableListOf<MokkeryOptionsContainer>()


    public override operator fun iterator(): Iterator<MokkeryOption<Any>> = _options
        .values
        .asSequence()
        .plus(_containers.flatten())
        .iterator()

    public operator fun get(name: String): MokkeryOption<Any>? {
        return _options[name] ?: _containers.firstNotNullOfOrNull { it[name] }
    }

    public operator fun plusAssign(option: MokkeryOption<*>) {
        _options += (option.name to option as MokkeryOption<Any>)
    }

    public operator fun plusAssign(container: MokkeryOptionsContainer) {
        _containers += container
    }

    override fun toString(): String = "MokkeryOptionsContainer<${this::class.simpleName}>[${joinToString()}]"
}


@InternalMokkeryApi
public interface MokkeryOptionsNamespace {

    public val name: String
    public fun createName(name: String): String

    public companion object {

        public val root: MokkeryOptionsNamespace = object : MokkeryOptionsNamespace {

            override val name: String = ""

            override fun createName(name: String): String = name
        }

        public val named: PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, MokkeryOptionsNamespace>>
            get() = PropertyDelegateProvider { _, it ->
                ReadOnlyProperty { _, _ ->
                    object : MokkeryOptionsNamespace {
                        override val name: String = it.name
                        override fun createName(name: String): String = "${this.name}.$name"
                    }
                }
            }
    }
}

@InternalMokkeryApi
public fun <T> MokkeryOptionsNamespace.defaultSingleOption(
    type: MokkeryOptionType<T>,
    description: String,
    defaultValue: T,
): MokkeryOptionPropertyDelegateProvider<T> = option(
    type = type,
    description = description,
    defaultValues = listOf(defaultValue),
    required = false,
    allowMultipleOccurrences = false,
)

@InternalMokkeryApi
public fun <T> MokkeryOptionsNamespace.option(
    type: MokkeryOptionType<T>,
    description: String,
    defaultValues: List<T>,
    required: Boolean,
    allowMultipleOccurrences: Boolean,
): MokkeryOptionPropertyDelegateProvider<T> = PropertyDelegateProvider { container, it ->
    val option = MokkeryOption(
        name = this.createName(it.name),
        description = description,
        required = required,
        allowMultipleOccurrences = allowMultipleOccurrences,
        type = type,
        defaultValues = defaultValues,
    )
    container += option
    ReadOnlyProperty { _, _ -> option }
}

@InternalMokkeryApi
public typealias MokkeryOptionPropertyDelegateProvider<T> = PropertyDelegateProvider<
        MokkeryOptionsContainer,
        ReadOnlyProperty<Any?, MokkeryOption<T>>
        >

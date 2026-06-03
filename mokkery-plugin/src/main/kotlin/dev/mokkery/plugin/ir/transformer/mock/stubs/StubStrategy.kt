package dev.mokkery.plugin.ir.transformer.mock.stubs

import dev.mokkery.context.MokkeryContext
import dev.mokkery.plugin.core.context.asMokkeryContext
import dev.mokkery.plugin.core.context.createValueKey
import dev.mokkery.plugin.core.context.readValue
import dev.mokkery.plugin.core.ir.irBuiltIns
import dev.mokkery.plugin.core.ir.transformer.TransformerScope
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.IrTypeSystemContext
import org.jetbrains.kotlin.ir.types.IrTypeSystemContextImpl
import org.jetbrains.kotlin.ir.util.constructors

interface StubStrategyScope : TransformerScope

context(scope: StubStrategyScope)
val StubStrategyScope.builder: IrBuilderWithScope
    get() = scope.readValue(builderKey)

context(scope: StubStrategyScope)
val typeSystem: IrTypeSystemContext
    get() = scope.readValue(typeSystemKey)

context(scope: StubStrategyScope)
val strategy: StubStrategy
    get() = scope.readValue(strategyKey)

fun IrBuilderWithScope.asMokkeryContext() = asMokkeryContext(builderKey)
fun IrTypeSystemContext.asMokkeryContext() = asMokkeryContext(typeSystemKey)
fun StubStrategy.asMokkeryContext() = asMokkeryContext(strategyKey)

private val builderKey = createValueKey<IrBuilderWithScope>()
private val typeSystemKey = createValueKey<IrTypeSystemContext>()
private val strategyKey = createValueKey<StubStrategy>()

context(scope: StubStrategyScope)
fun StubStrategy.provideConstructorWithStubs(
    cls: IrClass, visibilities: Set<DescriptorVisibility>
) = cls.constructors.firstNotNullOfOrNull { ctor ->
    if (ctor.visibility !in visibilities) return@firstNotNullOfOrNull null
    val stubs = ctor.parameters.mapNotNull { this.provide(it.type) }
    if (stubs.size != ctor.parameters.size) return@firstNotNullOfOrNull null
    ctor to stubs
}

fun StubStrategyScope.with(builder: IrBuilderWithScope) = stubStrategyScope(
    strategy,
    builder,
)

interface Stub {
    val expression: IrExpression
}

fun stub(expr: IrExpression): Stub = object : Stub {
    override val expression = expr
}

fun stub(block: () -> IrExpression): Stub = object : Stub {
    override val expression by lazy { block() }
}

fun interface StubStrategy {

    context(scope: StubStrategyScope)
    fun provide(type: IrType): Stub?

    companion object {

        fun default(config: MokkeryStubsConfig) = StubStrategy {
            add(TypeParameterStubStrategy)
            add(NullableStubStrategy)
            add(CoreStubStrategy)
            add(LambdaStubStrategy)
            add(CollectionStubStrategy)
            add(ArrayStubStrategy)
            add(EnumStubStrategy)
            add(ConstructableClassStubStrategy(config.allowConcreteClassInstantiation))
            add(OverridableTypeStubStrategy(config.allowClassInheritance))
        }
    }
}

fun StubStrategy(vararg strategies: StubStrategy): StubStrategy = StubStrategy(strategies.asList())

fun StubStrategy(block: MutableList<StubStrategy>.() -> Unit): StubStrategy = StubStrategy(buildList { block() })

fun StubStrategy(strategies: List<StubStrategy>): StubStrategy = StubStrategy { type ->
    strategies.firstNotNullOfOrNull { strategy ->
        strategy.provide(type)
    }
}

context(scope: TransformerScope)
fun stubStrategyScope(
    strategy: StubStrategy,
    builder: IrBuilderWithScope,
): StubStrategyScope {
    val irBuiltIns = irBuiltIns
    return object : StubStrategyScope {
        override val mokkeryContext: MokkeryContext = scope.mokkeryContext +
                strategy.asMokkeryContext() +
                builder.asMokkeryContext() +
                IrTypeSystemContextImpl(irBuiltIns).asMokkeryContext()
    }
}

package dev.mokkery.plugin.stubs

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.IrTypeSystemContext
import org.jetbrains.kotlin.ir.types.IrTypeSystemContextImpl
import org.jetbrains.kotlin.ir.util.constructors

interface StubStrategyScope {

    val compilerConfig: CompilerConfiguration
    val builder: IrBuilderWithScope
    val plugin: IrPluginContext
    val typeSystem: IrTypeSystemContext
    val strategy: StubStrategy
}

val StubStrategyScope.irBuiltIns get() = plugin.irBuiltIns

val StubStrategyScope.irFactory get() = plugin.irFactory

context(scope: StubStrategyScope)
fun StubStrategy.provideConstructorWithStubs(
    cls: IrClass, visibilities: Set<DescriptorVisibility>
) = cls.constructors.firstNotNullOfOrNull { ctor ->
    if (ctor.visibility !in visibilities) return@firstNotNullOfOrNull null
    val stubs = ctor.parameters.mapNotNull { this.provide(it.type) }
    if (stubs.size != ctor.parameters.size) return@firstNotNullOfOrNull null
    ctor to stubs
}

fun StubStrategyScope.with(builder: IrBuilderWithScope) = StubStrategyScope(
    strategy = strategy,
    compilerConfig = compilerConfig,
    pluginContext = plugin,
    builder = builder,
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

fun StubStrategyScope(
    strategy: StubStrategy,
    compilerConfig: CompilerConfiguration,
    pluginContext: IrPluginContext,
    builder: IrBuilderWithScope,
): StubStrategyScope = object : StubStrategyScope {

    override val compilerConfig = compilerConfig
    override val builder = builder
    override val plugin = pluginContext
    override val typeSystem: IrTypeSystemContext by lazy { IrTypeSystemContextImpl(plugin.irBuiltIns) }
    override val strategy = strategy
}

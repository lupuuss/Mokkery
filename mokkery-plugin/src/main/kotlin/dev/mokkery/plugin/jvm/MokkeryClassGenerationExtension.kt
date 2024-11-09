package dev.mokkery.plugin.jvm

import dev.mokkery.plugin.core.Mokkery
import org.jetbrains.kotlin.backend.jvm.extensions.ClassGenerator
import org.jetbrains.kotlin.backend.jvm.extensions.ClassGeneratorExtension
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.messageCollector
import org.jetbrains.kotlin.ir.declarations.IrClass

class MokkeryClassGenerationExtension(
    configuration: CompilerConfiguration
) : ClassGeneratorExtension {

    private val collector = configuration.messageCollector

    override fun generateClass(generator: ClassGenerator, declaration: IrClass?): ClassGenerator {
        if (declaration == null) return generator
        if (declaration.origin != Mokkery.Origin) return generator
        return JvmParamAssertionSkippingGenerator(generator, collector)
    }
}


package dev.mokkery.tests

import dev.mokkery.internal.options.MokkeryOption
import dev.mokkery.internal.options.MokkeryOptions.Core
import dev.mokkery.internal.options.MokkeryOptions.Stubs
import dev.mokkery.plugin.core.configurationKey
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.test.directives.model.Directive
import org.jetbrains.kotlin.test.directives.model.DirectiveApplicability
import org.jetbrains.kotlin.test.directives.model.SimpleDirective
import org.jetbrains.kotlin.test.directives.model.SimpleDirectivesContainer

@Suppress("unused")
object MokkeryDirectives : OptionDirectivesContainer() {

    val DISABLE_FIR_DIAGNOSTICS by flagOptionDirective(
        option = Core.enableFirDiagnostics,
        description = "Disables Mokkery's FIR diagnostics",
        value = false
    )
    val IGNORE_FINAL_MEMBERS by flagOptionDirective(option = Core.ignoreFinalMembers)
    val IGNORE_INLINE_MEMBERS by flagOptionDirective(option = Core.ignoreInlineMembers)
    val STUBS_ALLOW_CONCRETE_CLASS_INSTANTIATION by flagOptionDirective(option = Stubs.allowConcreteClassInstantiation)
    val STUBS_ALLOW_CLASS_INHERITANCE by flagOptionDirective(option = Stubs.allowClassInheritance)
}

abstract class OptionDirectivesContainer : SimpleDirectivesContainer() {

    private val directivesMapping = mutableMapOf<Directive, DirectiveToOptionMapping>()

    fun writeDirective(directive: Directive, configuration: CompilerConfiguration) {
        val mapping = directivesMapping[directive] ?: return
        configuration.put(
            mapping.option.configurationKey,
            listOf(mapping.value())
        )
    }

    protected fun flagOptionDirective(
        option: MokkeryOption<Boolean>,
        description: String = option.description,
        value: Boolean = true,
    ): DirectiveDelegateProvider<SimpleDirective> {
        return optionDirective(option, description) { value }
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <T> optionDirective(
        option: MokkeryOption<T>,
        description: String = option.description,
        value: () -> T
    ): DirectiveDelegateProvider<SimpleDirective> {
        return DirectiveDelegateProvider {
            val directive = SimpleDirective(
                name = it,
                description = description,
                applicability = DirectiveApplicability.Global
            )
            directivesMapping[directive] = DirectiveToOptionMapping(
                option = option as MokkeryOption<Any?>,
                value = value
            )
            directive
        }
    }

    private class DirectiveToOptionMapping(
        val option: MokkeryOption<Any?>,
        val value: () -> Any?
    )
}


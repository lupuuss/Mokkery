package dev.mokkery.tests

import org.jetbrains.kotlin.test.directives.model.DirectiveApplicability
import org.jetbrains.kotlin.test.directives.model.SimpleDirectivesContainer

object MokkeryDirectives : SimpleDirectivesContainer() {

    val DISABLE_FIR_DIAGNOSTICS by directive("Disables Mokkery's FIR diagnostics")
    val IGNORE_FINAL_MEMBERS by directive("Enable Mokkery's ignoreFinalMembers")
    val IGNORE_INLINE_MEMBERS by directive("Enable Mokkery's ignoreInlineMembers")
    val STUBS_ALLOW_CONCRETE_CLASS_INSTANTIATION by directive("Enable Mokkery's stubs.allowConcreteClassInstantiation")
    val STUBS_ALLOW_CLASS_INHERITANCE by directive("Enable Mokkery's stubs.allowClassInheritance")
}

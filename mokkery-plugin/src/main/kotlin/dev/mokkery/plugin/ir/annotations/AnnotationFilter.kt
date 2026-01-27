package dev.mokkery.plugin.ir.annotations

import dev.mokkery.options.AnnotationSelector
import dev.mokkery.options.AnnotationSelectorInternals
import org.jetbrains.kotlin.backend.jvm.codegen.AnnotationCodegen.Companion.annotationClass
import org.jetbrains.kotlin.ir.declarations.IrMutableAnnotationContainer
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.removeAnnotations
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.name.FqName

fun interface AnnotationFilter {

    fun filter(annotations: List<IrConstructorCall>): List<IrConstructorCall>

    companion object {

        val none = AnnotationFilter { emptyList() }

        val all = AnnotationFilter { it }

        fun named(names: Set<String>): AnnotationFilter {
            val fqNames = names.map { FqName(it) }
            return AnnotationFilter { annotations ->
                annotations.filter { it.annotationClass.kotlinFqName in fqNames }
            }
        }

        fun matches(regex: Regex) = AnnotationFilter { annotations ->
            annotations.filter { it.annotationClass.kotlinFqName.asString().matches(regex) }
        }
    }
}

fun AnnotationSelector.toFilter(): AnnotationFilter = when (this) {
    AnnotationSelectorInternals.All -> AnnotationFilter.all
    AnnotationSelectorInternals.None -> AnnotationFilter.none
    is AnnotationSelectorInternals.Matches -> AnnotationFilter.matches(regex)
    is AnnotationSelectorInternals.Minus -> AnnotationFilter.none
    is AnnotationSelectorInternals.Named ->  AnnotationFilter.named(this.names)
    is AnnotationSelectorInternals.Combined -> AnnotationFilter { annotations ->
        elements.fold(emptyList()) { acc, it ->
            if (it is AnnotationSelectorInternals.Minus) {
                acc - it.selector.toFilter().filter(annotations).toSet()
            } else {
                acc + it.toFilter().filter(annotations)
            }
        }
    }
}

internal fun IrSimpleFunction.deepApplyAnnotationsFilter(filter: AnnotationFilter) {
    applyAnnotationsFilter(filter)
    typeParameters.forEach { it.applyAnnotationsFilter(filter) }
    parameters.forEach { it.deepApplyAnnotationsRule(filter) }
    returnType.applyAnnotationsFilter(filter)
}

private fun IrValueParameter.deepApplyAnnotationsRule(filter: AnnotationFilter) {
    applyAnnotationsFilter(filter)
    type.applyAnnotationsFilter(filter)
}

private fun IrType.applyAnnotationsFilter(filter: AnnotationFilter) {
    val requiredAnnotations = filter.filter(annotations)
    if (annotations == requiredAnnotations) return
    val requiredAnnotationsSet = requiredAnnotations.toSet()
    type.removeAnnotations { it !in requiredAnnotationsSet }
}

private fun IrMutableAnnotationContainer.applyAnnotationsFilter(filter: AnnotationFilter) {
    annotations = filter.filter(annotations)
}

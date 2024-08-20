package dev.mokkery.plugin.diagnostics

import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.diagnostics.error2
import org.jetbrains.kotlin.diagnostics.error3
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirValueParameterSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.name.Name


object MokkeryDiagnostics {

    val INDIRECT_INTERCEPTION by error2<PsiElement, Name, ConeKotlinType>()

    val SEALED_TYPE_CANNOT_BE_INTERCEPTED by error2<PsiElement, Name, ConeKotlinType>()
    val FINAL_TYPE_CANNOT_BE_INTERCEPTED by error2<PsiElement, Name, ConeKotlinType>()
    val PRIMITIVE_TYPE_CANNOT_BE_INTERCEPTED by error2<PsiElement, Name, ConeKotlinType>()
    val FINAL_MEMBERS_TYPE_CANNOT_BE_INTERCEPTED by error3<PsiElement, Name, ConeKotlinType, List<FirBasedSymbol<*>>>()
    val NO_PUBLIC_CONSTRUCTOR_TYPE_CANNOT_BE_INTERCEPTED by error2<PsiElement, Name, ConeKotlinType>()
    val MULTIPLE_SUPER_CLASSES_FOR_MOCK_MANY by error2<PsiElement, Name, List<ConeKotlinType>>()
    val DUPLICATE_TYPES_FOR_MOCK_MANY by error3<PsiElement, ConeKotlinType, Name, String>()
    val FUNCTIONAL_TYPE_ON_JS_FOR_MOCK_MANY by error2<PsiElement, ConeKotlinType, Name>()

    val FUNCTIONAL_PARAM_MUST_BE_LAMBDA by error2<PsiElement, Name, FirValueParameterSymbol>()

}

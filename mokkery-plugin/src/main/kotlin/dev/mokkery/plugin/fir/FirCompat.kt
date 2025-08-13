package dev.mokkery.plugin.fir

import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory0DelegateProvider
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory1DelegateProvider
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory2DelegateProvider
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory3DelegateProvider
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.diagnostics.Severity
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory

fun KtDiagnosticFactoryToRendererMapCompat(
    name: String,
    block: KtDiagnosticFactoryToRendererMap.() -> Unit
) = lazy {
    try {
        KtDiagnosticFactoryToRendererMap(name) { block(it) }.value
    } catch (_: NoClassDefFoundError) {
        callPrimaryConstructorOf<KtDiagnosticFactoryToRendererMap>(name)
            .apply(block)
    }
}

abstract class KtDiagnosticsContainerCompat {

    val container: Any? = try {
        object : KtDiagnosticsContainer() {
            override fun getRendererFactory(): BaseDiagnosticRendererFactory =
                this@KtDiagnosticsContainerCompat.getRendererFactory()
        }
    } catch (_: NoClassDefFoundError) {
        null
    }

    abstract fun getRendererFactory(): BaseDiagnosticRendererFactory

    inline fun <reified P : PsiElement> error0(): DiagnosticFactory0DelegateProvider {
        return try {
            context(container as KtDiagnosticsContainer) {
                org.jetbrains.kotlin.diagnostics.error0<P>()
            }
        } catch (_: Exception) {
            callPrimaryConstructorOf<DiagnosticFactory0DelegateProvider>(
                Severity.ERROR,
                SourceElementPositioningStrategies.DEFAULT,
                P::class
            )
        }
    }

    inline fun <reified P : PsiElement, A> error1(): DiagnosticFactory1DelegateProvider<A> {
        return try {
            context(container as KtDiagnosticsContainer) {
                org.jetbrains.kotlin.diagnostics.error1<P, A>()
            }
        } catch (_: Exception) {
            callPrimaryConstructorOf<DiagnosticFactory1DelegateProvider<A>>(
                Severity.ERROR,
                SourceElementPositioningStrategies.DEFAULT,
                P::class
            )
        }
    }

    inline fun <reified P : PsiElement, A, B> error2(): DiagnosticFactory2DelegateProvider<A, B> {
        return try {
            context(container as KtDiagnosticsContainer) {
                org.jetbrains.kotlin.diagnostics.error2<P, A, B>()
            }
        } catch (_: Exception) {
            callPrimaryConstructorOf<DiagnosticFactory2DelegateProvider<A, B>>(
                Severity.ERROR,
                SourceElementPositioningStrategies.DEFAULT,
                P::class
            )
        }
    }

    inline fun <reified P : PsiElement, A, B, C> error3(): DiagnosticFactory3DelegateProvider<A, B, C> {
        return try {
            context(container as KtDiagnosticsContainer) {
                org.jetbrains.kotlin.diagnostics.error3<P, A, B, C>()
            }
        } catch (_: Exception) {
            callPrimaryConstructorOf<DiagnosticFactory3DelegateProvider<A, B, C>>(
                Severity.ERROR,
                SourceElementPositioningStrategies.DEFAULT,
                P::class
            )
        }
    }
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T> callPrimaryConstructorOf(vararg args: Any?) = T::class
    .java
    .constructors
    .first()
    .newInstance(*args) as T


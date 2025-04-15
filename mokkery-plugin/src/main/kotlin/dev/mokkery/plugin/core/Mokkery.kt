package dev.mokkery.plugin.core

import dev.mokkery.plugin.ir.fqName
import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty

object Mokkery {

    val dev_mokkery by fqName
    val dev_mokkery_context by fqName
    val dev_mokkery_internal by fqName
    val dev_mokkery_internal_context by fqName
    val dev_mokkery_internal_calls by fqName
    val dev_mokkery_internal_interceptor by fqName
    val dev_mokkery_matcher by fqName

    object Class {

        val MockMode by dev_mokkery.klass
        val MokkeryScope by dev_mokkery.klass
        val MokkeryKind by dev_mokkery_internal_interceptor.klass
        val ArgMatchersScope by dev_mokkery_matcher.klass
        val MockMany2 by dev_mokkery.klass
        val MockMany3 by dev_mokkery.klass
        val MockMany4 by dev_mokkery.klass
        val MockMany5 by dev_mokkery.klass

        val MokkerySuiteScope by dev_mokkery.klass

        val MokkeryInstanceScope by dev_mokkery_internal.klass

        val CallArgument by dev_mokkery_context.klass
        val SuiteName by dev_mokkery_internal_context.klass

        val TemplatingScope by dev_mokkery_internal_calls.klass
        val GlobalMokkeryScope by dev_mokkery_internal.klass

        fun mockMany(value: Int): ClassResolver {
            return mockManyMap[value] ?: error("Unsupported types number! Expected value: in ${2..5}; Actual value: $value")
        }

        private val mockManyMap = mapOf(
            2 to MockMany2,
            3 to MockMany3,
            4 to MockMany4,
            5 to MockMany5,
        )
    }

    object Function {
        val internalEvery by dev_mokkery_internal.function
        val internalEverySuspend by dev_mokkery_internal.function
        val internalVerify by dev_mokkery_internal.function
        val internalVerifySuspend by dev_mokkery_internal.function
        val callIgnoringClassCastException by dev_mokkery_internal.function
        val TemplatingScope by dev_mokkery_internal_calls.function
        val MokkerySuiteScope by dev_mokkery.function
        val MokkeryInstanceScope by dev_mokkery_internal.function
        val createMokkeryInstanceContext by dev_mokkery_internal.function
        val initializeInJsFunctionMock by dev_mokkery_internal.function
        val typeArgumentAt by dev_mokkery_internal.function
        val autofillConstructor by dev_mokkery_internal.function
        val invokeInstantiationListener by dev_mokkery_internal_context.function
        val createMokkeryBlockingCallScope by dev_mokkery_internal_interceptor.function
        val createMokkerySuspendCallScope by dev_mokkery_internal_interceptor.function
    }

    object Property {

        val mockIdString by dev_mokkery_internal.property
        val spiedObject by dev_mokkery_internal.property
        val callInterceptor by dev_mokkery_internal_context.property
    }

    object Name {
        val mock by dev_mokkery.fqName
        val mockMany by dev_mokkery.fqName
        val spy by dev_mokkery.fqName
        val every by dev_mokkery.fqName
        val everySuspend by dev_mokkery.fqName
        val verify by dev_mokkery.fqName
        val verifySuspend by dev_mokkery.fqName
    }

    object Callable {
        val mock by dev_mokkery.callableId
        val mockMany by dev_mokkery.callableId
        val spy by dev_mokkery.callableId
        val verify by dev_mokkery.callableId
        val verifySuspend by dev_mokkery.callableId
        val every by dev_mokkery.callableId
        val everySuspend by dev_mokkery.callableId
    }

    object Errors {
        fun indirectCall(
            typeArgument: String,
            functionName: String
        ) = "''$typeArgument'' is a type parameter! Specific type expected for a ''$functionName'' call!"

        fun notLambdaExpression(
            functionName: String,
            param: String,
        ) = "Argument passed to ''$functionName'' for param ''$param'' must be a lambda expression!"

        fun sealedTypeCannotBeIntercepted(
            typeName: String,
            functionName: String
        ) = "Type ''$typeName'' is sealed and cannot be used with ''$functionName''!"

        fun finalTypeCannotBeIntercepted(
            typeName: String,
            functionName: String
        ) = "Type ''$typeName'' is final and cannot be used with ''$functionName''!"

        fun primitiveTypeCannotBeIntercepted(
            typeName: String,
            functionName: String
        ) = "Type ''$typeName'' is primitive and cannot be used with ''$functionName''!"

        fun finalMembersTypeCannotBeIntercepted(
            typeName: String,
            functionName: String,
            nonAbstractMembers: String,
        ) = "Type ''$typeName'' has final members and cannot be used with ''$functionName''! Final members: $nonAbstractMembers"

        fun noPublicConstructorTypeCannotBeIntercepted(
            typeName: String,
            functionName: String
        ) = "Type ''$typeName'' has no public constructor and cannot be used with ''$functionName''!"

        fun noDuplicatesForMockMany(
            typeName: String, 
            functionName: String,
            occurrences: String,
        ) = "Type ''$typeName'' for ''$functionName'' must occur only once, but it occurs $occurrences times!"
        
        fun singleSuperClass(
            functionName: String,
            superClasses: String
        ) = "Only one super class is acceptable for ''$functionName'' type! Detected super classes: $superClasses"
        
        fun functionalTypeNotAllowedOnJs(
            typeName: String,
            functionName: String
        ) = "Type ''$typeName'' is a functional type and it is not acceptable as an argument for ''$functionName'' on JS platform!"
    }

    val Origin = IrDeclarationOrigin.GeneratedByPlugin(Key)

    object Key : GeneratedDeclarationKey() {
        override fun toString(): String = "MokkeryPlugin"
    }
}

val FqName.callableId: PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, CallableId>>
    get() = PropertyDelegateProvider { _: Any?, property ->
        ReadOnlyProperty { _, _ ->
            CallableId(this, Name.identifier(property.name))
        }
    }

val FqName.klass: PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, ClassResolver>>
    get() = PropertyDelegateProvider { _: Any?, property ->
        val resolver = ClassById(ClassId(this, Name.identifier(property.name)))
        ReadOnlyProperty { _, _ -> resolver }
    }

val FqName.function: PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, FunctionResolver>>
    get() = PropertyDelegateProvider { _: Any?, property ->
        val resolver = FunctionById(CallableId(this, Name.identifier(property.name)))
        ReadOnlyProperty { _, _ -> resolver }
    }

val FqName.property: PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, PropertyResolver>>
    get() = PropertyDelegateProvider { _: Any?, property ->
        val resolver = PropertyById(CallableId(this, Name.identifier(property.name)))
        ReadOnlyProperty { _, _ -> resolver }
    }

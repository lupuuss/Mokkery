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
    val dev_mokkery_internal by fqName
    val dev_mokkery_internal_templating by fqName
    val dev_mokkery_matcher by fqName
    val dev_mokkery_internal_tracing by fqName
    val dev_mokkery_internal_dynamic by fqName

    object Class {

        val MockMode by dev_mokkery.klass
        val ArgMatchersScope by dev_mokkery_matcher.klass
        val ManyMocks2 by dev_mokkery.klass
        val ManyMocks3 by dev_mokkery.klass
        val ManyMocks4 by dev_mokkery.klass
        val ManyMocks5 by dev_mokkery.klass

        val MokkeryInterceptor by dev_mokkery_internal.klass
        val MokkeryInterceptorScope by dev_mokkery_internal.klass

        val MokkeryMockScope by dev_mokkery_internal.klass

        val MokkerySpy by dev_mokkery_internal.klass
        val MokkerySpyScope by dev_mokkery_internal.klass
        val CallContext by dev_mokkery_internal.klass

        val CallArg by dev_mokkery_internal_tracing.klass

        val TemplatingInterceptor by dev_mokkery_internal_templating.klass
        val TemplatingScope by dev_mokkery_internal_templating.klass
        val MokkeryScopeLookup by dev_mokkery_internal_dynamic.klass

        fun manyMocks(value: Int): ClassResolver {
            return manyMocksMap[value] ?: error("Unsupported types number! Expected value: in ${2..5}; Actual value: $value")
        }

        private val manyMocksMap = mapOf(
            2 to ManyMocks2,
            3 to ManyMocks3,
            4 to ManyMocks4,
            5 to ManyMocks5,
        )
    }

    object Function {
        val MokkeryMock by dev_mokkery_internal.function
        val MokkerySpy by dev_mokkery_internal.function

        val internalEvery by dev_mokkery_internal.function
        val internalEverySuspend by dev_mokkery_internal.function
        val internalVerify by dev_mokkery_internal.function
        val internalVerifySuspend by dev_mokkery_internal.function

        val TemplatingScope by dev_mokkery_internal_templating.function
        val MokkeryMockScope by dev_mokkery_internal.function
        val MokkerySpyScope by dev_mokkery_internal.function
        val generateMockId by dev_mokkery_internal.function
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

        fun noDefaultConstructorTypeCannotBeIntercepted(
            typeName: String,
            functionName: String
        ) = "Class ''$typeName'' has no default constructor and cannot be used with ''$functionName''!"

        fun noDuplicatesForMockMany(
            typeName: String, 
            occurrences: Int,
            functionName: String
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

package dev.mokkery.plugin.ext

import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty

val fqName: PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, FqName>>
    get() = PropertyDelegateProvider { _: Any?, property ->
        val name = FqName(property.name.replace("_", "."))
        ReadOnlyProperty { _, _ -> name }
    }


val FqName.functionId: PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, CallableId>>
    get() = PropertyDelegateProvider { _: Any?, property ->
        val id = CallableId(this, Name.identifier(property.name))
        ReadOnlyProperty { _, _ -> id }
    }
val FqName.fqName: PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, FqName>>
    get() = PropertyDelegateProvider { _: Any?, property ->
        val name = child(Name.identifier(property.name))
        ReadOnlyProperty { _, _ -> name }
    }

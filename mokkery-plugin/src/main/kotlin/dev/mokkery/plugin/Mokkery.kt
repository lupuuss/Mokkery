package dev.mokkery.plugin

import dev.mokkery.plugin.ir.fqName
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty

object Mokkery {

    val dev_mokkery by fqName
    val dev_mokkery_annotations by fqName
    val dev_mokkery_internal_annotations by fqName
    val dev_mokkery_templating by fqName
    val dev_mokkery_context by fqName
    val dev_mokkery_internal by fqName
    val dev_mokkery_internal_context by fqName
    val dev_mokkery_matcher by fqName
    val dev_mokkery_internal_templating by fqName
    val dev_mokkery_internal_matcher by fqName
    val dev_mokkery_internal_defaults by fqName

    object Name {
        val mock by dev_mokkery.fqName
        val mockMany by dev_mokkery.fqName
        val spy by dev_mokkery.fqName
        val every by dev_mokkery.fqName
        val everySuspend by dev_mokkery.fqName
        val verify by dev_mokkery.fqName
        val verifySuspend by dev_mokkery.fqName
        val ext by dev_mokkery_templating.fqName
        val ctx by dev_mokkery_templating.fqName
    }

    object Callable {
        val mock by dev_mokkery.callableId
        val mockMany by dev_mokkery.callableId
        val spy by dev_mokkery.callableId
        val verify by dev_mokkery.callableId
        val verifySuspend by dev_mokkery.callableId
        val every by dev_mokkery.callableId
        val everySuspend by dev_mokkery.callableId
        val ext by dev_mokkery_templating.callableId
        val ctx by dev_mokkery_templating.callableId
        val matches by dev_mokkery_matcher.callableId
        val matchesComposite by dev_mokkery_matcher.callableId
    }

    object ClassId {
        val MokkeryMatcherScope by dev_mokkery_matcher.classId
        val MokkeryTemplatingScope by dev_mokkery_templating.classId
        val Matcher by dev_mokkery_annotations.classId
        val ArgMatcher by dev_mokkery_matcher.classId
        val ArgMatcherComposite = ClassId(
            packageFqName = dev_mokkery_matcher,
            relativeClassName = FqName.fromSegments(listOf("ArgMatcher", "Composite")),
            isLocal = false
        )
        val Templating by dev_mokkery_internal_annotations.classId
    }
}

val FqName.callableId: PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, CallableId>>
    get() = PropertyDelegateProvider { _: Any?, property ->
        ReadOnlyProperty { _, _ ->
            CallableId(this, Name.identifier(property.name))
        }
    }

val FqName.classId: PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, ClassId>>
    get() = PropertyDelegateProvider { _: Any?, property ->
        ReadOnlyProperty { _, _ ->
            ClassId(this, Name.identifier(property.name))
        }
    }



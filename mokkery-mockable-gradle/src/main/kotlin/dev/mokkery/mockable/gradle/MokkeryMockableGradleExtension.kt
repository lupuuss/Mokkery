package dev.mokkery.mockable.gradle

import dev.mokkery.gradle.ApplicationRule
import dev.mokkery.mockable.internal.options.MokkeryMockableOptions
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

public abstract class MokkeryMockableGradleExtension @Inject constructor(
    objects: ObjectFactory,
) {

    internal val annotations: ListProperty<String> = objects.listProperty(String::class.java)

    /**
     * Annotations that are used by default. Dependency with default annotations is added automatically, according to the [rule].
     */
    public val defaultAnnotations: Array<String> = MokkeryMockableOptions.annotations.defaultValues.toTypedArray()

    /**
     * Determines source sets to be affected by Mokkery Mockable.
     *
     * By default, it is [dev.mokkery.gradle.ApplicationRule.Companion.All]
     */
    public val rule: Property<ApplicationRule> = objects.property(ApplicationRule::class.java)

    /**
     * Sets annotations that should be used to mark classes that should be made mockable.
     *
     * By default, it is [defaultAnnotations]
     */
    public fun annotations(vararg annotations: String) {
        this.annotations.set(annotations.asList())
    }
}

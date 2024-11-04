package dev.mokkery.test

interface GenericPropertiesInterface<Type> {

    var property: Type

    var <R> R.extProperty: Type

    var <R : Any> R?.extPropertyNullabilityMarkers: Type & Any

    var <R : Any> List<R?>.extPropertyNestedNullabilityMarkers: List<Type & Any>

    var <R : CharSequence> R.extPropertyBound: Type

    var <R : Comparable<R>> R.extPropertyBoundRecursiveParam: Type

    var <R : Type> R.extPropertyBoundParentParam: Type

    var <R : List<Type>> R.extPropertyBoundNestedParentParam: Type

    var <R> R.extPropertyMultipleBounds: Type where R : CharSequence, R : Comparable<R>

    var List<*>.extPropertyStarProjection: List<*>

    var <T : GenericPropertiesInterface<T>> T.extPropertySelf: GenericPropertiesInterface<Type>
}

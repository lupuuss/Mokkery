package dev.mokkery.test

@OpenForMokkery
data class DataClass(
    val primitiveField: Int,
    val complexField1: ComplexType,
    val complexField2: ComplexType
)

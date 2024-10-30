package dev.mokkery.test

import dev.mokkery.test.ComplexType

interface PropertiesInterface {

    var primitiveProperty: Int
    var complexProperty: ComplexType

    var Int.primitivePropertyExtension: Int
    var ComplexType.complexPropertyExtension: ComplexType
}

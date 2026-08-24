package dev.mokkery.internal.contracts

import dev.mokkery.context.Function

@PublishedApi
internal interface MemberFunctionsContract : InstanceContract {

    fun mokkeryNormalizeId(id: Long): Long = id

    fun mokkeryFunction(id: Long): Function?
}

package dev.mokkery.factory.configurer

import dev.mokkery.configurer.MokkeryConfigurer

public interface InstanceFactoryConfigurer : MokkeryConfigurer {

    public typealias Block = InstanceFactoryConfigurer.() -> Unit
}

public interface SpyFactoryConfigurer : InstanceFactoryConfigurer {

    public typealias Block = SpyFactoryConfigurer.() -> Unit
}


public interface MockFactoryConfigurer : InstanceFactoryConfigurer {

    public typealias Block = MockFactoryConfigurer.() -> Unit
}

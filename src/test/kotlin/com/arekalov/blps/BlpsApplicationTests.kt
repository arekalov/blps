package com.arekalov.blps

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles(value = ["test"], inheritProfiles = false)
class BlpsApplicationTests {

    @Test
    fun contextLoads() = Unit
}

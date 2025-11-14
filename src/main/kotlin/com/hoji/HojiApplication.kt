package com.hoji

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class HojiApplication

fun main(args: Array<String>) {
    runApplication<HojiApplication>(*args)
}

package com.jwtstarter

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class JwtStarterApplication

fun main(args: Array<String>) {
    runApplication<JwtStarterApplication>(*args)
}

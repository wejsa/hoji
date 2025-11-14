package com.hoji

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HojiApplication

fun main(args: Array<String>) {
    runApplication<HojiApplication>(*args)
}

package com.example.messing

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class MessingApplication

fun main(args: Array<String>) {
    runApplication<MessingApplication>(*args)
}

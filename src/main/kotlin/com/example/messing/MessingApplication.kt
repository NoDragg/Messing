package com.example.messing

import com.example.messing.config.LiveKitProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan
class MessingApplication

fun main(args: Array<String>) {
    runApplication<MessingApplication>(*args)
}

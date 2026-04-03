package com.example.messing.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.file.Path
import java.nio.file.Paths

@Configuration
class StaticResourceConfig(
    @Value("\${app.upload-dir:uploads}") uploadDir: String
) : WebMvcConfigurer {

    private val uploadPath: Path = Paths.get(uploadDir).toAbsolutePath().normalize()

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/media/**")
            .addResourceLocations(uploadPath.toUri().toString())
    }
}

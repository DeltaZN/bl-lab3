package ru.itmo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer

@SpringBootApplication
open class Lab3ClientApp : SpringBootServletInitializer() {
    override fun configure(builder: SpringApplicationBuilder): SpringApplicationBuilder {
        return builder.sources(Lab3ClientApp::class.java)
    }
}

fun main(args: Array<String>) {
    runApplication<Lab3ClientApp>(*args)
}
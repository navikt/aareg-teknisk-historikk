package no.nav.aareg.teknisk_historikk

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestTemplate

@SpringBootApplication
open class AaregTekniskHistorikk {
    @Bean
    open fun restTemplate(restTemplateBuilder: RestTemplateBuilder, @Value("\${app.name}") appName: String): RestTemplate {
        return restTemplateBuilder
            .defaultHeader("Nav-Call-Id",appName)
            .build()
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(AaregTekniskHistorikk::class.java, *args)
}
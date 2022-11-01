package no.nav.aareg.teknisk_historikk

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestTemplate

@SpringBootApplication
open class AaregTekniskHistorikk {
    @Bean
    open fun restTemplate(restTemplateBuilder: RestTemplateBuilder): RestTemplate {
        return restTemplateBuilder.build()
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(AaregTekniskHistorikk::class.java, *args)
}
package no.nav.aareg.teknisk.historikk;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import static org.springframework.boot.Banner.Mode.OFF;

@SpringBootApplication
@ConfigurationPropertiesScan
public class Main extends SpringBootServletInitializer {

    static void main(String[] args) {
        new SpringApplicationBuilder()
                .bannerMode(OFF)
                .sources(Main.class)
                .registerShutdownHook(true)
                .run(args);
    }
}

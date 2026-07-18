package com.dogukantez.soaptask.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "vies")
public class ViesProperties {

    @NotBlank
    private String endpoint;

    @NotBlank
    private String wsdl;

    private Timeout timeout = new Timeout();

    @Getter
    @Setter
    public static class Timeout {
        private Duration connect = Duration.ofSeconds(5);
        private Duration read = Duration.ofSeconds(15);
    }
}

package com.dogukantez.soaptask.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "vies")
public class ViesProperties {

    private String endpoint;
    private String wsdl;

    private Timeout timeout = new Timeout();

    @Getter
    @Setter
    public static class Timeout {
        private int connect;
        private int read;
    }
}

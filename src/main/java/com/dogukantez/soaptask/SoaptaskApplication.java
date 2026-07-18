package com.dogukantez.soaptask;

import com.dogukantez.soaptask.config.ViesProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ViesProperties.class)
public class SoaptaskApplication {

	public static void main(String[] args) {
		SpringApplication.run(SoaptaskApplication.class, args);
	}

}

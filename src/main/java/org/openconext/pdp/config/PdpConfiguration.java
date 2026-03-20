package org.openconext.pdp.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(PdpMongoProperties.class)
public class PdpConfiguration {
}

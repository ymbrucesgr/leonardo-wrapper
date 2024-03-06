package org.generationai.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@AllArgsConstructor
@ConfigurationProperties(prefix = "leonardo-ai")
public class LeonardoAiConfig {
    private String apiKey;
}

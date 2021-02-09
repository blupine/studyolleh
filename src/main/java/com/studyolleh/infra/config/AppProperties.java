package com.studyolleh.infra.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("app") // application.properties에서 app.* 을 읽어
public class AppProperties {
    private String host;
}

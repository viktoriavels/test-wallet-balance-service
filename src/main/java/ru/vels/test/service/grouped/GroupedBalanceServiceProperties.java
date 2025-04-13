package ru.vels.test.service.grouped;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.grouped")
public class GroupedBalanceServiceProperties {
    private int threadsCount = 4;
}

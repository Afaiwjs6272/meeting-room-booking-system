package ru.company.booking.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableJpaAuditing
@EnableScheduling
@EnableFeignClients(basePackages = "ru.company.booking.client")
public class InfrastructureConfig {
}

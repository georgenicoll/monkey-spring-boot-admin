package com.monkeynuthead.spring.boot.admin.new_client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@SpringBootApplication
public class NewClientApplication {

    public static void main(final String[] args) {
        SpringApplication.run(NewClientApplication.class, args);
    }

    @Bean
    SecurityWebFilterChain securityWebFilterChain(final ServerHttpSecurity http) {
        return http
                .authorizeExchange().anyExchange().permitAll()
                .and().csrf().disable()
                .build();
    }

}

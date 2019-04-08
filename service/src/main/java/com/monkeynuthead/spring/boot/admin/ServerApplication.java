package com.monkeynuthead.spring.boot.admin;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@SpringBootApplication
@EnableAdminServer
public class ServerApplication {

    public static void main(final String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    @Bean
    SecurityWebFilterChain securityWebFilterChain(final ServerHttpSecurity http) {
        return http
                .authorizeExchange().anyExchange().permitAll()
                .and().csrf().disable()
                .build();
    }

    //private static final URI INSTANCES = URI.create("http://localhost:9090/instances");

//    @Bean
//    RouterFunction<ServerResponse> redirectRootToInstances() {
//        return route(path("/api/applications"), req ->
//                ServerResponse.temporaryRedirect(INSTANCES).build());
//    }

}

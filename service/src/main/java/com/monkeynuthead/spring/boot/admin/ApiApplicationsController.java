package com.monkeynuthead.spring.boot.admin;

import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

@RestController
public class ApiApplicationsController {

    private static final Logger log = LoggerFactory.getLogger(ServerApplication.class);

    private static final URI INSTANCES = URI.create("http://localhost:9090/instances");
    private static final String DELETE = "http://localhost:9090/instances/{id}";

    private final WebClient webClient = WebClient.create();

    /**
     * This apes what de.codecentric.boot.admin.server.web.InstancesController#register does
     * delegating to the new instances method but ensuring that a created is returned.
     */
    @PostMapping(path = "/api/applications", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Map<String, Object>>> register(final @RequestBody Registration registration,
                                                              final UriComponentsBuilder builder) {

        log.debug("Got POST request on /api/applications: {}", registration);

        final Mono<Registration> registrationMono = Mono.just(registration);

        return webClient.post()
                .uri(INSTANCES)
                .contentType(MediaType.APPLICATION_JSON)
                .body(registrationMono, Registration.class)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .doOnNext(response -> log.debug("Response: {}", response))
                .map(response -> {
                    final Object id = response.get("id");
                    final URI location = builder.replacePath("/instances/{id}").buildAndExpand(id).toUri();
                    return ResponseEntity.created(location).body(response);
                });

    }

    /**
     * This apes de.codecentric.boot.admin.server.web.InstancesController#unregister(java.lang.String)
     */
    @DeleteMapping(path = "/api/applications/{id}")
    public Mono<ResponseEntity<Void>> unregister(@PathVariable String id) {
        log.info("Got DELETE request for id '{}'", id);

        return webClient.delete()
                .uri(DELETE, id)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnNext(v -> log.info("Got the response for '{}'", id))
                .map(v -> ResponseEntity.noContent().build());
    }


}

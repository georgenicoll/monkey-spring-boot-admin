package com.monkeynuthead.spring.boot.admin;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import de.codecentric.boot.admin.server.web.client.HttpHeadersProvider;
import de.codecentric.boot.admin.server.web.client.InstanceExchangeFilterFunction;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SimpleTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.ConnectionObserver;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.*;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
class AdminServiceHttpConfiguration {

    private final AdminServerProperties adminServerProperties;

    AdminServiceHttpConfiguration(final AdminServerProperties adminServerProperties) {
        this.adminServerProperties = adminServerProperties;
    }

    /**
     * Following copied from {@link de.codecentric.boot.admin.server.config.AdminServerAutoConfiguration}
     * <p>
     * NB:  Really the certificates/trust stores should be setup correctly.
     */

    @Bean
    InstanceWebClient instanceWebClient(final HttpHeadersProvider httpHeadersProvider,
                                        final ObjectProvider<List<InstanceExchangeFilterFunction>> filtersProvider) {
        List<InstanceExchangeFilterFunction> additionalFilters = filtersProvider.getIfAvailable(Collections::emptyList);
        return InstanceWebClient.builder()
                //Added setting the webclient...
                .webClient(createDefaultWebClient(
                        this.adminServerProperties.getMonitor().getConnectTimeout(),
                        this.adminServerProperties.getMonitor().getReadTimeout()
                ).build())
                /* Following lines not needed - set on default web client
                .connectTimeout(this.adminServerProperties.getMonitor().getConnectTimeout())
                .readTimeout(this.adminServerProperties.getMonitor().getReadTimeout())
                 */
                .defaultRetries(this.adminServerProperties.getMonitor().getDefaultRetries())
                .retries(this.adminServerProperties.getMonitor().getRetries())
                .httpHeadersProvider(httpHeadersProvider)
                .filters(filters -> filters.addAll(additionalFilters))
                .build();
    }

    /**
     * Shamelessly copied from
     * {@link de.codecentric.boot.admin.server.web.client.InstanceWebClient.Builder#createDefaultWebClient(
     * java.time.Duration, java.time.Duration)}
     * with the addition of configuring the http client for unquestioning trust
     */
    private static WebClient.Builder createDefaultWebClient(Duration connectTimeout, Duration readTimeout) {
        HttpClient httpClient = HttpClient.create()
                .compress(true)
                //Following line added
                .secure(sslContextSpec -> sslContextSpec.sslContext(unquestioningTrustSslContext()))
                //Previous line added
                .tcpConfiguration(tcp -> tcp.bootstrap(bootstrap -> bootstrap.option(
                        ChannelOption.CONNECT_TIMEOUT_MILLIS,
                        (int) connectTimeout.toMillis()
                )).observe((connection, newState) -> {
                    if (ConnectionObserver.State.CONNECTED.equals(newState)) {
                        connection.addHandlerLast(new ReadTimeoutHandler(readTimeout.toMillis(),
                                TimeUnit.MILLISECONDS
                        ));
                    }
                }));
        ReactorClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);
        return WebClient.builder().clientConnector(connector);
    }


    private static final TrustManager[] UNQUESTIONING_TRUST_MANAGER = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
                    //No op
                }

                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
                    //No op
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            }
    };

    private static TrustManagerFactory unquestioningTrustManagerFactory() {
        return new SimpleTrustManagerFactory() {
            @Override
            protected void engineInit(KeyStore keyStore) {
                //Ignore
            }

            @Override
            protected void engineInit(ManagerFactoryParameters managerFactoryParameters) {
                //Ignore
            }

            @Override
            protected TrustManager[] engineGetTrustManagers() {
                return UNQUESTIONING_TRUST_MANAGER;
            }
        };
    }

    private static SslContext unquestioningTrustSslContext() {
        try {
            return SslContextBuilder.forClient().trustManager(unquestioningTrustManagerFactory()).build();
        } catch (final SSLException e) {
            throw new RuntimeException(e);
        }
    }

}

package com.hazse.mcp.boardgame.app.stdio.config;

import com.hazse.mcp.boardgame.app.stdio.apikey.HashedApiKey;
import com.hazse.mcp.boardgame.app.stdio.apikey.HashedApiKeyUtils;
import com.hazse.mcp.boardgame.app.stdio.config.props.ApiKeyConfigurationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.security.server.apikey.ApiKeyEntityRepository;
import org.springaicommunity.mcp.security.server.apikey.memory.InMemoryApiKeyEntityRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;

import static org.springaicommunity.mcp.security.server.config.McpApiKeyConfigurer.mcpServerApiKey;

@Configuration
@EnableConfigurationProperties(ApiKeyConfigurationProperties.class)
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class ApiKeyConfiguration {
    private final ApiKeyConfigurationProperties properties;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        if (properties.isEnabled()) {
            return createApiKeyFilterChain(http);
        }
        else {
            return createNotSecuredFilterChain(http);
        }
    }

    private DefaultSecurityFilterChain createNotSecuredFilterChain(HttpSecurity http) throws Exception {
        log.warn("MCP calls will not be secured");

        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz.anyRequest().permitAll())
                .build();
    }

    private DefaultSecurityFilterChain createApiKeyFilterChain(HttpSecurity http) throws Exception {
        log.info("MCP calls will be secured with API keys");

        return http.authorizeHttpRequests(authz -> authz.anyRequest().authenticated())
                .with(
                        mcpServerApiKey(),
                        configurer -> configurer.apiKeyRepository(apiKeyRepository())
                )
                .build();
    }

    private ApiKeyEntityRepository<HashedApiKey> apiKeyRepository() {
        Collection<HashedApiKey> apiKeys = HashedApiKeyUtils.readFrom(properties.getKeys());
        log.info("Loaded {} API key(s)", apiKeys.size());

        // Create some API keys
        // HashedApiKeyUtils.generateRandomApiKey();

        return new InMemoryApiKeyEntityRepository<>(apiKeys);
    }
}

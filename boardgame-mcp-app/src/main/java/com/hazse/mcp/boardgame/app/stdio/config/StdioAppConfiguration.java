package com.hazse.mcp.boardgame.app.stdio.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazse.mcp.boardgame.app.stdio.config.props.StdioAppConfigurationProperties;
import com.hazse.mcp.boardgame.app.stdio.provider.BoardGameResourceProvider;
import com.hazse.mcp.boardgame.app.stdio.provider.BoardGameToolProvider;
import com.hazse.mcp.boardgame.client.bgg.AuduxBggClient;
import com.hazse.mcp.boardgame.client.core.BoardGameInformationClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(StdioAppConfigurationProperties.class)
public class StdioAppConfiguration {
    @Bean
    public BoardGameInformationClient auduxBggClient(StdioAppConfigurationProperties properties) {
        return new AuduxBggClient(properties.getAuthenticationToken());
    }

    @Bean
    BoardGameToolProvider boardGameToolProvider(BoardGameInformationClient bggClient) {
        return new BoardGameToolProvider(bggClient);
    }

    @Bean
    BoardGameResourceProvider boardGameResourceProvider(BoardGameInformationClient bggClient, ObjectMapper objectMapper) {
        return new BoardGameResourceProvider(bggClient, objectMapper);
    }
}

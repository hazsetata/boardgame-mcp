package com.hazse.mcp.boardgame.app.stdio.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazse.mcp.boardgame.app.stdio.provider.BoardGameResourceProvider;
import com.hazse.mcp.boardgame.app.stdio.provider.BoardGameToolProvider;
import com.hazse.mcp.boardgame.client.bgg.AuduxBggClient;
import com.hazse.mcp.boardgame.client.core.BoardGameInformationClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StdioAppConfiguration {
    @Bean
    public BoardGameInformationClient auduxBggClient() {
        return new AuduxBggClient();
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

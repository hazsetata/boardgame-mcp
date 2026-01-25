package com.hazse.mcp.boardgame.app.stdio.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazse.mcp.boardgame.client.core.BoardGame;
import com.hazse.mcp.boardgame.client.core.BoardGameInformationClient;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import org.springaicommunity.mcp.annotation.McpResource;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings("unused")
@RequiredArgsConstructor
public class BoardGameResourceProvider {
    public static final String BOARD_GAME_DETAILS_URI = "boardgame://details/";

    private final BoardGameInformationClient bggClient;
    private final ObjectMapper objectMapper;

    @McpResource(
            uri = BOARD_GAME_DETAILS_URI + "{boardGameId}",
            name = "boardGameDetailsResource",
            description = "Provides detailed information about board games based on their id",
            mimeType = "application/json"
    )
    public McpSchema.ReadResourceResult getBoardGameDetails(String boardGameId) {
        int intBoardGameId = Integer.parseInt(boardGameId);

        return bggClient.getGameDetailsByIds(Set.of(intBoardGameId))
                .stream()
                .findFirst()
                .map(game -> convertToResult(game, intBoardGameId))
                .orElseGet(() -> new McpSchema.ReadResourceResult(List.of()));
    }

    private McpSchema.ReadResourceResult convertToResult(BoardGame boardGame, int boardGameId) {
        try {
            return new McpSchema.ReadResourceResult(List.of(
                    new McpSchema.TextResourceContents(
                            BOARD_GAME_DETAILS_URI + boardGameId,
                            "application/json",
                            objectMapper.writeValueAsString(boardGame)
                    )
            ));
        }
        catch (JsonProcessingException e) {
            return new McpSchema.ReadResourceResult(List.of());
        }
    }
}

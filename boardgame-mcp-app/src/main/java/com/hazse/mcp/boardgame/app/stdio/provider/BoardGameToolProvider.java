package com.hazse.mcp.boardgame.app.stdio.provider;

import com.hazse.mcp.boardgame.client.core.BoardGame;
import com.hazse.mcp.boardgame.client.core.BoardGameInformationClient;
import com.hazse.mcp.boardgame.client.core.BoardGameSearchResult;
import com.hazse.mcp.boardgame.client.core.BoardGameSearchResultList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@RequiredArgsConstructor
@Slf4j
public class BoardGameToolProvider {
    private final BoardGameInformationClient bggClient;

    @McpTool(
            name = "searchGames",
            description = "Search for a list of boardgames by the provided full or partial name",
            generateOutputSchema = true
    )
    public BoardGameSearchResultList getBoardGamesWithName(
            @McpToolParam(description =  "The full or partial name of the games to look for", required = true)
            String name
    ) {
        return BoardGameSearchResultList.builder()
                .games(bggClient.searchGamesByName(name))
                .build();
    }

    @McpTool(
            name = "getGameDetails",
            description = "Provides details about a game by its id",
            generateOutputSchema = true
    )
    public BoardGame getBoardGameDetails(
            @McpToolParam(description =  "The id of the game", required = true)
            String id
    ) {
        int intBoardGameId = Integer.parseInt(id);

        List<BoardGame> gameDetailsByIds = bggClient.getGameDetailsByIds(Set.of(intBoardGameId));

        log.info("Found {} game details", gameDetailsByIds.size());

        return gameDetailsByIds
                .stream()
                .findFirst()
                .orElse(null);
    }


    private String convertToText(BoardGameSearchResult bggGame) {
        return String.format(""" 
                Name: %s
                ID: %d
                Publication year: %s
                Url: https://boardgamegeek.com/boardgame/%d
                """,
                bggGame.getName(),
                bggGame.getId(),
                bggGame.getPublicationYear() == null ? "Unknown" : String.valueOf(bggGame.getPublicationYear()),
                bggGame.getId()
        );
    }
}

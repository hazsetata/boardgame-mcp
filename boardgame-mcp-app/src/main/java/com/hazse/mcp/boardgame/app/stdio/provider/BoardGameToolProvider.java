package com.hazse.mcp.boardgame.app.stdio.provider;

import com.hazse.mcp.boardgame.client.core.BoardGame;
import com.hazse.mcp.boardgame.client.core.BoardGameInformationClient;
import com.hazse.mcp.boardgame.client.core.BoardGameSearchResultList;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springaicommunity.mcp.context.McpSyncRequestContext;
import org.springaicommunity.mcp.context.StructuredElicitResult;

import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
@RequiredArgsConstructor
@Slf4j
public class BoardGameToolProvider {
    private final BoardGameInformationClient bggClient;

    @McpTool(
            name = "searchBoardGames",
            description = "Search for a list of board games by the provided full or partial name",
            generateOutputSchema = true
    )
    public BoardGameSearchResultList getBoardGamesWithName(
            @McpToolParam(description =  "The full or partial name of the board games to look for", required = true)
            String name
    ) {
        return BoardGameSearchResultList.builder()
                .games(bggClient.searchGamesByName(name))
                .build();
    }

    @McpTool(
            name = "getBoardGameDetails",
            description = "Provides details about a board game by its id",
            generateOutputSchema = true
    )
    @SneakyThrows
    public BoardGame getBoardGameDetails(
            @McpToolParam(description =  "The id of the board game", required = true)
            String id,
            McpSyncRequestContext context
    ) {
        int intBoardGameId = Integer.parseInt(id);

        context.progress(progress -> progress.percentage(10).message("Fetching game details..."));
        Thread.sleep(10000);

        List<BoardGame> gameDetailsByIds = bggClient.getGameDetailsByIds(Set.of(intBoardGameId));
        context.progress(progress -> progress.percentage(50).message("Organizing gaming session..."));
        Thread.sleep(10000);


        log.info("Found {} game details", gameDetailsByIds.size());
        context.progress(progress -> progress.percentage(100).message("Completed"));
        context.info("Game details fetching completed");

        return gameDetailsByIds
                .stream()
                .findFirst()
                .orElse(null);
    }

    record GameRating(Number rating) {}

    @McpTool(
            name = "rateBoardGame",
            description = "Saves the personal rating of the user for the given board game",
            generateOutputSchema = true
    )
    @SneakyThrows
    public BoardGame rateBoardGame(
            @McpToolParam(description =  "The id of the board game", required = true)
            String id,
            McpSyncRequestContext context
    ) {
        int intBoardGameId = Integer.parseInt(id);

        context.progress(progress -> progress.percentage(10).message("Fetching game details..."));
        Thread.sleep(3000);

        List<BoardGame> gameDetailsByIds = bggClient.getGameDetailsByIds(Set.of(intBoardGameId));

        if (gameDetailsByIds.isEmpty()) {
            context.error("Board game with id %s not found".formatted(id));
            return null;
        }
        else {
            BoardGame gameDetails = bggClient.getGameDetailsByIds(Set.of(intBoardGameId)).getFirst();
            context.progress(progress -> progress.percentage(50).message("Asking user's rating..."));
            StructuredElicitResult<GameRating> userRating = context.elicit(
                    elicit -> elicit.message("Your rating for %s".formatted(gameDetails.getName())),
                    GameRating.class
            );
            context.progress(progress -> progress.percentage(80).message("Rating received..."));

            if (userRating.structuredContent() == null) {
                context.error("User cancelled rating");
                context.progress(progress -> progress.percentage(100).message("Cancelled"));
            }
            else {
                context.info("Game rating saved: %s".formatted(userRating.structuredContent().rating()));
                context.progress(progress -> progress.percentage(100).message("Game rating saved: %s".formatted(userRating.structuredContent().rating())));
            }

            return gameDetails;
        }
    }
}

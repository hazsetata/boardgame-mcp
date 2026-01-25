package com.hazse.mcp.boardgame.client.bgg;

import com.hazse.mcp.boardgame.client.core.BoardGame;
import com.hazse.mcp.boardgame.client.core.BoardGameInformationClient;
import com.hazse.mcp.boardgame.client.core.BoardGameSearchResult;
import com.hazse.mcp.boardgame.client.core.BoardGameType;
import lombok.extern.slf4j.Slf4j;
import org.audux.bgg.common.ThingType;
import org.audux.bgg.response.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Slf4j
public class AuduxBggClient implements BoardGameInformationClient {
    protected static final Set<ThingType> ALLOWED_THING_TYPES_SET = Set.of(
            ThingType.BOARD_GAME,
            ThingType.BOARD_GAME_EXPANSION
    );
    protected static final ThingType[] ALLOWED_THING_TYPES = ALLOWED_THING_TYPES_SET.toArray(new ThingType[0]);
    public static final String BGG_GAME_URL = "https://boardgamegeek.com/boardgame/%d";

    public AuduxBggClient(String authToken) {
        org.audux.bgg.BggClient.configure(config -> {
            config.setAuthToken(authToken);
            return null;
        });
    }

    @SuppressWarnings("java:S2142")
    @Override
    public List<BoardGameSearchResult> searchGamesByName(String name) {
        try {
            Future<Response<SearchResults>> searchFuture = org.audux.bgg.BggClient
                    .search(name, new ThingType[]{ThingType.BOARD_GAME}, false)
                    .callAsync();
            Response<SearchResults> searchResponse = searchFuture.get();

            if (searchResponse.isSuccess()) {
                SearchResults searchResults = searchResponse.getData();

                return searchResults.getResults()
                        .stream()
                        .filter(result -> ALLOWED_THING_TYPES_SET.contains(result.getType()))
                        .map(this::convertToBggGameSearchResult)
                        .toList();
            }
        }
        catch (Exception e) {
            // We do nothing with these, just return an empty list below
            log.error("Error searching for games: '{}'", name, e);
        }

        return List.of();
    }

    @SuppressWarnings("java:S2142")
    @Override
    public List<BoardGame> getGameDetailsByIds(Set<Integer> ids) {
        try {
            Response<Things> fetchResponse = org.audux.bgg.BggClient
                    .things(
                            ids.toArray(new Integer[0]),
                            ALLOWED_THING_TYPES
                    )
                    .callAsync()
                    .get();
            if (fetchResponse.isSuccess()) {
                Things fetchResults = fetchResponse.getData();

                return fetchResults.getThings()
                        .stream()
                        .map(this::convertToBggGame)
                        .toList();
            }
        }
        catch (Exception e) {
            // We do nothing with these, just return an empty list below
            log.error("Error fetching game details for: {}", ids, e);
        }

        return List.of();
    }

    private BoardGameSearchResult convertToBggGameSearchResult(SearchResult result) {
        return BoardGameSearchResult.builder()
                .id(result.getId())
                .name(result.getName().getValue())
                .url(BGG_GAME_URL.formatted(result.getId()))
                .publicationYear(result.getYearPublished())
                .build();
    }

    private BoardGame convertToBggGame(Thing thing) {
        BoardGame.BoardGameBuilder retValue = BoardGame.builder()
                .id(thing.getId())
                .type(convertToBoardGameType(thing.getType()))
                .name(thing.getName())
                .url(BGG_GAME_URL.formatted(thing.getId()));

        if (thing.getDescription() != null) {
            retValue.description(thing.getDescription());
        }
        if (thing.getYearPublished() != null) {
            retValue.publicationYear(thing.getYearPublished());
        }
        if (thing.getThumbnail() != null) {
            retValue.thumbnailUrl(thing.getThumbnail());
        }
        if (thing.getImage() != null) {
            retValue.imageUrl(thing.getImage());
        }
        if (thing.getMinAge() != null) {
            retValue.minPlayerAge(thing.getMinAge());
        }
        if (thing.getMinPlayers() != null) {
            retValue.minPlayers(thing.getMinPlayers());
        }
        if (thing.getMaxPlayers() != null) {
            retValue.maxPlayers(thing.getMaxPlayers().intValue());
        }

        return retValue.build();
    }

    private BoardGameType convertToBoardGameType(ThingType thingType) {
        if (thingType != null) {
            return switch (thingType) {
                case BOARD_GAME -> BoardGameType.BOARD_GAME;
                case BOARD_GAME_EXPANSION -> BoardGameType.EXPANSION;
                default -> BoardGameType.UNKNOWN;
            };
        }
        else {
            return BoardGameType.UNKNOWN;
        }
    }
}

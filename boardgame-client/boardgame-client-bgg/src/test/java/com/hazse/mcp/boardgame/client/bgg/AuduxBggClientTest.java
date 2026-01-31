package com.hazse.mcp.boardgame.client.bgg;

import com.hazse.mcp.boardgame.client.core.BoardGameSearchResult;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AuduxBggClientTest {
    @Disabled("This test requires a working authentication token.")
    @Test
    void whenSearchingForExistingGame_thenSuccess() {
        AuduxBggClient client = new AuduxBggClient("testtoken");
        List<BoardGameSearchResult> results = client.searchGamesByName("dixit");

        assertThat(results).isNotNull().isNotEmpty();
    }
}
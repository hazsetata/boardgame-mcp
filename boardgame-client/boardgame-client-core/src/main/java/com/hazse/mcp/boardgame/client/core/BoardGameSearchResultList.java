package com.hazse.mcp.boardgame.client.core;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@Builder
@Jacksonized
public class BoardGameSearchResultList {
    private List<BoardGameSearchResult> games;
}

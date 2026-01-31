package com.hazse.mcp.boardgame.client.core;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class BoardGame {
    private int id;
    private BoardGameType type;
    private String name;
    private String url;
    private String description;
    private Integer publicationYear;
    private String thumbnailUrl;
    private String imageUrl;
    private Integer minPlayerAge;
    private Integer minPlayers;
    private Integer maxPlayers;
}

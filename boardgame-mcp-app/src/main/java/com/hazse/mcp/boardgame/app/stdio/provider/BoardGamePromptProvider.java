package com.hazse.mcp.boardgame.app.stdio.provider;

import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpArg;
import org.springaicommunity.mcp.annotation.McpPrompt;

import java.util.Calendar;
import java.util.List;

@Slf4j
public class BoardGamePromptProvider {
    private static final String PROMPT_FOR_LATEST = "Search for board games named \"%s\". If any games are found, fetch the details for the ones where the publication year is %d or %d. DO NOT fetch details for any other games.";
    private static final String PROMPT_FOR_SPECIFIC = "Search for board games named \"%s\". If any games are found, fetch the details for the ones where the publication year is %d. DO NOT fetch details for any other games.";

    @McpPrompt(
            name = "lookupBoardGameDetails",
            description = "Prompt to search board games"
    )
    public McpSchema.GetPromptResult getBoardGameSearchPrompt(
            @McpArg(description =  "The full or partial name of the board games to look for", required = true)
            String name,
            @McpArg(description =  "The release year of the board games to look for", required = false)
            Integer releaseYear
    ) {
        if ((releaseYear != null) && (releaseYear > 0)) {
            return new McpSchema.GetPromptResult(
                    "Board Game Search",
                    List.of(new McpSchema.PromptMessage(
                            McpSchema.Role.USER,
                            new McpSchema.TextContent(PROMPT_FOR_SPECIFIC.formatted(name, releaseYear))
                    ))
            );
        }
        else {
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);

            return new McpSchema.GetPromptResult(
                    "Board Game Search",
                    List.of(new McpSchema.PromptMessage(
                            McpSchema.Role.USER,
                            new McpSchema.TextContent(PROMPT_FOR_LATEST.formatted(name, currentYear - 1, currentYear))
                    ))
            );
        }
    }
}

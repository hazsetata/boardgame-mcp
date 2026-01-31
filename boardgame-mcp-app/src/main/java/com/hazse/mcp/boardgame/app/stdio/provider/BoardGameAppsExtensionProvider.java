package com.hazse.mcp.boardgame.app.stdio.provider;

import com.hazse.mcp.boardgame.app.stdio.meta.McpToolMeta;
import com.hazse.mcp.boardgame.app.stdio.utils.ImageDownloader;
import com.hazse.mcp.boardgame.app.stdio.utils.ImageDownloaderException;
import com.hazse.mcp.boardgame.app.stdio.utils.ResourceUtils;
import com.hazse.mcp.boardgame.client.core.BoardGame;
import com.hazse.mcp.boardgame.client.core.BoardGameInformationClient;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpResource;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Slf4j
public class BoardGameAppsExtensionProvider {
    public static final String UI_BOARD_GAME_DISPLAY = "ui://board-game/display.html";

    private final BoardGameInformationClient bggClient;
    private final ImageDownloader imageDownloader;

    @McpTool(
            name = "getBoardGameCoverImage",
            description = "Gets the cover image of a board game by its id"
    )
    @McpToolMeta(
            metadata = "ext_apps_ui=" + UI_BOARD_GAME_DISPLAY
    )
    public McpSchema.CallToolResult getBoardGameCover(
            @McpToolParam(description = "The id of the board game", required = true)
            String id
    ) {
        int intBoardGameId = Integer.parseInt(id);
        List<BoardGame> gameDetailsByIds = bggClient.getGameDetailsByIds(Set.of(intBoardGameId));

        if (!gameDetailsByIds.isEmpty() && gameDetailsByIds.getFirst().getImageUrl() != null) {
            try {
                String coverImage = imageDownloader.downloadAsBase64Png(gameDetailsByIds.getFirst().getImageUrl());
                if (coverImage != null) {
                    return McpSchema.CallToolResult.builder()
                            .addContent(new McpSchema.ImageContent(null, coverImage, "image/png"))
                            .isError(false)
                            .build();
                }
            }
            catch (ImageDownloaderException e) {
                log.error("Error downloading cover image: {}", e.getMessage(), e);
            }
        }

        return McpSchema.CallToolResult.builder()
                .addTextContent("Cover image not available / couldn't download or convert cover image.")
                .isError(true)
                .build();
    }

    @McpResource(
            uri = UI_BOARD_GAME_DISPLAY,
            name = "boardGameDisplayResource",
            mimeType = "text/html;profile=mcp-app"
    )
    public McpSchema.ReadResourceResult getBoardGameDisplayResource() {
        String displayHtml = ResourceUtils.readHtmlResource("boardgame-display.html");

        return wrapUiHtmlcontent(UI_BOARD_GAME_DISPLAY, displayHtml);
    }

    public record GamerGreeting(String greetingMessage) {}

    @McpTool(
            name = "greetBoardGamers",
            description = "Sends a greeting message for board gamers",
            generateOutputSchema = true
    )
    public GamerGreeting sendBoardGamerGreeting(
            @McpToolParam(description =  "The group to send the message to", required = true)
            String gamerGroup
    ) {
        return new GamerGreeting("Hello board gamers @ %s!".formatted(gamerGroup));
    }

    private McpSchema.ReadResourceResult wrapUiHtmlcontent(String uri, String content) {
        return new McpSchema.ReadResourceResult(List.of(
                new McpSchema.TextResourceContents(
                        uri,
                        "text/html;profile=mcp-app",
                        content
                )
        ));
    }
}

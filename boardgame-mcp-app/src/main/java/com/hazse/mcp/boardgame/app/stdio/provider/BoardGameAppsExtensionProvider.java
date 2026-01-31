package com.hazse.mcp.boardgame.app.stdio.provider;

import com.hazse.mcp.boardgame.app.stdio.meta.McpToolMeta;
import com.hazse.mcp.boardgame.app.stdio.utils.ImageDownloader;
import com.hazse.mcp.boardgame.app.stdio.utils.ImageDownloaderException;
import com.hazse.mcp.boardgame.app.stdio.utils.ResourceUtils;
import com.hazse.mcp.boardgame.client.core.BoardGame;
import com.hazse.mcp.boardgame.client.core.BoardGameInformationClient;
import com.hazse.mcp.boardgame.client.core.BoardGameSearchResult;
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
            description = "Gets the cover image of the first board game that matches the provided name"
    )
    @McpToolMeta(
            metadata = "ext_apps_ui=" + UI_BOARD_GAME_DISPLAY
    )
    public McpSchema.CallToolResult getBoardGameCoverWithName(
            @McpToolParam(description =  "The full or partial name of the board games to look for", required = true)
            String name
    ) {
        List<BoardGameSearchResult> searchResults = bggClient.searchGamesByName(name);

        if (!searchResults.isEmpty()) {
            List<BoardGame> boardGames = bggClient.getGameDetailsByIds(Set.of(searchResults.getFirst().getId()));

            if (!boardGames.isEmpty() && boardGames.getFirst().getImageUrl() != null) {
                try {
                    String coverImage = imageDownloader.downloadAsBase64Png(boardGames.getFirst().getImageUrl());
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

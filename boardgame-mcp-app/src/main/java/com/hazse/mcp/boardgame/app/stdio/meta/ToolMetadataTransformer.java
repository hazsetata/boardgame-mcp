package com.hazse.mcp.boardgame.app.stdio.meta;

import java.util.Map;

public interface ToolMetadataTransformer {
    Map<String, Object> transformMetadata(Map<String, Object> metadata);
}

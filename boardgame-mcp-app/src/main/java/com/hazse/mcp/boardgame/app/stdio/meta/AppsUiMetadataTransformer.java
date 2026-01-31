package com.hazse.mcp.boardgame.app.stdio.meta;

import java.util.Map;

public class AppsUiMetadataTransformer implements ToolMetadataTransformer {
    private static final String APPS_UI_METADATA_KEY = "ext_apps_ui";

    private static final String UI_KEY = "ui";
    private static final String RESOURCE_URI_KEY = "resourceUri";
    private static final String UI_RESOURCE_PREFIX = "ui://";

    @Override
    public Map<String, Object> transformMetadata(Map<String, Object> metadata) {
        if (metadata.containsKey(APPS_UI_METADATA_KEY)) {
            String appsUiMetadata = ((String) metadata.get(APPS_UI_METADATA_KEY)).trim();
            metadata.remove(APPS_UI_METADATA_KEY);

            if (!appsUiMetadata.startsWith(UI_RESOURCE_PREFIX)) {
                appsUiMetadata = UI_RESOURCE_PREFIX + appsUiMetadata;
            }
            Map<String, Object> uiMetadata = Map.of(RESOURCE_URI_KEY, appsUiMetadata);
            metadata.put(UI_KEY, uiMetadata);
        }

        return metadata;
    }
}

package com.hazse.mcp.boardgame.app.stdio.config.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "com.hazse.mcp.boardgame.apikey")
@Validated
public class ApiKeyConfigurationProperties {
    private boolean enabled = false;

    private List<String> keys;
}

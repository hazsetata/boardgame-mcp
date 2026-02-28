package com.hazse.mcp.boardgame.app.stdio.config.props;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@ConfigurationProperties(prefix = "com.hazse.mcp.boardgame.bgg")
@Validated
public class AppConfigurationProperties {
    @NotBlank
    private String authenticationToken;
}

package com.hazse.mcp.boardgame.app.stdio.apikey;

import lombok.Builder;
import lombok.Data;
import org.springaicommunity.mcp.security.server.apikey.ApiKeyEntity;

@Data
@Builder
public class HashedApiKey implements ApiKeyEntity {
    private String id;
    private String secret;

    @Override
    public HashedApiKey copy() {
        return HashedApiKey.builder()
                .id(id)
                .secret(secret)
                .build();
    }

    @Override
    public void eraseCredentials() {
        secret = null;
    }
}

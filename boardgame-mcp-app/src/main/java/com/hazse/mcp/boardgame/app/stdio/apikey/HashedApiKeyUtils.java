package com.hazse.mcp.boardgame.app.stdio.apikey;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.util.*;

@UtilityClass
@Slf4j
public class HashedApiKeyUtils {
    public static final int ID_LENGTH = 5;
    private static final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    public Collection<HashedApiKey> readFrom(List<String> keyStrings) {
        Map<String, HashedApiKey> retValue = new HashMap<>();

        if (keyStrings != null && !keyStrings.isEmpty()) {
            for (String keyString : keyStrings) {
                String rawString = new String(
                        Base64.getUrlDecoder().decode(keyString),
                        StandardCharsets.UTF_8
                );

                String id = rawString.substring(0, ID_LENGTH);
                String hashedSecret = rawString.substring(ID_LENGTH);

                HashedApiKey hashedApiKey = new HashedApiKey(id, hashedSecret);
                retValue.put(id, hashedApiKey);
            }
        }

        return retValue.values();
    }

    public void generateRandomApiKey() {
        String id = RandomStringUtils.secure().nextAlphanumeric(ID_LENGTH);
        String secret = UUID.randomUUID().toString();
        String hashedSecret = passwordEncoder.encode(secret);

        String apiKeyToSend = String.format("%s.%s", id, secret);

        String apiKeyRaw = String.format("%s%s", id, hashedSecret);
        String apiKeyToStore = Base64.getUrlEncoder().withoutPadding().encodeToString(apiKeyRaw.getBytes(StandardCharsets.UTF_8));

        log.info("Generated a new API key: {}. Store in config: {}", apiKeyToSend, apiKeyToStore);
    }
}

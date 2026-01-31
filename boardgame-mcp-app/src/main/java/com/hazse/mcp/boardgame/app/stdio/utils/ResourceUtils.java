package com.hazse.mcp.boardgame.app.stdio.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@UtilityClass
@Slf4j
public class ResourceUtils {
    public String readHtmlResource(String resourceName) {
        try (var inputStream = ResourceUtils.class.getClassLoader().getResourceAsStream("content/" + resourceName)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found: content/" + resourceName);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            log.error("Failed to read HTML resource: {}", resourceName, e);

            throw new RuntimeException("Failed to read HTML resource: " + resourceName, e);
        }
    }

    public String readBinaryResource(String resourceName) {
        try (var inputStream = ResourceUtils.class.getClassLoader().getResourceAsStream("content/" + resourceName)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found: content/" + resourceName);
            }
            byte[] bytes = inputStream.readAllBytes();
            return Base64.getEncoder().encodeToString(bytes);
        }
        catch (IOException e) {
            log.error("Failed to read binary resource: {}", resourceName, e);

            throw new RuntimeException("Failed to read binary resource: " + resourceName, e);
        }
    }
}

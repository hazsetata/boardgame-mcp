package com.hazse.mcp.boardgame.app.stdio.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Component
@Slf4j
public class ImageDownloader {
    private static final int TARGET_WIDTH = 368;
    private final RestClient restClient;

    static {
        // Prevents ImageIO from using disk cache, which is crucial for server performance
        // and prevents filling up /tmp in containerized environments.
        ImageIO.setUseCache(false);
    }

    public ImageDownloader(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    public String downloadAsBase64Png(String imageUrl) {
        log.info("Downloading image from URL: {}", imageUrl);

        byte[] imageBytes = restClient.get()
                .uri(imageUrl)
                .retrieve()
                .body(byte[].class);

        if (imageBytes == null || imageBytes.length == 0) {
            throw new ImageDownloaderException("Failed to download image from " + imageUrl);
        }

        try (ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes)) {
            BufferedImage image = ImageIO.read(bais);
            if (image == null) {
                throw new ImageDownloaderException("Failed to parse image from " + imageUrl);
            }

            BufferedImage rescaledImage = rescale(image, TARGET_WIDTH);

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                boolean success = ImageIO.write(rescaledImage, "png", baos);
                if (!success) {
                    throw new ImageDownloaderException("Failed to convert image to PNG: " + imageUrl);
                }
                byte[] pngBytes = baos.toByteArray();
                return Base64.getEncoder().encodeToString(pngBytes);
            }
        }
        catch (IOException e) {
            log.error("Error processing image from {}", imageUrl, e);
            throw new ImageDownloaderException("Error processing image from " + imageUrl, e);
        }
    }

    private BufferedImage rescale(BufferedImage originalImage, int targetWidth) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        if (originalWidth == targetWidth) {
            return originalImage;
        }

        double aspectRatio = (double) originalHeight / originalWidth;
        int targetHeight = (int) Math.max(1, Math.round(targetWidth * aspectRatio));

        log.debug("Rescaling image from {}x{} to {}x{}", originalWidth, originalHeight, targetWidth, targetHeight);

        BufferedImage rescaledImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = rescaledImage.createGraphics();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        }
        finally {
            g2d.dispose();
        }

        return rescaledImage;
    }
}

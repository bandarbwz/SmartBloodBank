package com.smartbloodbank.ui;

import javafx.scene.image.Image;

import java.io.InputStream;

/**
 * Loads image resources from the classpath with explicit failure reporting.
 * A missing or corrupt image resource would otherwise degrade silently
 * (null stream throws deep inside the Image constructor, or a bad decode
 * just leaves the ImageView blank) — both print a clear message here
 * instead, so a packaging mistake shows up as a log line, not a mystery.
 */
final class ImageResources {

    private ImageResources() {
    }

    /**
     * @param owner        class whose package the resourcePath is resolved against
     * @param resourcePath path relative to owner's package, e.g. "images/logo.png"
     * @return the loaded Image, or {@code null} if it could not be found or decoded
     */
    static Image load(Class<?> owner, String resourcePath) {
        InputStream stream = owner.getResourceAsStream(resourcePath);
        if (stream == null) {
            System.err.println("[ImageResources] Resource not found on classpath: " + resourcePath
                    + " (resolved relative to " + owner.getName() + ")");
            return null;
        }
        Image image = new Image(stream);
        if (image.isError()) {
            System.err.println("[ImageResources] Failed to decode image resource: " + resourcePath);
            if (image.getException() != null) {
                image.getException().printStackTrace();
            }
            return null;
        }
        return image;
    }
}

package com.example.cpt202heritage.util;

import java.util.Set;

public final class FileTypeValidator {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of( "jpg", "jpeg", "png", "gif","mp4", "mp3", "wav", "pdf", "doc", "docx"
    );

    private FileTypeValidator() {
    }

    public static boolean isSupported(String filename) {
        if (filename == null || filename.isBlank()) {
            return false;
        }

        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex < 0 || lastDotIndex == filename.length() - 1) {
            return false;
        }

        String extension = filename.substring(lastDotIndex + 1).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(extension);
    }
}

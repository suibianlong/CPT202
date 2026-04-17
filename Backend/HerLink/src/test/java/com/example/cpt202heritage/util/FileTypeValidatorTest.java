package com.example.cpt202heritage.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// test all types of files
class FileTypeValidatorTest {

    @Test
    void isSupported_shouldReturnTrueForAllowedImageFile() {
        // setup
        String filename = "photo.jpg";

        // call
        boolean result = FileTypeValidator.isSupported(filename);

        // assertion
        assertTrue(result);
    }

    @Test
    void isSupported_shouldReturnTrueForAllowedVideoFile() {
        // setup
        String filename = "demo.mp4";

        // call
        boolean result = FileTypeValidator.isSupported(filename);

        // assertion
        assertTrue(result);
    }

    @Test
    void isSupported_shouldReturnTrueForUpperCaseExtension() {
        // setup
        String filename = "report.PDF";

        // call
        boolean result = FileTypeValidator.isSupported(filename);

        // assertion
        assertTrue(result);
    }

    @Test
    void isSupported_shouldReturnFalseForUnsupportedExtension() {
        // setup
        String filename = "virus.exe";

        // call
        boolean result = FileTypeValidator.isSupported(filename);

        // assertion
        assertFalse(result);
    }

    @Test
    void isSupported_shouldReturnFalseWhenFilenameHasNoExtension() {
        // setup
        String filename = "README";

        // call
        boolean result = FileTypeValidator.isSupported(filename);

        // assertion
        assertFalse(result);
    }

    @Test
    void isSupported_shouldReturnFalseWhenFilenameIsNull() {
        // setup
        String filename = null;

        // call
        boolean result = FileTypeValidator.isSupported(filename);

        // assertion
        assertFalse(result);
    }

    @Test
    void isSupported_shouldReturnFalseWhenFilenameIsBlank() {
        // setup
        String filename = " ";

        // call
        boolean result = FileTypeValidator.isSupported(filename);

        // assertion
        assertFalse(result);
    }

    @Test
    void isSupported_shouldReturnFalseWhenFilenameEndsWithDot() {
        // setup
        String filename = "file.";

        // call
        boolean result = FileTypeValidator.isSupported(filename);

        // assertion
        assertFalse(result);
    }
}
package com.ticketsystem.cli;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * The single place that reads from the keyboard.
 * It reads byte by byte (no buffering ahead), so the arrow-key menu and
 * normal line input never steal characters from each other.
 */
public class InputReader {
    private final InputStream in;

    public InputReader(InputStream in) {
        this.in = in;
    }

    /** Reads one raw byte. Returns -1 when input has ended. */
    public int readByte() {
        try {
            return in.read();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String readLine(String prompt) {
        System.out.print(prompt);
        System.out.flush();

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int b = readByte();
        while (b != -1 && b != '\n') {
            if (b != '\r') {
                buffer.write(b);
            }
            b = readByte();
        }
        if (b == -1 && buffer.size() == 0) {
            throw new InputEndedException();
        }
        return buffer.toString(StandardCharsets.UTF_8).trim();
    }

    /** Keeps asking until the user types something. */
    public String readRequiredLine(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("  This field is required.");
        }
    }

    public int readInt(String prompt) {
        while (true) {
            String value = readLine(prompt);
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.out.println("  Please enter a number.");
            }
        }
    }
}

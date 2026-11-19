package com.ticketsystem.cli;

/** Thrown when the keyboard/input stream is closed (Ctrl+D or end of a piped file). */
public class InputEndedException extends RuntimeException {
    public InputEndedException() {
        super("Input ended");
    }
}

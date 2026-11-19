package com.ticketsystem.cli;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Lets the user move through a menu with the Up/Down arrow keys and press Enter.
 *
 * How it works:
 *  1. Normally the terminal waits for Enter before giving us any input.
 *     We run "stty -icanon -echo" so every key press arrives immediately.
 *  2. Arrow keys arrive as 3 bytes: ESC '[' 'A' (up) or ESC '[' 'B' (down).
 *  3. After each key we move the cursor back up (ANSI codes) and redraw the menu.
 *  4. The terminal settings are always restored afterwards.
 *
 * Works on macOS and Linux terminals. Main falls back to NumberedMenuSelector elsewhere.
 */
public class ArrowKeyMenuSelector implements MenuSelector {
    private static final String CSI = "\033[";
    private static final String RESET = "\033[0m";
    private static final String BOLD = "\033[1m";
    private static final String DIM = "\033[2m";
    private static final String HIGHLIGHT = "\033[1;36m"; // bold cyan

    private static final int KEY_UP = -2;
    private static final int KEY_DOWN = -3;
    private static final int KEY_ENTER = -4;
    private static final int KEY_OTHER = -5;

    private final InputReader input;

    public ArrowKeyMenuSelector(InputReader input) {
        this.input = input;
        restoreTerminalOnExit();
    }

    /** True only in a real macOS/Linux terminal where "stty" works. */
    public static boolean isSupported() {
        if (System.console() == null) {
            return false; // input or output is piped / redirected
        }
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            return false;
        }
        String term = System.getenv("TERM");
        if (term == null || term.equals("dumb")) {
            return false;
        }
        return runStty("-g") != null;
    }

    @Override
    public int select(String title, List<String> options) {
        String savedMode = runStty("-g");
        runStty("-icanon -echo min 1");
        System.out.print(CSI + "?25l"); // hide the cursor while choosing

        int selected = 0;
        try {
            draw(title, options, selected, false);
            while (true) {
                int key = readKey();
                if (key == KEY_UP) {
                    selected = (selected - 1 + options.size()) % options.size(); // wrap to bottom
                } else if (key == KEY_DOWN) {
                    selected = (selected + 1) % options.size();                  // wrap to top
                } else if (key == KEY_ENTER) {
                    break;
                } else if (key >= '1' && key <= '9' && key - '1' < options.size()) {
                    selected = key - '1'; // number shortcut
                    break;
                } else {
                    continue; // ignore any other key
                }
                draw(title, options, selected, true);
            }
        } finally {
            System.out.print(CSI + "?25h"); // show the cursor again
            if (savedMode != null) {
                runStty(savedMode);
            }
        }

        // replace the whole menu with one short line, so the screen stays tidy
        System.out.print(CSI + linesUsed(options) + "A\r" + CSI + "J");
        System.out.println(BOLD + title + ": " + RESET + options.get(selected));
        return selected;
    }

    private void draw(String title, List<String> options, int selected, boolean redraw) {
        StringBuilder screen = new StringBuilder();
        if (redraw) {
            screen.append(CSI).append(linesUsed(options)).append("A"); // cursor back to the top of the menu
        }
        screen.append(clearLine()).append(BOLD).append(title).append(RESET).append('\n');
        for (int i = 0; i < options.size(); i++) {
            screen.append(clearLine());
            String label = (i + 1) + ". " + options.get(i);
            if (i == selected) {
                screen.append(HIGHLIGHT).append("  > ").append(label).append(RESET);
            } else {
                screen.append("    ").append(label);
            }
            screen.append('\n');
        }
        screen.append(clearLine()).append(DIM)
                .append("  Up/Down to move, Enter to select, or press a number").append(RESET).append('\n');
        System.out.print(screen);
        System.out.flush();
    }

    private int linesUsed(List<String> options) {
        return options.size() + 2; // title + options + hint
    }

    private String clearLine() {
        return "\r" + CSI + "2K";
    }

    private int readKey() {
        int b = input.readByte();
        if (b == -1) {
            throw new InputEndedException();
        }
        if (b == '\n' || b == '\r') {
            return KEY_ENTER;
        }
        if (b == 'k' || b == 'w') {
            return KEY_UP; // vim / gamer keys as a bonus
        }
        if (b == 'j' || b == 's') {
            return KEY_DOWN;
        }
        if (b == 27) { // ESC: start of an arrow key sequence
            int second = input.readByte();
            if (second == '[' || second == 'O') {
                int third = input.readByte();
                if (third == 'A') {
                    return KEY_UP;
                }
                if (third == 'B') {
                    return KEY_DOWN;
                }
            }
            return KEY_OTHER;
        }
        return b;
    }

    /** If the app is killed with Ctrl+C mid-menu, put the terminal back to normal. */
    private void restoreTerminalOnExit() {
        String originalMode = runStty("-g");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.print(CSI + "?25h");
            System.out.flush();
            if (originalMode != null) {
                runStty(originalMode);
            }
        }));
    }

    /** Runs "stty <args>" against the real terminal. Returns its output, or null if it failed. */
    private static String runStty(String args) {
        try {
            Process process = new ProcessBuilder("sh", "-c", "stty " + args + " < /dev/tty")
                    .redirectErrorStream(true)
                    .start();
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            return process.waitFor() == 0 ? output : null;
        } catch (IOException e) {
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
}

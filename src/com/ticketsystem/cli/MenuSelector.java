package com.ticketsystem.cli;

import java.util.List;

/**
 * "How does the user pick one option from a list?"
 * Arrow keys in a real terminal, typed numbers everywhere else.
 * ConsoleApp doesn't care which one it gets (Strategy + Dependency Inversion).
 */
public interface MenuSelector {

    /** Shows the options and returns the index (0-based) of the one chosen. */
    int select(String title, List<String> options);
}

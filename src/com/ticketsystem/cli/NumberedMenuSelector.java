package com.ticketsystem.cli;

import java.util.List;

/** Fallback selector: works everywhere (Windows, IDE consoles, piped input). */
public class NumberedMenuSelector implements MenuSelector {
    private final InputReader input;

    public NumberedMenuSelector(InputReader input) {
        this.input = input;
    }

    @Override
    public int select(String title, List<String> options) {
        System.out.println(title);
        for (int i = 0; i < options.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + options.get(i));
        }

        while (true) {
            String answer = input.readLine("Choose 1-" + options.size() + ": ");
            int index = toIndex(answer, options);
            if (index != -1) {
                return index;
            }
            System.out.println("  Please choose a number between 1 and " + options.size() + ".");
        }
    }

    /** Accepts either the number ("2") or the option text ("medium"). */
    private int toIndex(String answer, List<String> options) {
        try {
            int number = Integer.parseInt(answer);
            if (number >= 1 && number <= options.size()) {
                return number - 1;
            }
        } catch (NumberFormatException e) {
            for (int i = 0; i < options.size(); i++) {
                if (options.get(i).equalsIgnoreCase(answer)) {
                    return i;
                }
            }
        }
        return -1;
    }
}

package org.example;

import com.googlecode.lanterna.TextColor;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ANSIParser {

    // Regex to match ANSI escape sequences (e.g., \033[31m)
    private static final Pattern ANSI_PATTERN = Pattern.compile("\u001B\\[(\\d+)(;(\\d+))*m");

    // Maps ANSI codes to Lanterna colors
    private static final Map<Integer, TextColor> COLOR_MAP = Map.ofEntries(
        Map.entry(30, TextColor.ANSI.BLACK),
        Map.entry(31, TextColor.ANSI.RED),
        Map.entry(32, TextColor.ANSI.GREEN),
        Map.entry(33, TextColor.ANSI.YELLOW),
        Map.entry(34, TextColor.ANSI.BLUE),
        Map.entry(35, TextColor.ANSI.MAGENTA),
        Map.entry(36, TextColor.ANSI.CYAN),
        Map.entry(37, TextColor.ANSI.WHITE),
        Map.entry(0, TextColor.ANSI.DEFAULT)
    );

    public static List<ParsedCharacter> parse(String input) {
        List<ParsedCharacter> output = new ArrayList<>();
        TextColor currentColor = TextColor.ANSI.DEFAULT;

        Matcher matcher = ANSI_PATTERN.matcher(input);
        int lastEnd = 0;

        while (matcher.find()) {
            // Text before ANSI sequence
            String plainText = input.substring(lastEnd, matcher.start());
            for (char c : plainText.toCharArray()) {
                output.add(new ParsedCharacter(c, currentColor));
            }

            // Parse ANSI sequence
            String[] codes = matcher.group().replaceAll("[\\u001B\\[m]", "").split(";");
            for (String code : codes) {
                try {
                    int codeNum = Integer.parseInt(code);
                    if (COLOR_MAP.containsKey(codeNum)) {
                        currentColor = COLOR_MAP.get(codeNum);
                    } else if (codeNum == 0) {
                        currentColor = TextColor.ANSI.DEFAULT;
                    }
                } catch (NumberFormatException ignored) {}
            }

            lastEnd = matcher.end();
        }

        // Remainder after last ANSI
        String plainText = input.substring(lastEnd);
        for (char c : plainText.toCharArray()) {
            output.add(new ParsedCharacter(c, currentColor));
        }

        return output;
    }

    public static class ParsedCharacter {
        public final char character;
        public final TextColor color;

        public ParsedCharacter(char character, TextColor color) {
            this.character = character;
            this.color = color;
        }
    }
}

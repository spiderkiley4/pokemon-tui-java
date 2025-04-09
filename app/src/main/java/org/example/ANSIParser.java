package org.example;

import java.util.*;
import java.util.regex.*;
import com.googlecode.lanterna.TextColor;

public class ANSIParser {

    public static class ANSIStyle {
        public String text;
        public TextColor foreground;
        public TextColor background;
        public boolean bold;
        public boolean underline;

        public ANSIStyle(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return String.format("Text: \"%s\", FG: %s, BG: %s, Bold: %b, Underline: %b",
                    text, foreground, background, bold, underline);
        }
    }

    private static final Pattern ESC_SEQ = Pattern.compile("\u001B\\[([0-9;]*)m");

    public static List<ANSIStyle> parse(String input) {
        List<ANSIStyle> result = new ArrayList<>();
        Matcher matcher = ESC_SEQ.matcher(input);

        int lastEnd = 0;
        ANSIStyle currentStyle = new ANSIStyle("");
        resetStyle(currentStyle);

        while (matcher.find()) {
            // Add preceding plain or styled text
            if (matcher.start() > lastEnd) {
                ANSIStyle styledText = copyStyle(currentStyle);
                styledText.text = input.substring(lastEnd, matcher.start());
                result.add(styledText);
            }

            // Parse the ANSI sequence
            String[] codes = matcher.group(1).isEmpty() ? new String[]{"0"} : matcher.group(1).split(";");
            applyCodes(currentStyle, codes);

            lastEnd = matcher.end();
        }

        // Remaining text
        if (lastEnd < input.length()) {
            ANSIStyle styledText = copyStyle(currentStyle);
            styledText.text = input.substring(lastEnd);
            result.add(styledText);
        }

        return result;
    }

    private static void applyCodes(ANSIStyle style, String[] codes) {
        for (int i = 0; i < codes.length; i++) {
            int code = Integer.parseInt(codes[i]);
            switch (code) {
                case 0 -> resetStyle(style);
                case 1 -> style.bold = true;
                case 4 -> style.underline = true;
                case 22 -> style.bold = false;
                case 24 -> style.underline = false;
                case 38, 48 -> {
                    // Handle RGB colors
                    if (i + 4 < codes.length && Integer.parseInt(codes[i + 1]) == 2) {
                        int r = Integer.parseInt(codes[i + 2]);
                        int g = Integer.parseInt(codes[i + 3]);
                        int b = Integer.parseInt(codes[i + 4]);
                        TextColor color = new TextColor.RGB(r, g, b);
                        if (code == 38) style.foreground = color;
                        else style.background = color;
                        i += 4; // Skip the color parameters
                    }
                }
                case 39 -> style.foreground = TextColor.ANSI.DEFAULT;
                case 49 -> style.background = TextColor.ANSI.DEFAULT;
                default -> {
                    if (30 <= code && code <= 37)
                        style.foreground = TextColor.ANSI.values()[code - 30];
                    else if (40 <= code && code <= 47)
                        style.background = TextColor.ANSI.values()[code - 40];
                    else if (90 <= code && code <= 97)
                        style.foreground = TextColor.ANSI.values()[(code - 90) + 8];
                    else if (100 <= code && code <= 107)
                        style.background = TextColor.ANSI.values()[(code - 100) + 8];
                }
            }
        }
    }

    private static void resetStyle(ANSIStyle style) {
        style.foreground = TextColor.ANSI.DEFAULT;
        style.background = TextColor.ANSI.DEFAULT;
        style.bold = false;
        style.underline = false;
    }

    private static ANSIStyle copyStyle(ANSIStyle style) {
        ANSIStyle newStyle = new ANSIStyle("");
        newStyle.foreground = style.foreground;
        newStyle.background = style.background;
        newStyle.bold = style.bold;
        newStyle.underline = style.underline;
        return newStyle;
    }
}

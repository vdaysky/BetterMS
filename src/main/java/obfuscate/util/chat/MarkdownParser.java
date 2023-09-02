package obfuscate.util.chat;

import java.util.ArrayList;

public class MarkdownParser {

    final static char BOLD = '*';
    final static char ITALIC = '/';
    final static char STRIKE = '~';
    final static char UNDERLINE = '_';

    private static String modifiersToString(ArrayList<Character> modifiers) {
        StringBuilder result = new StringBuilder();
        for (char c : modifiers) {
            if (c == BOLD) {
                result.append(C.Bold);
            } else if (c == ITALIC) {
                result.append(C.Italics);
            } else if (c == STRIKE) {
                result.append(C.Strike);
            } else if (c == UNDERLINE) {
                result.append(C.Line);
            }
        }
        return result.toString();
    }

    public static String parse(String raw) {


        int boldCount = 0;
        int italicCount = 0;
        int strikeCount = 0;
        int underlineCount = 0;

        boolean escaped = false;
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);

            if (c == '\\') {
                if (!escaped) {
                    escaped = true;
                    continue;
                }
            }

            if (!escaped) {
                if (c == BOLD) {
                    boldCount++;
                } else if (c == ITALIC) {
                    italicCount++;
                } else if (c == STRIKE) {
                    strikeCount++;
                } else if (c == UNDERLINE) {
                    underlineCount++;
                }
            }
            escaped = false;
        }

        StringBuilder result = new StringBuilder();
        ArrayList<Character> modifiers = new ArrayList<>();

        escaped = false;
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);

            if (c == '\\') {
                if (!escaped) {
                    escaped = true;
                    continue;
                }
            }

            if (!escaped && (
                    (boldCount % 2 == 0 && c == BOLD) ||
                    (italicCount % 2 == 0 && c == ITALIC) ||
                    (strikeCount % 2 == 0 && c == STRIKE) ||
                    (underlineCount % 2 == 0 && c == UNDERLINE)
            )) {
                if (modifiers.contains(c)) {
                    modifiers.remove((Character) c);
                } else {
                    modifiers.add(c);
                }
                result.append(C.Reset);
                result.append(modifiersToString(modifiers));
            } else {
                result.append(c);
            }
            escaped = false;
        }
        return result.toString();
    }
}

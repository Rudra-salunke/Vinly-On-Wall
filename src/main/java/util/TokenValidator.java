package util;

import java.util.regex.Pattern;

public final class TokenValidator {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("[0-9a-f]{32}");

    private TokenValidator() {
    }

    public static boolean isValid(String token) {
        return token != null && TOKEN_PATTERN.matcher(token).matches();
    }
}

package org.example.util;

public final class LogUtil {

    public LogUtil() {}

    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }

        String[] parts = email.split("@", 2);
        String local = parts[0];
        String domain = parts[1];

        if (local.length() <= 1) {
            return "*@" + domain;
        }

        return local.charAt(0) + "***@" + domain;
    }
}

package com.example.itc;

public class RunLengthEncoding {

    public String encode(String string) {
        if (string == null || string.isEmpty()) return "";

        StringBuilder builder = new StringBuilder();
        char[] chars = string.toCharArray();
        char current = chars[0];
        int count = 1;

        for (int i = 1; i < chars.length; i++) {
            if (current == chars[i]) {
                count++;
            } else {
                if (count > 1) builder.append(count);
                builder.append(current);
                current = chars[i];
                count = 1;
            }
        }
        if (count > 1) builder.append(count);
        builder.append(current);
        return builder.toString();
    }

    public String decode(String string) {
        if (string == null || string.isEmpty()) return "";

        StringBuilder builder = new StringBuilder();
        char[] chars = string.toCharArray();
        boolean preIsDigit = false;
        String digitString = "";
        for (char current : chars) {
            if (!Character.isDigit(current)) {
                if (preIsDigit) {
                    String multipleString = new String(new char[Integer.valueOf(digitString)]).replace("\0", current + "");
                    builder.append(multipleString);
                    preIsDigit = false;
                    digitString = "";
                } else {
                    builder.append(current);
                }
            } else {
                digitString += current;
                preIsDigit = true;
            }
        }
        return builder.toString();
    }
}

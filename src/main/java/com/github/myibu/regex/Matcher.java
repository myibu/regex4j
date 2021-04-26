package com.github.myibu.regex;

public class Matcher {
    Pattern pattern;

    public Matcher(Pattern pattern) {
        this.pattern = pattern;
    }

    public boolean matches(String input) {
        if (null == input) {
            throw new IllegalArgumentException("input can not be empty string");
        }
        return pattern.nfa().search(input);
    }
}

package dev.kasapdev.argparser;

/**
 * Internal registration record for a single flag or value-taking option.
 */
final class OptionSpec {
    final String name;
    final String shortAlias; // may be null
    final String description;
    final boolean isFlag;
    final String defaultValue; // only meaningful for options (null for flags)

    OptionSpec(String name, String shortAlias, String description, boolean isFlag, String defaultValue) {
        this.name = name;
        this.shortAlias = shortAlias;
        this.description = description;
        this.isFlag = isFlag;
        this.defaultValue = defaultValue;
    }
}

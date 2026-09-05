package dev.kasapdev.argparser;

/**
 * Thrown when command-line arguments cannot be parsed against the registered options —
 * e.g. an unrecognized {@code --flag}, or an option that requires a value but none was given.
 */
public class ArgParseException extends RuntimeException {
    public ArgParseException(String message) {
        super(message);
    }
}

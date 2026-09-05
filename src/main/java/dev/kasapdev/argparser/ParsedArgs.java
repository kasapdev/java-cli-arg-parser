package dev.kasapdev.argparser;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The result of {@link ArgParser#parse(String[])}: resolved flag states, resolved option
 * values (with defaults applied), and the list of positional arguments in order.
 */
public final class ParsedArgs {
    private final Map<String, Boolean> flags;
    private final Map<String, String> options;
    private final List<String> positionals;

    ParsedArgs(Map<String, Boolean> flags, Map<String, String> options, List<String> positionals) {
        this.flags = Collections.unmodifiableMap(flags);
        this.options = Collections.unmodifiableMap(options);
        this.positionals = Collections.unmodifiableList(positionals);
    }

    /**
     * @param name a registered flag's long name (without leading dashes)
     * @return {@code true} if the flag was present on the command line, {@code false}
     *         otherwise (including if {@code name} was never registered)
     */
    public boolean flag(String name) {
        return Boolean.TRUE.equals(flags.get(name));
    }

    /**
     * @param name a registered option's long name (without leading dashes)
     * @return the value supplied on the command line, or the option's default value if it
     *         was not supplied, or {@code null} if {@code name} was never registered
     */
    public String option(String name) {
        return options.get(name);
    }

    /** @return the positional arguments, in the order they appeared */
    public List<String> positionals() {
        return positionals;
    }
}

package dev.kasapdev.argparser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The result of {@link ArgParser#parse(String[])}: resolved flag states, resolved option
 * values (with defaults applied), and the list of positional arguments in order.
 *
 * <p>When the parser that produced this result has registered subcommands and one was
 * invoked, {@link #subcommandName()} and {@link #subcommand()} expose which subcommand ran
 * and its own, independently-parsed result; in that case this outer result's own
 * flags/options/positionals are empty, since the whole remainder of the arguments was
 * handed to the subcommand's parser.
 */
public final class ParsedArgs {
    private final Map<String, Boolean> flags;
    private final Map<String, String> options;
    private final List<String> positionals;
    private final String subcommandName;
    private final ParsedArgs subcommandResult;

    ParsedArgs(Map<String, Boolean> flags, Map<String, String> options, List<String> positionals) {
        this(flags, options, positionals, null, null);
    }

    private ParsedArgs(Map<String, Boolean> flags, Map<String, String> options, List<String> positionals,
                        String subcommandName, ParsedArgs subcommandResult) {
        this.flags = Collections.unmodifiableMap(flags);
        this.options = Collections.unmodifiableMap(options);
        this.positionals = Collections.unmodifiableList(positionals);
        this.subcommandName = subcommandName;
        this.subcommandResult = subcommandResult;
    }

    /**
     * Builds the outer result for a parse that dispatched to a subcommand: no top-level
     * flags/options/positionals of its own, just the invoked subcommand's name and result.
     */
    static ParsedArgs forSubcommand(String name, ParsedArgs subcommandResult) {
        return new ParsedArgs(new HashMap<>(), new HashMap<>(), new ArrayList<>(), name, subcommandResult);
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

    /**
     * @return the name of the subcommand that was invoked, or {@code null} if the parser
     *         that produced this result has no registered subcommands, or none was invoked
     */
    public String subcommandName() {
        return subcommandName;
    }

    /**
     * @return the invoked subcommand's own, independently-parsed result, or {@code null} if
     *         the parser that produced this result has no registered subcommands, or none
     *         was invoked
     */
    public ParsedArgs subcommand() {
        return subcommandResult;
    }
}

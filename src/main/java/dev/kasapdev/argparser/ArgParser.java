package dev.kasapdev.argparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A lightweight command-line argument parser.
 *
 * <p>Supports boolean flags ({@code --verbose} / {@code -v}), value-taking options
 * ({@code --output=file.txt} or {@code --output file.txt}, with an optional {@code -o} short
 * alias), positional arguments, and generated {@code --help} text.
 *
 * <p>An {@code --} token by itself marks the end of options: everything after it is treated
 * as a positional argument, even if it looks like a flag.
 */
public final class ArgParser {

    private final Map<String, OptionSpec> specsByName = new LinkedHashMap<>();
    private final Map<String, String> aliasToName = new HashMap<>();

    /** Registers a boolean flag with no short alias, e.g. {@code --verbose}. */
    public ArgParser addFlag(String name, String description) {
        return addFlag(name, null, description);
    }

    /** Registers a boolean flag with a short alias, e.g. {@code --verbose} / {@code -v}. */
    public ArgParser addFlag(String name, String shortAlias, String description) {
        register(new OptionSpec(name, shortAlias, description, true, null));
        return this;
    }

    /** Registers a value-taking option with no short alias, e.g. {@code --output=file.txt}. */
    public ArgParser addOption(String name, String description, String defaultValue) {
        return addOption(name, null, description, defaultValue);
    }

    /** Registers a value-taking option with a short alias, e.g. {@code --output} / {@code -o}. */
    public ArgParser addOption(String name, String shortAlias, String description, String defaultValue) {
        register(new OptionSpec(name, shortAlias, description, false, defaultValue));
        return this;
    }

    private void register(OptionSpec spec) {
        if (specsByName.containsKey(spec.name)) {
            throw new IllegalArgumentException("Option already registered: --" + spec.name);
        }
        specsByName.put(spec.name, spec);
        if (spec.shortAlias != null) {
            if (aliasToName.containsKey(spec.shortAlias)) {
                throw new IllegalArgumentException("Short alias already registered: -" + spec.shortAlias);
            }
            aliasToName.put(spec.shortAlias, spec.name);
        }
    }

    /**
     * Parses the given command-line arguments against the registered flags and options.
     *
     * @throws ArgParseException on an unrecognized {@code --flag}/{@code -f}, or an option
     *                           that is missing its required value
     */
    public ParsedArgs parse(String[] args) {
        Map<String, Boolean> flags = new HashMap<>();
        Map<String, String> options = new HashMap<>();
        for (OptionSpec spec : specsByName.values()) {
            if (spec.isFlag) {
                flags.put(spec.name, false);
            } else {
                options.put(spec.name, spec.defaultValue);
            }
        }

        List<String> positionals = new ArrayList<>();
        boolean endOfOptions = false;

        int i = 0;
        while (i < args.length) {
            String token = args[i];

            if (!endOfOptions && token.equals("--")) {
                endOfOptions = true;
                i++;
                continue;
            }

            if (!endOfOptions && token.startsWith("--")) {
                String body = token.substring(2);
                String name;
                String inlineValue;
                int eq = body.indexOf('=');
                if (eq >= 0) {
                    name = body.substring(0, eq);
                    inlineValue = body.substring(eq + 1);
                } else {
                    name = body;
                    inlineValue = null;
                }
                OptionSpec spec = specsByName.get(name);
                if (spec == null) {
                    throw new ArgParseException("Unknown flag: --" + name);
                }
                i = applyOption(spec, token, inlineValue, args, i, flags, options);
            } else if (!endOfOptions && token.startsWith("-") && token.length() > 1) {
                String body = token.substring(1);
                String alias;
                String inlineValue;
                int eq = body.indexOf('=');
                if (eq >= 0) {
                    alias = body.substring(0, eq);
                    inlineValue = body.substring(eq + 1);
                } else {
                    alias = body;
                    inlineValue = null;
                }
                String name = aliasToName.get(alias);
                if (name == null) {
                    throw new ArgParseException("Unknown flag: " + token);
                }
                OptionSpec spec = specsByName.get(name);
                i = applyOption(spec, token, inlineValue, args, i, flags, options);
            } else {
                positionals.add(token);
                i++;
            }
        }

        return new ParsedArgs(flags, options, positionals);
    }

    /**
     * Applies a single recognized flag/option occurrence starting at index {@code i} in
     * {@code args}, consuming an extra token for the value if needed. Returns the next index
     * to continue parsing from.
     */
    private int applyOption(OptionSpec spec, String originalToken, String inlineValue, String[] args, int i,
                             Map<String, Boolean> flags, Map<String, String> options) {
        if (spec.isFlag) {
            if (inlineValue != null) {
                throw new ArgParseException("Flag does not take a value: " + originalToken);
            }
            flags.put(spec.name, true);
            return i + 1;
        } else {
            if (inlineValue != null) {
                options.put(spec.name, inlineValue);
                return i + 1;
            }
            if (i + 1 >= args.length) {
                throw new ArgParseException("Missing value for option: --" + spec.name);
            }
            options.put(spec.name, args[i + 1]);
            return i + 2;
        }
    }

    /**
     * @return generated help text listing every registered flag/option with its aliases,
     *         defaults, and description, one per line
     */
    public String helpText() {
        StringBuilder sb = new StringBuilder();
        for (OptionSpec spec : specsByName.values()) {
            StringBuilder header = new StringBuilder("  --").append(spec.name);
            if (spec.shortAlias != null) {
                header.append(", -").append(spec.shortAlias);
            }
            if (!spec.isFlag) {
                header.append(" <value>");
            }
            sb.append(header);
            int padding = Math.max(1, 28 - header.length());
            for (int p = 0; p < padding; p++) {
                sb.append(' ');
            }
            sb.append(spec.description);
            if (!spec.isFlag && spec.defaultValue != null) {
                sb.append(" (default: ").append(spec.defaultValue).append(")");
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}

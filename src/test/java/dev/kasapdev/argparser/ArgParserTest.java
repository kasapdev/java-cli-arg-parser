package dev.kasapdev.argparser;

import java.util.List;

public final class ArgParserTest {

    public static void main(String[] args) {
        testMixedFlagsOptionsAndPositionals();
        testEqualsSyntaxForOptionValue();
        testSpaceSeparatedSyntaxForOptionValue();
        testShortAliasesForFlagsAndOptions();
        testUnknownLongFlagThrows();
        testUnknownShortFlagThrows();
        testDefaultValueAppliesWhenOptionOmitted();
        testFlagDefaultsToFalseWhenOmitted();
        testMissingValueForOptionThrows();
        testFlagRejectsInlineValue();
        testEndOfOptionsMarkerTreatsRestAsPositional();
        testHelpTextListsAllRegisteredOptions();
        testDuplicateRegistrationRejected();
        testUnregisteredNameReturnsFalseOrNull();
        testNullDefaultValueResolvesToNullWhenOmitted();
        testDuplicateShortAliasRejected();
        testShortFlagRejectsInlineValue();
        testShortAliasOptionMissingValueThrows();
        testLoneDashIsTreatedAsPositional();

        TestKit.finish();
    }

    private static void testUnregisteredNameReturnsFalseOrNull() {
        ArgParser parser = buildStandardParser();
        ParsedArgs parsed = parser.parse(new String[]{});
        TestKit.check("flag() for a never-registered name returns false", !parsed.flag("nonexistent-flag"));
        TestKit.check("option() for a never-registered name returns null", parsed.option("nonexistent-option") == null);
    }

    private static void testNullDefaultValueResolvesToNullWhenOmitted() {
        ArgParser parser = new ArgParser().addOption("tag", "An optional tag", null);
        ParsedArgs parsed = parser.parse(new String[]{});
        TestKit.check("an option with a null default resolves to null (not the string \"null\") when omitted", parsed.option("tag") == null);
    }

    private static void testDuplicateShortAliasRejected() {
        ArgParser parser = new ArgParser().addFlag("verbose", "v", "desc");
        boolean threw = false;
        try {
            parser.addOption("value", "v", "a different option reusing -v", "default");
        } catch (IllegalArgumentException e) {
            threw = true;
        }
        TestKit.check("registering a short alias already claimed by another option is rejected", threw);
    }

    private static void testShortFlagRejectsInlineValue() {
        ArgParser parser = buildStandardParser();
        boolean threw = false;
        try {
            parser.parse(new String[]{"-v=true"});
        } catch (ArgParseException e) {
            threw = true;
        }
        TestKit.check("a short flag with an inline '=' value throws ArgParseException, mirroring the long-flag case", threw);
    }

    private static void testShortAliasOptionMissingValueThrows() {
        ArgParser parser = buildStandardParser();
        boolean threw = false;
        try {
            parser.parse(new String[]{"-o"});
        } catch (ArgParseException e) {
            threw = true;
        }
        TestKit.check("a short-alias option missing its value at end of args throws ArgParseException", threw);
    }

    private static void testLoneDashIsTreatedAsPositional() {
        ArgParser parser = buildStandardParser();
        ParsedArgs parsed = parser.parse(new String[]{"-"});
        TestKit.check("a lone '-' token is treated as a positional argument, not an unknown flag", parsed.positionals().equals(List.of("-")));
    }

    private static ArgParser buildStandardParser() {
        return new ArgParser()
                .addFlag("verbose", "v", "Enable verbose output")
                .addOption("output", "o", "Output file path", "out.txt")
                .addOption("format", "Output format", "json");
    }

    private static void testMixedFlagsOptionsAndPositionals() {
        ArgParser parser = buildStandardParser();
        ParsedArgs parsed = parser.parse(new String[]{"input.csv", "--verbose", "-o", "result.json", "extra.txt"});

        TestKit.check("verbose flag detected", parsed.flag("verbose"));
        TestKit.check("output option resolved via short alias + space syntax", parsed.option("output").equals("result.json"));
        TestKit.check("format option falls back to default", parsed.option("format").equals("json"));
        TestKit.check("positionals collected in order, interleaved with options",
                parsed.positionals().equals(List.of("input.csv", "extra.txt")));
    }

    private static void testEqualsSyntaxForOptionValue() {
        ArgParser parser = buildStandardParser();
        ParsedArgs parsed = parser.parse(new String[]{"--output=report.csv"});
        TestKit.check("--opt=value syntax parses correctly", parsed.option("output").equals("report.csv"));
    }

    private static void testSpaceSeparatedSyntaxForOptionValue() {
        ArgParser parser = buildStandardParser();
        ParsedArgs parsed = parser.parse(new String[]{"--output", "report.csv"});
        TestKit.check("--opt value syntax parses correctly", parsed.option("output").equals("report.csv"));
    }

    private static void testShortAliasesForFlagsAndOptions() {
        ArgParser parser = buildStandardParser();
        ParsedArgs parsed = parser.parse(new String[]{"-v", "-o=short.txt"});
        TestKit.check("short flag alias -v sets verbose", parsed.flag("verbose"));
        TestKit.check("short option alias -o= sets output value", parsed.option("output").equals("short.txt"));
    }

    private static void testUnknownLongFlagThrows() {
        ArgParser parser = buildStandardParser();
        boolean threw = false;
        String message = null;
        try {
            parser.parse(new String[]{"--nonexistent"});
        } catch (ArgParseException e) {
            threw = true;
            message = e.getMessage();
        }
        TestKit.check("unknown long flag throws ArgParseException", threw);
        TestKit.check("exception message names the bad token", message != null && message.contains("--nonexistent"));
    }

    private static void testUnknownShortFlagThrows() {
        ArgParser parser = buildStandardParser();
        boolean threw = false;
        String message = null;
        try {
            parser.parse(new String[]{"-z"});
        } catch (ArgParseException e) {
            threw = true;
            message = e.getMessage();
        }
        TestKit.check("unknown short flag throws ArgParseException", threw);
        TestKit.check("exception message names the bad token", message != null && message.contains("-z"));
    }

    private static void testDefaultValueAppliesWhenOptionOmitted() {
        ArgParser parser = buildStandardParser();
        ParsedArgs parsed = parser.parse(new String[]{});
        TestKit.check("default applies for 'output' when omitted", parsed.option("output").equals("out.txt"));
        TestKit.check("default applies for 'format' when omitted", parsed.option("format").equals("json"));
    }

    private static void testFlagDefaultsToFalseWhenOmitted() {
        ArgParser parser = buildStandardParser();
        ParsedArgs parsed = parser.parse(new String[]{});
        TestKit.check("verbose flag defaults to false when omitted", !parsed.flag("verbose"));
    }

    private static void testMissingValueForOptionThrows() {
        ArgParser parser = buildStandardParser();
        boolean threw = false;
        try {
            parser.parse(new String[]{"--output"});
        } catch (ArgParseException e) {
            threw = true;
        }
        TestKit.check("option missing its value throws ArgParseException", threw);
    }

    private static void testFlagRejectsInlineValue() {
        ArgParser parser = buildStandardParser();
        boolean threw = false;
        try {
            parser.parse(new String[]{"--verbose=true"});
        } catch (ArgParseException e) {
            threw = true;
        }
        TestKit.check("flag with inline value via '=' throws ArgParseException", threw);
    }

    private static void testEndOfOptionsMarkerTreatsRestAsPositional() {
        ArgParser parser = buildStandardParser();
        ParsedArgs parsed = parser.parse(new String[]{"--", "--verbose", "-o"});
        TestKit.check("everything after '--' is positional", parsed.positionals().equals(List.of("--verbose", "-o")));
        TestKit.check("flags untouched after end-of-options marker", !parsed.flag("verbose"));
    }

    private static void testHelpTextListsAllRegisteredOptions() {
        ArgParser parser = buildStandardParser();
        String help = parser.helpText();
        TestKit.check("help text mentions --verbose", help.contains("--verbose"));
        TestKit.check("help text mentions short alias -v", help.contains("-v"));
        TestKit.check("help text mentions --output default value", help.contains("out.txt"));
        TestKit.check("help text mentions descriptions", help.contains("Enable verbose output"));
    }

    private static void testDuplicateRegistrationRejected() {
        ArgParser parser = new ArgParser().addFlag("verbose", "v", "desc");
        boolean threw = false;
        try {
            parser.addFlag("verbose", "Another desc");
        } catch (IllegalArgumentException e) {
            threw = true;
        }
        TestKit.check("registering the same option name twice is rejected", threw);
    }
}

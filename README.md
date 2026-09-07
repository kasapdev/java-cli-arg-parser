# java-cli-arg-parser

[![CI](https://github.com/kasapdev/java-cli-arg-parser/actions/workflows/ci.yml/badge.svg)](https://github.com/kasapdev/java-cli-arg-parser/actions/workflows/ci.yml) [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE) ![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)

A lightweight, zero-dependency command-line argument parser for Java. Supports boolean
flags, value-taking options (`--opt=value` or `--opt value`), short aliases (`-v` for
`--verbose`), positional arguments, an `--` end-of-options marker, and generated `--help`
text. Pure Java 17, no external libraries, no build tool required.

## Build & Run

```bash
JAVAC="/path/to/jdk/bin/javac"
JAVA="/path/to/jdk/bin/java"

# Compile the library
"$JAVAC" -d out $(find src/main/java -name "*.java")

# Compile the tests against the compiled library
"$JAVAC" -cp out -d out $(find src/test/java -name "*.java")

# Run the tests
"$JAVA" -cp out dev.kasapdev.argparser.ArgParserTest
```

On Windows, replace `$(find ... -name "*.java")` with an explicit file list, or run the
`find` substitution from Git Bash / WSL.

## Usage

```java
import dev.kasapdev.argparser.ArgParser;
import dev.kasapdev.argparser.ParsedArgs;
import dev.kasapdev.argparser.ArgParseException;

public class Example {
    public static void main(String[] args) {
        ArgParser parser = new ArgParser()
                .addFlag("verbose", "v", "Enable verbose output")
                .addOption("output", "o", "Output file path", "out.txt")
                .addOption("format", "Output format (json|csv)", "json");

        try {
            ParsedArgs parsed = parser.parse(args);

            if (parsed.flag("verbose")) {
                System.out.println("Verbose mode on");
            }
            System.out.println("Output file: " + parsed.option("output"));
            System.out.println("Format: " + parsed.option("format"));
            System.out.println("Positional args: " + parsed.positionals());
        } catch (ArgParseException e) {
            System.err.println("Error: " + e.getMessage());
            System.err.println(parser.helpText());
        }
    }
}
```

Running with `input.csv --verbose -o result.json` yields `verbose=true`,
`output=result.json`, `format=json` (default), and `positionals=["input.csv"]`.

Here's a second example showing a required-looking option enforced via `--`,
positionals, and error handling together — a `grep`-ish tool that takes a
pattern and files:

```java
import dev.kasapdev.argparser.ArgParser;
import dev.kasapdev.argparser.ParsedArgs;
import dev.kasapdev.argparser.ArgParseException;
import java.util.List;

public class Search {
    public static void main(String[] args) {
        ArgParser parser = new ArgParser()
                .addFlag("ignore-case", "i", "Case-insensitive match")
                .addFlag("count", "c", "Print only the match count")
                .addOption("context", "C", "Lines of context around each match", "0");

        try {
            ParsedArgs parsed = parser.parse(args);
            List<String> files = parsed.positionals(); // e.g. pattern + file names
            System.out.println("Pattern/files: " + files);
            System.out.println("Ignore case: " + parsed.flag("ignore-case"));
            System.out.println("Context lines: " + parsed.option("context"));
        } catch (ArgParseException e) {
            System.err.println("Error: " + e.getMessage());
            System.err.println(parser.helpText());
        }
    }
}
```

Running with `-i -C 2 TODO src/Main.java` yields `ignore-case=true`,
`context=2`, and `positionals=["TODO", "src/Main.java"]`.

## Subcommands

Register named subcommands with `ArgParser.subcommand(String)`. Each one
returns its own independent `ArgParser`, configured with the same
`addFlag`/`addOption` builder methods as the top-level parser — so two
subcommands can reuse the same option name with completely different
meanings, with no risk of cross-contamination. Parsing dispatches on the
first argument: if it matches a registered subcommand name, every remaining
argument is parsed by that subcommand's own parser; a name that matches no
subcommand throws `ArgParseException`.

```java
import dev.kasapdev.argparser.ArgParser;
import dev.kasapdev.argparser.ParsedArgs;
import dev.kasapdev.argparser.ArgParseException;

public class Deployer {
    public static void main(String[] args) {
        ArgParser parser = new ArgParser();

        parser.subcommand("deploy")
                .addOption("env", "e", "Target environment", "staging")
                .addOption("port", "p", "Target port to deploy to", "8080")
                .addFlag("force", "f", "Skip the confirmation prompt");

        parser.subcommand("rollback")
                .addOption("env", "e", "Target environment", "staging")
                .addOption("to-version", "Version to roll back to", "previous");

        try {
            ParsedArgs parsed = parser.parse(args);

            switch (parsed.subcommandName()) {
                case "deploy" -> {
                    ParsedArgs deploy = parsed.subcommand();
                    System.out.println("Deploying to " + deploy.option("env")
                            + " on port " + deploy.option("port")
                            + (deploy.flag("force") ? " (forced)" : ""));
                }
                case "rollback" -> {
                    ParsedArgs rollback = parsed.subcommand();
                    System.out.println("Rolling back " + rollback.option("env")
                            + " to " + rollback.option("to-version"));
                }
                default -> {
                    System.err.println("Usage: deploy|rollback [options]");
                }
            }
        } catch (ArgParseException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
```

Running with `deploy --env=prod --port=9090 --force` yields
`subcommandName()=="deploy"` and a subcommand result with
`env=prod`, `port=9090`, `force=true`. Running with
`rollback --env=prod --to-version=1.4.2` yields `subcommandName()=="rollback"`
and a subcommand result with `env=prod`, `to-version=1.4.2` — parsed against
`rollback`'s own registered options, entirely independent of `deploy`'s.
Running with an unrecognized name, e.g. `launch`, throws `ArgParseException`
with a message naming the bad subcommand.

## API

### `ArgParser`

- `ArgParser addFlag(String name, String description)` / `addFlag(String name, String shortAlias, String description)`
  — registers a boolean flag.
- `ArgParser addOption(String name, String description, String defaultValue)` / `addOption(String name, String shortAlias, String description, String defaultValue)`
  — registers a value-taking option with a default.
- `ArgParser subcommand(String name)` — registers a subcommand under `name` and returns its
  own independent `ArgParser` to configure with `addFlag`/`addOption`.
- `ParsedArgs parse(String[] args)` — parses the given arguments; throws `ArgParseException`
  naming the offending token on an unrecognized `--flag`/`-f`, a missing option value, or
  (when subcommands are registered) an unrecognized subcommand name.
- `String helpText()` — generated help listing every registered option, its alias, and
  default value.

### `ParsedArgs`

- `boolean flag(String name)` — whether the flag was present.
- `String option(String name)` — the supplied value, or the registered default.
- `List<String> positionals()` — positional arguments, in order.
- `String subcommandName()` — the name of the invoked subcommand, or `null` if none was
  invoked.
- `ParsedArgs subcommand()` — the invoked subcommand's own, independently-parsed result, or
  `null` if none was invoked.

## License

MIT — see [LICENSE](LICENSE).

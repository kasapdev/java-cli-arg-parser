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

## API

### `ArgParser`

- `ArgParser addFlag(String name, String description)` / `addFlag(String name, String shortAlias, String description)`
  — registers a boolean flag.
- `ArgParser addOption(String name, String description, String defaultValue)` / `addOption(String name, String shortAlias, String description, String defaultValue)`
  — registers a value-taking option with a default.
- `ParsedArgs parse(String[] args)` — parses the given arguments; throws `ArgParseException`
  naming the offending token on an unrecognized `--flag`/`-f` or a missing option value.
- `String helpText()` — generated help listing every registered option, its alias, and
  default value.

### `ParsedArgs`

- `boolean flag(String name)` — whether the flag was present.
- `String option(String name)` — the supplied value, or the registered default.
- `List<String> positionals()` — positional arguments, in order.

## License

MIT — see [LICENSE](LICENSE).

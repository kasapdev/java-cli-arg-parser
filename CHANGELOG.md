# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [1.2.0] - 2026-09-07

### Added

- Subcommand support on `ArgParser`:
  - `ArgParser subcommand(String name)` registers a named subcommand and
    returns its own independent `ArgParser`, configured with the same
    `addFlag`/`addOption` builder methods as the top-level parser.
  - `ArgParser.parse(String[])` now dispatches on the first argument when
    one or more subcommands are registered: a match parses the remaining
    arguments against that subcommand's own parser; an unrecognized name
    throws `ArgParseException`.
  - `ParsedArgs.subcommandName()` and `ParsedArgs.subcommand()` expose the
    invoked subcommand's name and its own, independently-parsed result.
  - A parser with no registered subcommands is completely unaffected —
    the first argument is parsed as an ordinary flag/option/positional,
    exactly as before.
- Tests proving two subcommands that register an overlapping option name
  (`--port`) with different shapes — a value-taking option on one, a
  boolean flag on the other — parse independently with no
  cross-contamination between their registrations, plus a test for the
  unrecognized-subcommand error case.
- A `## Subcommands` section in the README with a runnable
  deploy/rollback example, and a second worked example added to
  `## Usage`.

## [1.1.0] - 2026-09-06

### Added

- Test coverage for documented-but-untested `ArgParser`/`ParsedArgs` edge
  cases:
  - `ParsedArgs.flag()`/`option()` for a name that was never registered
    return `false`/`null` respectively, as documented.
  - An option registered with a `null` default resolves to `null` (not
    the string `"null"`) when omitted from the command line.
  - Registering a short alias already claimed by a different option is
    rejected with `IllegalArgumentException`.
  - A short boolean flag combined with an inline `=value` (e.g.
    `-v=true`) throws `ArgParseException`, mirroring the existing
    long-flag behavior.
  - A short-alias option missing its value at the end of the argument
    list throws `ArgParseException`, mirroring the existing long-option
    behavior.
  - A lone `-` token is treated as a positional argument rather than an
    unknown flag.

No behavioral changes were needed — all new edge-case tests passed against
the existing implementation.

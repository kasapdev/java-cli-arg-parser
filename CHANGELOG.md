# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

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

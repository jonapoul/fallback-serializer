# CLAUDE.md

Kotlin compiler plugin that generates kotlinx.serialization serializers for enums with a `@Fallback` entry. README.md
has the user-facing behaviour.

## Modules

- `runtime` - KMP. `@Fallback` and `FallbackEnumSerializer`, the base class of the generated serializers.
- `compiler` - K2 plugin. FIR generates the nested `FallbackSerializer` object and reports misuse, IR fills in its
  constructor.
- `gradle-plugin` - applies the compiler plugin and adds the `runtime` dependency.
- `compiler-tests` - tests on JetBrains' compiler test framework.
- `build-logic` - `fallback.convention`: JDK toolchain from `.java-version`, warnings as errors, and detekt with
  `config/detekt.yml` (adds `detektCheck`).

`PLUGIN_ID`, `GROUP` and `VERSION_NAME` live in the root `gradle.properties` and reach the code through buildconfig.

## Commands

```bash
./gradlew :compiler-tests:test
./gradlew :compiler-tests:generateTests            # after adding or removing a test data file
./gradlew :compiler-tests:test -PupdateTestData    # rewrite expected diagnostics
./gradlew :runtime:jvmTest
./gradlew -p build-logic check
./gradlew detektCheck
scripts/ktfmt.sh                                   # format files changed since main
```

## Tests

- Test function names use backticks with spaced words, e.g. `` fun `decodes by name`() ``.

## Compiler tests

- `compiler-tests/src/test/data/box` - each file is compiled with our plugin and kotlinx.serialization's, then its
  `box()` runs and must return `"OK"`. `kotlin.test` assertions are available.
- `compiler-tests/src/test/data/diagnostic` - frontend only. Diagnostics are checked against the inline
  `<!NAME!>...<!>` markup and the neighbouring `.diag.txt`.
- The suites in `compiler-tests/src/test/java` are generated and checked in. Don't edit them by hand.
- The Kotlin version is pinned: the plugin and the test framework must match `kotlin` in `gradle/libs.versions.toml`.

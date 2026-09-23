# CLAUDE.md

Kotlin compiler plugin that generates kotlinx.serialization serializers for enums with a `@Fallback` entry. README.md
has the user-facing behaviour.

## Modules

- `runtime` - KMP. `@Fallback` and `FallbackEnumSerializer`, the base class of the generated serializers.
- `compiler` - K2 plugin. FIR generates the nested `$serializer` object and reports misuse, IR fills in its
  constructor.
- `gradle-plugin` - applies the compiler plugin and adds the `runtime` dependency.
- `compiler-tests` - tests on JetBrains' compiler test framework.
- `build-logic` - `fallback.convention`: JDK toolchain from `.java-version`, warnings as errors, and detekt with
  `config/detekt.yml` (adds `detektCheck`).
- `sample` - separate build that applies the Gradle plugin from this build via `includeBuild("..")`, then runs tests on
  jvm, js, wasmJs and linuxX64. Checks the generated code on every backend.

`PLUGIN_ID`, `GROUP` and `VERSION_NAME` live in the root `gradle.properties` and reach the code through buildconfig.

## Commands

```bash
./gradlew :compiler-tests:test
./gradlew :compiler-tests:generateTests            # after adding or removing a test data file
./gradlew :compiler-tests:test -PupdateTestData    # rewrite expected diagnostics
./gradlew :runtime:jvmTest
./gradlew :gradle-plugin:test                      # TestKit scenarios, using blueprint's test framework
./gradlew -p build-logic check
./gradlew -p sample check                          # end-to-end tests on every backend
./gradlew :compiler-tests:test -Pfallback.testKotlinVersion=2.4.0   # also works with -p sample check
./gradlew detektCheck
scripts/ktfmt.sh                                   # format files changed since main
```

## Tests

- Test function names use backticks with spaced words, e.g. `` fun `decodes by name`() ``.

## Compiler tests

- `compiler-tests/src/test/data/box` - each file is compiled with our plugin and kotlinx.serialization's, then its
  `box()` runs and must return `"OK"`. `kotlin.test` assertions are available.
- `compiler-tests/src/test/data/diagnostic` - frontend only. Diagnostics are checked against the inline
  `<!NAME!>...<!>` markup and the neighbouring `.diag.txt`. The `.diag.txt` is only checked on the pinned Kotlin
  version.
- The suites in `compiler-tests/src/test/java` are generated and checked in. Don't edit them by hand.
- The build uses `kotlin` in `gradle/libs.versions.toml`. `KOTLIN_VERSIONS` in `gradle.properties` lists the versions
  that CI tests and the Gradle plugin accepts. Stick to compiler APIs that exist in all of them, and avoid inline
  compiler helpers, since their bodies get copied into the plugin.
- `KOTLIN_PRERELEASE` in `gradle.properties` is the latest Kotlin Beta or RC, bumped by Renovate. CI tests it in a
  non-blocking job, skipped once it's in `KOTLIN_VERSIONS`.
- `.github/workflows/kotlin-dev.yml` tests the latest Kotlin dev build daily and opens an issue when it fails. Dev
  versions resolve from JetBrains' bootstrap repo, e.g. `-Pfallback.testKotlinVersion=2.5.0-dev-7359`.

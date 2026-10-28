# CardWiki Backend

## Requirements

Use JDK 11 for Maven and for the project runtime in your IDE. Check the runtime
with `mvn -version`; setting `java.version` in the POM does not select the JDK
used to launch Maven.

On macOS, select an installed JDK 11 for the current terminal with:

```sh
export JAVA_HOME=$(/usr/libexec/java_home -v 11)
export PATH="$JAVA_HOME/bin:$PATH"
```

On Windows or Linux, set `JAVA_HOME` to your own JDK 11 installation and add its
`bin` directory to `PATH`. Reopen the terminal and verify `mvn -version`.
No database server installation is needed: the application uses a local H2 file.

## VS Code troubleshooting

Install the Java extensions and use `Java: Configure Java Runtime` to select
your JDK 11 for this Maven project. The JDK running the language server can be
different from the project's JDK; do not force the language server itself to
run on Java 11. Keep machine-specific JDK paths in local settings.

If Java extension 1.57.0 reports `Flexible Constructor Bodies` on Lombok
annotations, use **Language Support for Java(TM) by Red Hat 1.56.0** as a
workaround, as described in the [upstream issue](https://github.com/redhat-developer/vscode-java/issues/4521).
Avoid automatically updating that extension to the affected version.

If Maven succeeds but VS Code reports unresolved imports in
`target/generated-sources/annotations`, open the Command Palette, run
`Java: Force Java Compilation`, and select `Full`. Wait for `Java: Ready`.
This rebuilds the IDE's classes and diagnostics after Maven regenerates files.
Do not edit generated mapper implementations directly.

If Maven tests fail with `NoClassDefFoundError: LImageMapper;` in a generated
`UserMapperImpl`, or JMH reports `Unresolved compilation problems`, the editor
may have overwritten Maven's generated classes while the build was running.
Disable **Java: Autobuild Enabled** in this workspace's VS Code settings
(`"java.autobuild.enabled": false`), then rerun `mvn clean package` with JDK 11.
This leaves compilation to Maven; do not force IDE compilation during the
Maven build. If VS Code has not applied the setting, reload its window before
rebuilding. Keep this preference in the local, ignored `.vscode/settings.json`.

## How to run it

Run these commands from `backend/`.

### Start the backend

	mvn spring-boot:run

### Start the backend with test data

If the database is not clean, the test data won't be inserted

	mvn spring-boot:run -Dspring-boot.run.profiles=dev

On macOS/Linux, `./run-dev.sh` is a shortcut for the same command.
Open the frontend at `http://localhost:4200` and choose **Test User** or
**Test Admin** on the login page. External OAuth providers require separate
credentials when used outside this development login flow.

### Run the tests

	mvn test

If build artifacts are stale, run `mvn clean test`. If VS Code subsequently
shows unresolved imports in generated sources, use the full IDE compilation
described above.

## JMH benchmarks

Microbenchmarks live in the separate [`benchmarks` module](../benchmarks/README.md).
Build them from the repository root with `mvn clean package` using JDK 11.
The backend also produces a `plain` JAR for that module; JMH dependencies and
benchmark sources stay outside the application module.

For other backend analyses, save outputs in the tool's directory under
`sse-reports/baseline/` or `sse-reports/sse-improvements/`, according to the
application version measured. Follow the [report guide](../sse-reports/README.md)
and the [branch workflow](../README.md#branches-and-improvements): each
optimization is developed on a temporary branch from `master`, while `baseline`
keeps the application without SSE optimizations.

## Deployment

Configure `src/main/resources/application-prod.yml` and run it with `-Dspring-boot.run.profiles=prod`.

To initially give your user admin rights run:

	mvn spring-boot:run -Dspring-boot.run.profiles=makeAdmin -Dspring-boot.run.arguments=--admin-username=<username>

# CardWiki

![CardWiki logo](frontend/src/assets/logo.png)

A flashcard learning web app where decks can be edited collaboratively (like in a wiki).

![Backend Tests](https://github.com/cardwiki/cardwiki/workflows/Backend%20Tests/badge.svg)
![Frontend Tests](https://github.com/cardwiki/cardwiki/workflows/Frontend%20Tests/badge.svg)
![Backend code coverage](https://codecov.io/gh/cardwiki/cardwiki/branch/master/graph/badge.svg)

## Features

* **Learn with spaced-repetition** (our algorithm is inspired by Anki & SuperMemo2)
* **Markdown, LaTeX, images**
* **Share & collaborate**: Decks are public and can be edited by anyone.
* **History**: Revisions can be easily reviewed and reverted.
* **Organize** cards into categories.
* **Remix**: both decks and cards can be easily copied.
* **No password**: Login with GitHub, GitLab or Google

## Setup

The backend is written in Java with [Spring Boot](https://spring.io/projects/spring-boot) and the [Hibernate ORM](https://hibernate.org/orm/). The frontend is written in TypeScript with [Angular](https://angular.io/).

For setup instructions refer to the READMEs in [backend](backend/README.md) and [frontend](frontend/README.md).

### Local development after cloning

Install **JDK 11**, **Maven**, and **Node.js 14.21.3 with npm 6.14.18**.
The `.nvmrc` pins the Node version used for local setup to the Node 14 series
already used by the deployment workflow. With nvm installed, run `nvm install`
and `nvm use` from the repository root. These are legacy project versions;
updating the application stack is a separate task.

Check `mvn -version`: it must report **Java 11**. Configure `JAVA_HOME` on your
machine if it selects a newer JDK; see the [backend setup](backend/README.md).

In one terminal, start the backend:

```sh
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

In another terminal, install and start the frontend:

```sh
cd frontend
npm ci
npm start
```

Open **http://localhost:4200**. The backend runs on **http://localhost:8080**.
The `dev` profile creates sample data in an empty local H2 database and exposes
**Test User** and **Test Admin** login options, so local development needs no
external database or OAuth credentials. Stop each server with `Ctrl+C`.

VS Code settings in `.vscode/` are local and ignored by Git. Each developer must
select their own JDK 11 installation. See [VS Code troubleshooting](backend/README.md#vs-code-troubleshooting)
for extension compatibility and generated-source diagnostics.

## JMH benchmarks

The root Maven project builds `backend` and the separate `benchmarks` module.
With JDK 11 selected, run from the repository root:

```sh
mvn clean package
java -jar benchmarks/target/benchmarks.jar
```

See the [benchmark guide](benchmarks/README.md) for a quick smoke run, benchmark
selection, and JSON results.

## Contributing

Issues and pull requests are welcome!

For contributing code, check out our [contributions guide](CONTRIBUTING.md).

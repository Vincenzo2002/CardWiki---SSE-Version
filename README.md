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

## Sustainable Software Engineering (SSE)

Frontend, backend e strumenti di misura rimangono nella stessa repository:

```text
frontend/                  Applicazione Angular
backend/                   Applicazione Spring Boot
benchmarks/                Benchmark JMH già configurati
jmeter/                    Piani di carico e dati di test
energibridge/              Script per le misurazioni energetiche
sse-reports/
  baseline/                Risultati prima delle ottimizzazioni
  sse-improvements/        Risultati dopo le ottimizzazioni
```

### Branch e miglioramenti

I branch permanenti sono `master` e `baseline`:

* `baseline` conserva l'applicazione senza ottimizzazioni SSE. Può ricevere
  nuovi test, strumenti di misura e risultati iniziali.
* `master` raccoglie i miglioramenti accettati e i risultati dei confronti.

Per ogni miglioramento creare un branch da `master`, per esempio
`perf/category-query`. Dopo modifica, test e misurazione, aprire una pull request
verso `master`. Eliminare il branch di lavoro dopo il merge.
Non serve un branch permanente `sse-improvements`: il nome della cartella dei
report identifica la versione misurata.

Se manca un test, aggiungerlo a `baseline`, eseguirlo e salvarne il risultato
in `sse-reports/baseline/`. Poi, da un branch di lavoro creato da `master`,
integrare `baseline` con `git merge baseline`, controllare eventuali conflitti
ed eseguire lo stesso test sulla versione corrente. Portare test e risultati
in `master` tramite pull request. Evitare il merge di `master` in `baseline`,
perché porterebbe anche le ottimizzazioni nella versione di riferimento.

### Tool e risultati

| Tool | Sorgenti o configurazioni | Cartella dei risultati per ciascuna versione |
| --- | --- | --- |
| JMH | [benchmarks/](benchmarks/README.md) | `jmh/` |
| JMeter | [jmeter/](jmeter/README.md) | `jmeter/` |
| EnergiBridge | [energibridge/](energibridge/README.md) | `energibridge/` |
| Creedengo tramite SonarQube | Configurazione da aggiungere quando usato | `creedengo/` |
| WebsiteCarbon | Analisi esterna del frontend | `websitecarbon/` |
| EcoIndex | Analisi esterna del frontend | `ecoindex/` |
| GreenIT-Analysis | Analisi nel browser | `greenit-analysis/` |
| FOSSA | Configurazione da aggiungere quando usato | `fossa/` |
| GUIDO | Analisi del progetto e della collaborazione | `guido/` |

Le cartelle sono predisposte per questi strumenti; al momento è configurato
soltanto JMH. Non sono ancora presenti misurazioni SSE archiviate.

Salvare JSON, CSV, screenshot e brevi note nella cartella del tool sotto
`sse-reports/baseline/` oppure `sse-reports/sse-improvements/`.
Per ogni misura annotare data, commit eseguito, comando o procedura, ambiente,
dati e parametri usati. Usare condizioni equivalenti per confrontare le versioni.
Il confronto finale verrà riassunto qui, con i collegamenti ai risultati.

Vedere la [guida dei report](sse-reports/README.md) per esempi di archiviazione.

## Contributing

Issues and pull requests are welcome!

For contributing code, check out our [contributions guide](CONTRIBUTING.md).

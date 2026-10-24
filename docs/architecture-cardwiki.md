# CardWiki Architecture Analysis

This document describes the original `cardwiki/cardwiki` application at commit
`bbe4f0d2dd423aef69a8106df9513deb54276b0a`. All source references point to that version.

## 1. Project Overview and Purpose

CardWiki is a web application for creating, sharing, and studying flashcards using spaced repetition. Decks are public; registered users can edit their cards and categories, browse revision history, and restore previous content. Other features include comments, favorites, copying decks and cards, CSV import/export, profiles, and user administration. Content supports Markdown text, formulas rendered with KaTeX, and images. [Project overview](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/README.md).

| Area | Declared and used technologies |
|---|---|
| Backend | Java 11, Spring Boot 2.2.7.RELEASE, Spring MVC, Spring Data JPA/Hibernate, Spring Security 5.3.2.RELEASE |
| Authentication | OAuth 2.0/OpenID Connect; JWT through JJWT 0.11.1 |
| Persistence | File-based H2, Liquibase 4.0.0; images stored on the filesystem |
| Mapping and validation | MapStruct 1.3.1.Final, Lombok 1.18.12, Bean Validation |
| Frontend | Angular 9.1.1, TypeScript 3.8.3, RxJS 6.5.5; Bootstrap 4.4.1 and ng-bootstrap 6.0.2 |
| Content rendering | markdown-it, markdown-it-texmath, KaTeX; diff-match-patch for comparisons |
| Build and quality | Maven, Angular CLI/npm, GitHub Actions, Spring Boot Test/JUnit, JaCoCo/Codecov, Cypress, ESLint, and Prettier |

Sources: [pom.xml](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/backend/pom.xml), [package.json](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/frontend/package.json), [backend workflow](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/.github/workflows/maven.yml), and [frontend workflow](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/.github/workflows/node.js.yml).

Main structure, abbreviating the Java path prefix `at/ac/tuwien/sepm/groupphase/backend/`:

```text
cardwiki/
├── backend/
│   ├── pom.xml, mvnw, run-dev.sh
│   ├── src/main/java/.../backend/
│   │   ├── BackendApplication.java
│   │   ├── config/security/          # OAuth, JWT, filters
│   │   ├── endpoint/                # REST, dto/, mapper/, exceptionhandler/
│   │   ├── service/impl/            # use cases and application rules
│   │   ├── repository/              # Spring Data, JPQL, and native SQL
│   │   ├── entity/                  # JPA model
│   │   ├── validation/              # custom constraints
│   │   ├── exception/
│   │   └── profiles/                # dev, datagenerator, makeadmin
│   ├── src/main/resources/
│   │   ├── application*.yml
│   │   └── db/migration/            # changelogs 0.0, 0.1, and 0.2
│   ├── src/test/                    # unit and integration tests
│   └── images/                     # default upload destination
├── frontend/
│   ├── src/app/
│   │   ├── components/              # deck, card, category, learn, profiles...
│   │   ├── services/, dtos/
│   │   ├── guards/, interceptors/, directives/, pipes/
│   │   └── app.module.ts, app-routing.module.ts
│   ├── src/environments/
│   └── cypress/integration/e2e/     # core and login
├── .github/workflows/
├── production.md
└── cardwiki.service
```

## 2. System Architecture and Design Patterns

**Style:** a client–server system with an Angular SPA and a **layered backend monolith**. The backend and frontend have separate builds. The deployment guide describes static files served through NGINX and a single JVM JAR managed by systemd. Internally, the application is organized into technical layers rather than independent domain modules. [Documented deployment](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/production.md).

The usual request path is `Endpoint → Service → Repository → Hibernate → H2`. Controllers validate DTOs, invoke services, and convert entities into response DTOs; services coordinate business rules, the current user, and transactions; repositories provide data access. JPA entities cross service boundaries, so the domain remains coupled to persistence. In addition, `SimpleLearnService` directly uses the endpoint's `AttemptInputDto`, and `DeckService` exposes HTTP-related types such as `MultipartFile`: the boundaries are practical but not independent of the transport layer. [CardEndpoint](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/backend/src/main/java/at/ac/tuwien/sepm/groupphase/backend/endpoint/CardEndpoint.java), [SimpleCardService](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/backend/src/main/java/at/ac/tuwien/sepm/groupphase/backend/service/impl/SimpleCardService.java), [DeckService](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/backend/src/main/java/at/ac/tuwien/sepm/groupphase/backend/service/DeckService.java).

| Pattern/mechanism | Concrete implementation |
|---|---|
| Service Layer and Dependency Injection | `CardService`, `DeckService`, and `LearnService` interfaces with `@Service` implementations; dependencies are mostly injected through constructors |
| Repository | `JpaRepository` interfaces, derived queries, `@Query`, and `@EntityGraph` |
| DTO / Data Mapper | DTOs separate from entities; MapStruct mappers generated as Spring components |
| Filter chain / Interceptor | `TokenAuthenticationFilter` runs before the username/password filter; `AuthInterceptor` adds the Bearer token in the browser |
| Observer | `BehaviorSubject` and `Observable` for roles, username, and clipboard state |
| Extensible providers | `OAuth2ProviderSupplier` and a list of implementations injected into `AuthEndpoint` |
| Snapshot versioning | JPA hierarchy `Revision → RevisionEdit → RevisionCreate`, plus `RevisionDelete`; an explicit reference identifies the current revision |

Revision history applies to cards; the rest of the system stores mutable entities. Restoring content creates a new edit containing the old content. The application does not reconstruct its entire state by replaying events. Sources: [Revision hierarchy](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/backend/src/main/java/at/ac/tuwien/sepm/groupphase/backend/entity/Revision.java), [Card](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/backend/src/main/java/at/ac/tuwien/sepm/groupphase/backend/entity/Card.java), [Frontend restore operation](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/frontend/src/app/components/card/card-history/card-history.component.ts#L63-L81), [AuthEndpoint](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/backend/src/main/java/at/ac/tuwien/sepm/groupphase/backend/endpoint/AuthEndpoint.java), [AuthService](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/frontend/src/app/services/auth.service.ts).

## 3. Data Flow and Control Flow

**Entry points.** `BackendApplication.main()` starts Spring Boot; `frontend/src/main.ts` bootstraps `AppModule`, with routes defined in `AppRoutingModule`. Profiles provide data initialization, development mock logins, and administrative promotion. The main CLI is used to build and start the application rather than providing a separate application interface. [Backend startup](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/backend/src/main/java/at/ac/tuwien/sepm/groupphase/backend/BackendApplication.java), [Frontend startup](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/frontend/src/main.ts), [UI routes](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/frontend/src/app/app-routing.module.ts).

Main APIs, all under `/api/v1`:

| Responsibility | Routes and operations |
|---|---|
| Decks | `GET/POST /decks`, `GET/PATCH/DELETE /decks/{id}`, `POST /decks/{id}/copy` |
| Cards | `GET/POST /decks/{id}/cards`, `GET/PATCH /cards/{id}`; `POST /cards/{id}` records a soft deletion; `DELETE` performs physical deletion |
| History | `GET /cards/{id}/revisions`, `/decks/{id}/revisions`, `/revisions`, `/revisions/byid?id=...` |
| Learning | `GET /learn/next?deckId=...&reverse=...`; `POST /learn/attempt`; `/decks/progress` and progress reset |
| Identity | `/auth/providers`, `/auth/providers/{id}`, `/auth/whoami`; registration through `POST /users` |
| Other features | `/categories`, `/users`, `/users/{id}/favorites/...`, `/comments`, `POST /images` |

The same `POST /decks/{id}/cards` endpoint accepts JSON for a single card or multipart input for CSV import. `GET /decks/{id}` with `text/csv` content negotiation exports two text columns, without images or history. Pagination uses `limit` and `offset`, but **`offset` is the zero-based page number**; the default page size is 10, with a maximum of 100. [DeckEndpoint](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/backend/src/main/java/at/ac/tuwien/sepm/groupphase/backend/endpoint/DeckEndpoint.java), [CardEndpoint](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/backend/src/main/java/at/ac/tuwien/sepm/groupphase/backend/endpoint/CardEndpoint.java), [Frontend Pageable](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/frontend/src/app/dtos/pageable.ts), [Pagination configuration](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/backend/src/main/resources/application.yml#L29-L35).

**Editing a card.** The form passes a `CardUpdate` to the Angular service; image references become filenames in the JSON payload. The filter validates the JWT and retrieves roles from the database; `@Secured("ROLE_USER")` protects the edit operation. `RevisionInputDto` applies length limits, MapStruct creates a new `RevisionEdit`, and `SimpleCardService` checks that both sides contain text or an image and that the referenced images exist. A transaction sets the author, card reference, and `latestRevision`, then saves the change and returns a DTO. Although named `PATCH`, this operation requires a valid new snapshot of both sides rather than applying a diff or a partial merge. [Angular service](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/frontend/src/app/services/card.service.ts), [Backend edit operation](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/backend/src/main/java/at/ac/tuwien/sepm/groupphase/backend/service/impl/SimpleCardService.java#L130-L159).

**Learning flow and algorithm.** The SQL selection joins cards, their current revisions, and the user's progress for the requested learning direction. It includes new or due cards, excludes those with a `RevisionDelete`, and sorts new cards first, followed by status and due date. The browser requests one card at a time; after a rating, it submits an attempt and requests the next card. [Selection query](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/backend/src/main/java/at/ac/tuwien/sepm/groupphase/backend/repository/CardRepository.java#L68-L99), [UI flow](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/frontend/src/app/components/learn-deck/learn-deck.component.ts).

`SimpleLearnService` implements a two-state machine with ratings `AGAIN`, `GOOD`, and `EASY`. The initial easiness factor is 2.5. In `LEARNING`, `AGAIN` schedules the card one minute later, `GOOD` advances to ten minutes and then to one day in `REVIEWING`, and `EASY` advances to four days. In `REVIEWING`, `GOOD` multiplies the interval by the factor; `EASY` increases the factor by 0.15 and applies a 1.3 bonus; `AGAIN` reduces the factor by 0.2, with a minimum of 1.3, and reduces the interval to 20%, **remaining in REVIEWING**. Intervals are rounded up. No scheduler sends review reminders: the due date is checked when the query runs. [Complete algorithm](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/backend/src/main/java/at/ac/tuwien/sepm/groupphase/backend/service/impl/SimpleLearnService.java#L56-L135).

**Authentication and state.** OAuth/OIDC login produces an HS512 JWT with subject `provider:identity` and a default lifetime of twelve hours. The backend delivers it in a cookie readable by the frontend; `LoginComponent` copies it to local storage, and the interceptor uses it in the `Authorization` header. The temporary OAuth request is stored in an `HttpOnly` cookie for 120 seconds. The backend is configured as `STATELESS`, but it reloads account status and privileges from the database for each request carrying a token. The frontend stores state in components, singleton RxJS services, and `localStorage`, without a dedicated global store. [Security configuration](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/backend/src/main/java/at/ac/tuwien/sepm/groupphase/backend/config/security/SecurityConfig.java), [JWT filter](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/backend/src/main/java/at/ac/tuwien/sepm/groupphase/backend/config/security/TokenAuthenticationFilter.java), [Login UI](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/frontend/src/app/components/login/login.component.ts), [OAuth cookie](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/backend/src/main/java/at/ac/tuwien/sepm/groupphase/backend/config/security/HttpCookieOAuth2AuthorizationRequestRepository.java).

**Concurrency.** The backend uses synchronous Spring MVC requests and JPA transactions in many services. There are no `@Version` annotations, explicit locks, or checks against the starting revision. `saveAttempt()` reads and saves progress without a single transaction covering the service operation. This creates a risk of lost updates during simultaneous changes; no load or concurrency test has been performed as part of this analysis. The clipboard submits multiple creation requests in parallel through `forkJoin`, without atomicity across requests. [Progress](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/backend/src/main/java/at/ac/tuwien/sepm/groupphase/backend/entity/Progress.java), [Clipboard](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/frontend/src/app/services/clipboard.service.ts).

## 4. Data Model and Persistence

| Entity | Structure and relationships |
|---|---|
| `User` | OAuth identity `authId`, username, description, admin/enabled/deleted flags; relationships with contributions, favorites, and progress records |
| `Deck` | Author and timestamps; one-to-many cards/comments; many-to-many categories; many-to-many users through `favorites` |
| `Card` | Deck, creation date, one-to-many revisions, and `latest_revision` reference; content is stored in the revision |
| `Revision` | Author, card, message, and date; `JOINED` inheritance with the `revisions`, `revision_edits`, `revision_create`, and `revision_delete` tables |
| `Category` | Parent/children hierarchy and a many-to-many relationship with decks through `category_deck` |
| `Progress` | Composite key `(card_id, reverse, user_id)`; easiness factor, interval, status, and due date |
| `Image` | Filename as primary key, author, and date; image bytes are stored outside the database |
| `Comment` | Message, author, deck, and timestamps; editable by its owner and deletable by its owner or an administrator |

The default datasource and the `prod` profile both use file-based H2 at `./database/db`. Liquibase manages the schema through three changelogs; Hibernate uses `ddl-auto: none` and `open-in-view: false`. Data needed by DTOs is loaded through `@EntityGraph`, transactions, or explicit initialization. Tests use in-memory H2 with `ddl-auto` set to `create`: the suite does not constitute an upgrade test against a persistent production database. [Base configuration](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/backend/src/main/resources/application.yml), [Production configuration](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/backend/src/main/resources/application-prod.yml), [Migrations](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/backend/src/main/resources/db/migration/db.changelog-master.xml), [Test configuration](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/backend/src/test/resources/application-test.yml).

**Integrity.** The schema defines foreign keys, required columns, unique usernames, authIds, and category names, and composite keys for many-to-many relationships and progress records. Foreign keys use `RESTRICT`; services and ORM cascades coordinate the deletion of dependent records. The application prevents category cycles through a recursive query and normalizes whitespace in names. The service validates both sides of a card; the database permits a null `latest_revision` and does not enforce that the referenced revision belongs to the same card. Progress status is stored as a numeric ordinal, making the enum order significant for native queries as well. [Initial schema](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/backend/src/main/resources/db/migration/db.changelog-0.0.xml), [Progress key](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/backend/src/main/resources/db/migration/db.changelog-0.1.xml), [Categories](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/backend/src/main/java/at/ac/tuwien/sepm/groupphase/backend/service/impl/CategoryServiceImpl.java).

**History and deletion.** Ordinary card deletion adds a `RevisionDelete`; card lists and learning selections only include cards whose current revision is a `RevisionEdit`, of which `RevisionCreate` is a subclass. Physical deletion removes the card, its revisions, and progress records. User deletion is logical: it disables the account, clears `authId`, sets the deleted flag, and removes progress records while retaining contributions. Copying a deck recreates cards and CREATE revisions from the current content without copying history or progress. [Card service](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/backend/src/main/java/at/ac/tuwien/sepm/groupphase/backend/service/impl/SimpleCardService.java), [Deck copy](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/backend/src/main/java/at/ac/tuwien/sepm/groupphase/backend/service/impl/SimpleDeckService.java#L119-L151), [User deletion](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/backend/src/main/java/at/ac/tuwien/sepm/groupphase/backend/service/impl/SimpleUserService.java#L179-L197).

**Images.** The maximum upload size is 2 MB; PNG/JPEG files are accepted based on their byte content. Filenames consist of the content's SHA-256 hash plus an extension; an image already registered is reused. `Files.write` stores the bytes before JPA metadata is saved; Spring MVC serves them under `/images/*`. This deduplication does not provide a distributed transaction between files and the database. [Image storage](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/backend/src/main/java/at/ac/tuwien/sepm/groupphase/backend/service/impl/SimpleImageService.java), [Static resource serving](https://github.com/cardwiki/cardwiki/blob/bbe4f0d2dd423aef69a8106df9513deb54276b0a/backend/src/main/java/at/ac/tuwien/sepm/groupphase/backend/config/ImageResourceConfiguration.java).

## 5. Application Boundaries

```text
Angular browser client
  components → HttpClient services → Bearer interceptor
                     │ JSON / multipart / CSV
                     ▼
Spring Security → REST endpoints → DTO / MapStruct
                                        │
                                        ▼
                              Application services
                         ┌──────────────┴─────────────┐
                         ▼                            ▼
                  JPA repositories               Image service
                         │                            │
                         ▼                            ▼
                  Hibernate → H2              Local filesystem

OAuth/OIDC: GitHub, GitLab, Google → Spring callback → JWT cookie
                                              → frontend localStorage
Images: Spring MVC ResourceHandler → filesystem → HTTP response
```

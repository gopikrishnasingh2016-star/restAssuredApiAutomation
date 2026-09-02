# REST Assured API Automation Framework

A modern, layered API test framework in **Java 21** using **REST Assured**, **TestNG** and
**Cucumber (BDD)**, exercising the public [reqres.in](https://reqres.in) REST API — full CRUD,
pagination, authentication and negative paths.

It applies the **Page Object Model to APIs**: every endpoint family is encapsulated in a service
object, so a test reads as business intent and no test ever contains a URL, a header or a timeout.

```
mvn test                          # full regression against SIT
mvn test -Denv=mock               # everything offline, no internet needed
mvn test -Dsuite=cucumber         # the BDD suite
```

---

## The four layers

A test only contains what is unique to its scenario. Everything else lives in one of four layers.

| # | Layer | Where | Responsibility |
|---|-------|-------|----------------|
| 1 | **Config** | `config/ConfigReader.java`, `src/test/resources/config/*.properties` | One properties file per environment, selected with `-Denv`. CI switches SIT → UAT with a parameter, never a code change. |
| 2 | **Token manager** | `auth/TokenManager.java` | Authenticates **once per suite**, caches the token, refreshes it only when it is inside the expiry skew. |
| 3 | **Spec layer** | `core/SpecFactory.java`, `base/BaseTest.java` | Builds the `RequestSpecification` (base URI, JSON content type, bearer header, correlation id, logging filters) and the `ResponseSpecification` every response must satisfy. |
| 4 | **Tests / services / features** | `services/`, `tests/`, `features/` | The scenario itself: a path parameter or body, plus the matchers that are unique to it. |

### 1. Config layer — one file per environment

```
src/test/resources/config/
├── config.properties        # shared defaults
├── config-sit.properties    # System Integration Test
├── config-uat.properties    # User Acceptance Test
├── config-live.properties   # public production API
└── config-mock.properties   # local WireMock stub server
```

`ConfigReader` resolves each key in this order, first match wins:

```
-Dbase.uri=…   >   BASE_URI env var   >   config-<env>.properties   >   config.properties
```

That precedence is what lets Jenkins inject credentials from its credential store
(`-Dauth.username=$CRED_USR -Dauth.password=$CRED_PSW`) without any secret being committed.

```bash
mvn test -Denv=uat                                   # UAT
mvn test -Denv=sit -Dsla.response.time.ms=3000       # SIT with a tighter SLA
ENV=mock BASE_URI=http://localhost:9999 mvn test     # env vars work too
```

### 2. Token manager — authenticate once, cache, refresh before expiry

`TokenManager` is a lock-guarded singleton. The first caller performs the auth round trip; every
later caller gets the cached `Token` until it is within `auth.token.refresh.skew.seconds` of
expiry, at which point exactly one thread re-authenticates. In a 20-thread suite that is **one**
call to the auth service instead of one per test.

Two grant types are supported out of the box:

| `auth.grant.type` | Request | Response fields read |
|---|---|---|
| `client_credentials` | form-encoded OAuth2 (`grant_type`, `client_id`, `client_secret`) | `access_token`, `expires_in` |
| `password_json` | JSON `{email, password}` | `token` (TTL from config) |

`Token.toString()` redacts the value, and `LoggingFilter` masks `password`, `client_secret`,
`access_token` and `token` in both headers and bodies — a token never reaches a log or a report.

### 3. Spec layer — built once in `@BeforeSuite`

```java
@BeforeSuite(alwaysRun = true)
public void prepareEnvironment() {
    TestEnvironment.start();          // starts WireMock when env=mock
    reqSpec = TestEnvironment.requestSpec();
    okSpec  = TestEnvironment.okSpec();
}
```

`SpecFactory` assembles the request spec from: base URI and base path from config, JSON content
type and accept, the API key header, `Authorization: Bearer …` from the `TokenManager`, an
`X-Correlation-ID` per request for server-side tracing, connect/read timeouts, and the logging +
Allure filters. `okSpec` carries status `200`, JSON content type and the response-time SLA.

### 4. Tests — only what is unique to the scenario

Specification style, for thin contract checks:

```java
given()
    .spec(reqSpec)
    .pathParam("id", 2)
.when()
    .get(Endpoints.USER_BY_ID)
.then()
    .spec(okSpec)
    .body("data.id", equalTo(2));
```

Service-object style (Page Object Model for APIs), for anything with business meaning:

```java
Response response = users.getUser(2);

assertThat(response)
    .hasStatusCode(StatusCode.OK)
    .matchesSchema("schemas/single-user.json")
    .hasField("data.email", "janet.weaver@reqres.in");
```

| UI Page Object Model | This framework |
|---|---|
| `BasePage` | `BaseService` — verbs, specs, path/query parameters |
| `LoginPage`, `UsersPage` | `AuthService`, `UserService`, `ResourceService` |
| Locators | `constants/Endpoints.java` |
| Page actions | `getUser(id)`, `createUser(payload)`, `login(credentials)` |
| `BaseTest` | `BaseTest` — suite fixture and spec layer |

---

## Project layout

```
src/main/java/com/apiautomation/
├── config/        ConfigReader, Environment          ← layer 1
├── auth/          TokenManager, Token                ← layer 2
├── core/          SpecFactory                        ← layer 3
│   └── filters/   LoggingFilter, CorrelationIdFilter
├── services/      BaseService, UserService, AuthService, ResourceService
├── models/        request/ + response/ records (Jackson)
├── assertions/    ApiAssertions, ResponseAssert (fluent, AssertJ-based)
├── constants/     Endpoints, StatusCode
├── listeners/     TestListener, RetryAnalyzer, RetryTransformer
└── utils/         JsonUtils, TestDataFactory (Datafaker)

src/test/java/com/apiautomation/
├── base/          BaseTest — @BeforeSuite spec layer
├── support/       TestEnvironment, MockApiServer, TestData
├── tests/         users/, auth/, resources/, spec/     ← TestNG tests
└── bdd/           context/, steps/, runner/            ← Cucumber glue

src/test/resources/
├── config/        config.properties + one file per environment
├── features/      users.feature, authentication.feature
├── schemas/       JSON Schema contracts
├── suites/        testng.xml, smoke.xml, negative.xml, cucumber.xml
├── testdata/      data-driven cases
└── wiremock/      stub mappings for the offline environment
```

---

## Running

| Command | What it does |
|---|---|
| `mvn test` | Full regression against SIT |
| `mvn test -Denv=mock` | Everything against the local stub server — no internet |
| `mvn test -Dsuite=smoke` | Smoke suite (deployment gate) |
| `mvn test -Dsuite=negative` | Contract-violation checks only |
| `mvn test -Dsuite=cucumber` | BDD suite, all scenarios |
| `mvn test -Dsuite=cucumber -Dtags="@smoke and @users"` | BDD suite, filtered by tag |
| `mvn test -Denv=uat -Dthreads=8` | UAT, eight parallel threads |
| `mvn test -Dretry.count=1` | Grant one retry to failing tests |

Reports:

```bash
allure serve target/allure-results        # rich HTML report (requires the Allure CLI)
open target/cucumber-reports/cucumber.html
tail -f target/logs/api-tests.log
```

### Offline / mock mode

`-Denv=mock` starts WireMock on a free port from `src/test/resources/wiremock/mappings` and points
`base.uri` at it. The stubs mirror the live contract, including the `x-api-key` header requirement
and an OAuth2 token endpoint, so the same tests, schemas and assertions run unchanged. If a test
passes in `mock` but fails in `sit`, the defect is in the service, not in the suite.

---

## BDD layer

```gherkin
@api @users
Feature: User directory

  Background:
    Given the users API is reachable

  @smoke
  Scenario: Read a user that exists
    When I request the user with id 2
    Then the response status code should be 200
    And the response should match the "single-user" schema
    And the returned user should have the email "janet.weaver@reqres.in"
```

- **Features** — `src/test/resources/features/`
- **Step definitions** — `bdd/steps/` (`UserSteps`, `AuthSteps`, `CommonSteps`, `Hooks`)
- **Runner** — `bdd/runner/CucumberTestNgRunner`, a TestNG `AbstractTestNGCucumberTests` with
  parallel scenarios
- **Shared state** — `ScenarioContext`, injected by PicoContainer, one instance per scenario, so
  parallel scenarios never see each other's data

Step definitions call the same service objects the TestNG tests use, so an endpoint change is
fixed in one place for both suites.

---

## Docker

```bash
docker build -t api-automation:latest .

docker run --rm -e ENV=mock -e SUITE=testng \
  -v "$PWD/target:/app/target" api-automation:latest

docker run --rm -e ENV=uat -e SUITE=cucumber -e TAGS="@smoke" \
  -e AUTH_USERNAME=svc_qa -e AUTH_PASSWORD="$UAT_PASSWORD" api-automation:latest
```

The suite's exit code is the container's exit code, so any orchestrator can gate on it.

## Jenkins

`Jenkinsfile` is a declarative pipeline with `ENVIRONMENT`, `SUITE`, `TAGS` and `THREADS` build
parameters, a nightly `cron` trigger, credentials injected from the Jenkins credential store, and
`post` publishing of JUnit results, the Allure report, the Cucumber HTML report and the logs.

GitHub Actions (`.github/workflows/api-tests.yml`) runs both suites in a matrix on every push,
against `mock` by default and against a deployed environment on the nightly schedule.

---

## What the suite covers

| Area | Checks |
|---|---|
| Users | single read, pagination, create, replace (PUT), patch, delete, unknown id → 404, page beyond last |
| Auth | login, registration, token issue/cache/refresh, missing password, missing email |
| Resources | collection read, single read, unknown id → 404 |
| Contract | JSON Schema validation on every documented payload |
| Non-functional | response-time SLA, server-side delay handling, correlation id on every request |

Assertions are fluent and diagnostic — a failure prints the status line, the elapsed time and the
body that came back:

```
Expected status code <200> but was <404>.
--- response ---
status : HTTP/1.1 404 Not Found
time   : 143 ms
body   : {}
```

## Requirements

- Java 21+
- Maven 3.9+
- Internet access for `sit` / `uat` / `live`; none for `mock`

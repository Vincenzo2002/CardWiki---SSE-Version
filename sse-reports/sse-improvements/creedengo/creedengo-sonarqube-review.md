# Summary of Creedengo and SonarQube corrections

## Changes implemented

We reviewed the findings reported by Creedengo and SonarQube, prioritising
changes that could improve code quality without substantially changing the
application structure. The work covered backend configuration and security,
endpoints, mappers, entities, exceptions, profiles, repositories, services and
validation, together with existing tests and JMH benchmarks.

Most corrections consisted of declaring suitable variables and parameters
`final`. Some Spring dependencies were moved to constructor injection to allow
their fields to be final as well. Other changes included removing unused code,
introducing reusable constants, improving Boolean checks and exception handling,
and making time-zone selection explicit.

## False positives

We considered the two suggestions to use `System.arraycopy` in
`SimpleImageService.bytesToHex` to be false positives. The method converts each
byte into two hexadecimal characters rather than simply copying array elements.
Replacing the conversion with an array copy would therefore not preserve its
behaviour, so the existing implementation was retained.

## Changes not applied

Some suggestions were left unchanged after considering their impact:

- **DTO and entity fields:** we avoided a general conversion to `final`, since
  setters, object mapping and persistence may require mutable fields. This does
  not prevent local references to these objects from being final.
- **Persistence-context injection:** the `EntityManager` in `DataGenerator`
  retained its existing injection mechanism, as changing it would require a
  separate check of persistence-context behaviour.
- **Transactional calls and method complexity:** the changes discussed for
  `CategoryServiceImpl` and `SimpleLearnService.saveAttempt` were postponed to
  keep this intervention focused and review the broader refactoring separately.
- **Service-interface parameters:** adding `final` was not considered useful
  here, since it does not constrain parameter reassignment in implementations.

These decisions concern scope or compatibility; they do not classify all
remaining findings as false positives.

## Validation and expected benefits

The pre-commit backend validation completed with **457 tests passed**, with no
failures or errors. The JMH benchmarks also compiled and completed a smoke run.
The expected benefits mainly concern maintainability and clearer dependency
management. No comparative measurements were collected to quantify performance
or energy improvements from this refactoring.

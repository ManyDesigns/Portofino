# Changelog
All notable changes to this project from version 5.0.0 upwards will be documented in this file. 
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

### Changed
- New versatile strategy for annotations in Elements and in the model: use proxies instead of implementation classes.
  This allows to support third-party annotations with no effort from the developer.
  Existing annotations are automatically migrated.
  
### Deprecated
- The old `AnnotationsManager` in Elements, replaced by the `AnnotationFactory`.
  
### Removed
- The `@LabelI18n` annotation that has been long deprecated in favor of `@Label`. 

## [Unreleased]

### Added
- Restored and extended some Portofino 4 features:
    - Numbers and dates can be searched with ranges as well as with an exact value.
    - Text properties can be searched with various operators (contains, equals, starts-with and ends-with).
    - Sign up, change password and forgot password dialogs and REST methods.
    - Password fields can ask to confirm the password (i.e., to type it twice).
- Improve developer experience with better code reloading:
    - Quartz jobs are reloaded automatically when the code changes, without restarting the application,
    and any open Hibernate sessions are closed even in case of exceptions.
    - When source code changes, the user-defined Spring context is refreshed, so that services and actions can pick up the changes.
    Only works with classes annotated `@Component`, `@Repository` or `@Service`, to avoid excessive refreshes.
- When embedded, the crud page has now the option to open the detail in the same page instead of navigating to the detail URL.

### Changed
- UI improvements:
    - Better looks by drawing inspiration from [sb-admin-material](https://github.com/start-javascript/sb-admin-material).
    - Use Material Typography.
    - The user declining to log in is sent back to the home. The current page has a chance to inject another behaviour.
    - Support the `multiplier` property of the `@DecimalFormat` annotation (for percent, per mille and similar).
    - Rich text component is better integrated in Material forms.
    - Better detection of links in text fields with `@HighlightLinks`.
- Important dependencies updated: Angular, Groovy, Liquibase, Shiro.

### Fixed
- Select fields with no value and disabled select fields showing nothing or `undefined`.
- Create new page at the top level.
- Toolbar overflowing on mobile.
- Support BigInteger and BigDecimal properties in the UI.
- Properly save the crud page configuration.

### Security
- Improved code quality and security and updated insecure dependencies using automated tools.

## [5.0.1] - 2019-06-07

### Added
- Created this changelog.
- Completed the translation to Italian.
- HQL queries support list parameters (ported from P4).

### Changed
- The no-frontend-build Maven profile skips all TypeScript/Angular builds without compromising the Java build.
- Keep the order of databases in the model (ported from P4).
- Updated to Angular 8.
- Logging out redirects to the home page.

### Fixed
- CRUD bulk operations detection by the UI.
- Inconsistent use of schemaName vs actualSchemaName.
- Default field encrypter (ported from P4).
- Many to many selection provider database session (ported from P4).
- Annotations not preserved when saving CRUD configuration and tables.

### Security
- Improved code quality and security using snyk, lgtm and SpotBugs to find vulnerabilities and brittle code.

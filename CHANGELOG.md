# Changelog
All notable changes to this project from version 5.0.0 upwards are documented in this file. 
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

### Added
- Notifications bell showing up to 20 messages
- More error reporting in the wizard
- Spanish translation of the UI, thanks to Gabriel Mautino
- Possibility for buttons to prevent double submit by returning an Observable
- RestEasy support (alternative to Jersey JAX-RS)
- Tested on WildFly 19.0.0.Final
- Docker image for the upstairs application

### Changed
- Polished the Maven archetype

### Fixed
- Database views aren't usable in practice
- Move page broken for text pages ([#349](https://github.com/ManyDesigns/Portofino/issues/349))

## [5.1.2] - 2020-03-02

### Added
- API for pages to compute their navigation menu
- Support for buttons with only an icon and no text
- Button to refresh the CRUD search

### Changed
- Wizard is a bit more user-friendly
- Pretty-print saved `config.json`

### Fixed
- `SendMailAction` not working ([#323](https://github.com/ManyDesigns/Portofino/issues/323))
- Arrays in XML annotations ([#325](https://github.com/ManyDesigns/Portofino/issues/325))
- Local API with IPv6 addresses ([#327](https://github.com/ManyDesigns/Portofino/issues/327))
- Can't save selection providers ([#328](https://github.com/ManyDesigns/Portofino/issues/328))
- Generated `Security.groovy` errors when loading groups ([#329](https://github.com/ManyDesigns/Portofino/issues/329))
- OPTIONS with expired JWT fails triggering bad CORS ([#333](https://github.com/ManyDesigns/Portofino/issues/333))
- Server-side localized info/warning/error messages ([#334](https://github.com/ManyDesigns/Portofino/issues/334))

## [5.1.1] - 2020-01-10

### Added
- Text pages (client only, no I18n).
- Support deployment behind a proxy.
- Documentation of Docker development, debugging and deployment in the archetype.
- Example deployment of demo-tt as Docker containers on Amazon ECS.
- Sample Docker deployment with the backend and frontend separated into different containers. 
- Bare-bones support for mapping tables as POJO's (objects) rather than maps.

### Changed
- Give pages more control on security checks, allowing for client-only pages.
- Improve performance of selection providers by asking for data lazily in certain cases.
- Properly support Docker deployments using the image built by the standard pom of archetype-generated projects.
- Several wizard improvements and fixes.

### Fixed
- Model not properly saved ([#294](https://github.com/ManyDesigns/Portofino/issues/294), [#303](https://github.com/ManyDesigns/Portofino/issues/303)).
- Page configuration not properly saved ([#310](https://github.com/ManyDesigns/Portofino/issues/310), [#311](https://github.com/ManyDesigns/Portofino/issues/311)).
- Names of mapped database objects not escaped ([#297](https://github.com/ManyDesigns/Portofino/issues/297)).
- New actions added with bad class names ([#301](https://github.com/ManyDesigns/Portofino/issues/301)).
- Database synchronization issues with MariaDB and PostgreSQL ([#283](https://github.com/ManyDesigns/Portofino/issues/283), [#298](https://github.com/ManyDesigns/Portofino/issues/298)).
- KeyManager with no password (ported from Portofino 4).

## [5.1.0] - 2019-10-31

### Added
- Map database views, by default as read-only, but overridable.
- Support non insertable and/or non updatable entities in CRUD actions.
- Annotations are propagated from model to annotated classes.
- Support persistent Java 8+ Date and time API values.
- Generic database platform for unrecognized databases.
- Support for development and debug with Docker, both in demo-tt and in the archetype.
- Periodically retry database connections that have failed at startup if Quartz is available.
- Fallback database platform for unrecognized database systems.
- Filter CRUD fields according to permissions (at the level of the ClassAccessor).
- Filter configuration fields according to permissions.
  In particular, the CRUD query is hidden if the user is not a developer.
- Proper support for `@DatabaseBlob` upstairs.

### Changed
- **Update Hibernate to the 5.x branch**, in particular to version 5.4.7.Final.
    The Hibernate Session factory is now configured with annotated Java classes generated at runtime.
    The ad-hoc code for Hibernate 4 has been removed.
- Deprecate the single `portofino-model.xml` file in favor of multiple `portofino-model/<database-name>/database.xml` files.
  Legacy files are supported and converted to the new format upon save.
- Change the format of annotations in the model and in `configuration.xml`.
  Annotations in the legacy format are converted upon save.
- Replace `java.io.File` with Apache Commons VFS `FileObject` in `Persistence`.
  **This is a breaking API change.**
- Update all uses of Commons Configuration to version 2.6, including Commons Configuration 1.x uses.
  **This is a breaking API change.**
- Ensure that Portofino 5 can run without a Java compiler (JDK) available.
  
### Fixed
- Virtual properties not saved properly [[#269]](https://github.com/ManyDesigns/Portofino/issues/269).
- Wizard failing on connections to Oracle databases [[#271]](https://github.com/ManyDesigns/Portofino/issues/271).
- Minor upstairs, Spring and Security.groovy fixes.

## [5.0.3] - 2019-09-25

### Added
- Support for MariaDB Connector/J.
- Simple automatic JWT renewal (this means that user sessions do not expire during regular user activity).
- Possibility to override buttons in subclasses [[#253]](https://github.com/ManyDesigns/Portofino/issues/253).

### Changed
- JWT expiration property is now called `jwt.expiration`.
- Better Spring context reloading and more rational context layout w/ documentation. Still doesn't support all scenarios.
- Ensure Portofino does not create any HTTP sessions by itself in any circumstances [[#255]](https://github.com/ManyDesigns/Portofino/issues/255). 

### Fixed
- Badly broken `Security.groovy`, both the built-in one and ones generated by the wizard.
- `mail.enabled = false` makes the application fail to start.
- CRUD/forms:
   - Form invalid with empty, pristine date fields.
   - Save/update exception not handled properly.
   - Rich text field label misplacement.
   - Passwords visible in search results.
   - Possibility to save configuration with missing required fields.
   - Missing error message for max length in text fields.
   - IDs with spaces cause exception [[#254]](https://github.com/ManyDesigns/Portofino/issues/254).

## [5.0.2] - 2019-08-29

### Added
- Restored and extended some Portofino 4 features:
    - Numbers and dates can be searched with ranges as well as with an exact value.
    - Text properties can be searched with various operators (contains, equals, starts-with and ends-with).
    - Sign up, change password and forgot password dialogs and REST methods.
    - Password fields can ask to confirm the password (i.e., to type it twice).
- Improve developer experience with better code reloading:
    - Quartz jobs are reloaded automatically when the code changes, without restarting the application,
      and, when they terminate execution, any open Hibernate sessions are closed even in case of exceptions.
    - When source code changes, the user-defined Spring context is refreshed, so that services and actions can pick up the changes.
      This only works with classes annotated `@Component`, `@Repository` or `@Service`, to avoid excessive refreshes.
- When embedded, the crud page has now the option to open the detail in the same page instead of navigating to the detail URL.
- Check for new versions at startup.
- Make the JWT expiration time configurable (in minutes, defaults to 30).

### Changed
- UI improvements:
    - Improve looks by drawing inspiration from [sb-admin-material](https://github.com/start-javascript/sb-admin-material).
    - Use Material Typography.
    - The user declining to log in is sent back to the home. The current page has a chance to inject another behaviour.
    - Support the `multiplier` property of the `@DecimalFormat` annotation (for percent, per mille and similar).
    - The rich text component is better integrated in Material forms.
    - More comprehensive detection of links in text fields with `@HighlightLinks`.
    - The page settings panel is reachable via the URL, by including the query parameter `settings`.
- Important dependencies updated: Angular, Groovy, Liquibase, Shiro.

### Fixed
- Select fields with no value or disabled showing nothing or `undefined`.
- Create new page at the top level.
- Toolbar overflowing on mobile.
- Support BigInteger and BigDecimal properties in the UI.
- Properly save the crud page configuration.
- Use the correct schema name when synchronizing an aliased schema.

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

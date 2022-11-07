# Changelog
All notable changes to this project from version 5.0.0 upwards are documented in this file. 
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [5.3.4] – Unreleased

### Added
- Explicit priority to `PortofinoFilter` so that users can install their own filters before or after Portofino's.
- Possibility to enable Portofino's dispatcher in Boot applications not managed by Portofino, thus easing migration.

### Changed
- Mounted children that point to files are resolved relative to the application directory. In practice,
  this means that Portofino no longer saves the absolute path of the login action in the root `action.xml`.

### Fixed
- Database annotations lost when synchronizing the model [#593](https://github.com/ManyDesigns/Portofino/issues/593)

## [5.3.3] – 2022-10-22

### Added
- New deployment option: Portofino modules as Spring Boot components, without using Portofino's dispatcher and without 
  setting up Portofino's context hierarchy.

### Changed
- Updated Angular to version 13
- Replaced moment.js with Luxon

### Fixed
- Wrong links in the war archetype [#556](https://github.com/ManyDesigns/Portofino/issues/556)
- Omitting the login.path property results in a malfunctioning application [#557](https://github.com/ManyDesigns/Portofino/issues/557)
- Mounted child path not converted to the native OS format [#566](https://github.com/ManyDesigns/Portofino/issues/566)

## [5.3.2] – 2022-05-12

### Added
- Pluggable exporters for CRUD.
- Configuration abstraction, integrated with Spring's *Environment* (which includes `application.properties` in Spring Boot).  

### Changed
- Refactored the CRUD's JSON output logic into a separate exporter class. **This is a breaking API change**
  for CRUD extensions overriding `jsonXYZData` methods.
- In Spring Boot applications, when using Jersey (the default), allow Portofino actions to coexist with Boot services 
  such as actuators.

### Fixed
- Multiple issues with the Spring Boot-based archetypes, including but not limited to:
  - Application generated from Java service archetype searches for action classes in `src/main/resources/portofino`
  - Unsupported CORS with service archetypes. By default, archetypes will use an embedded Tomcat instead of Undertow for the time being (startup is slower)
  - Misleading Java service archetype `SpringConfiguration.java`

## [5.3.1] – 2022-03-10

### Added
- Support building on Java 17
- Generic CRUD page upstairs
- Allow resource-actions to return synthetic resource-actions without any file object
- Configurable API path with Spring Boot
- Include sample GitHub action in archetypes
- AWS S3 blob manager
- Ability to disable a database connection with an annotation (adapted from Portofino 4 which uses a configuration property instead)

### Changed
- Updated Angular libraries and material icons package
- Updated several Java libraries to fix vulnerabilities. Note that all the Portofino versions in the last decade (4.x and 5.x) do NOT suffer from log4shell because they don't use Log4j (unless you explicitly replace Logback with Log4j in your project, of course).

### Fixed
- Refreshing the UI's security token when the app is starting
- Validation issues during insertion float/double numbers in Postgres [#471](https://github.com/ManyDesigns/Portofino/issues/471)
- Several issues with the Maven archetypes

## [5.3.0] – 2021-09-13

### Added
- Microservice deployment options using Spring Boot.
- Easier Quartz job registration using Spring beans.
- Email microservice.
- Quartz scheduler microservice.
- Possibility to exclude Shiro-based security from the application (and implement security using other
  libraries).
- Support for multi-tenancy in Hibernate.

### Changed
- Authentication/authorization endpoint is now fixed (`/:auth`) and forwards to the login action. This makes life a little easier for clients.
- Simplify initialization by removing PortofinoListener.
- URL to trigger mail sender changed from `/actions/mail-sender-run` to `/portofino-send-mail` and HTTP method changed from GET to POST.
- Mail sender action can be disabled with `mail.sender.action.enabled=false`.
- Ensured that Java-only microservices without Groovy are possible.
- Optimized selection providers based on foreign keys on save.
- Reduce assumptions on persistent entities by CRUD actions allowing more possibilities for custom entities.
- Liquibase updated to version 4, minor library updates to fix vulnerabilities.

## [5.2.1] – 2021-04-10

### Added
- Possibility to reorder table columns in the tables section upstairs.

### Changed
- Render internal links in text pages with no target attribute (that the Quill editor adds by default).
- The welcome page is now a standard text page.
- Security.groovy can now have user beans injected with `@Autowired`.

### Fixed
- Important security vulnerability that may have allowed access with forged tokens.
- Authentication token refresh after expiration. [#430](https://github.com/ManyDesigns/Portofino/issues/430)
- Backwards compatibility: revert `T extends Serializable` in CRUD actions, introduced in v5.2.0. [#428](https://github.com/ManyDesigns/Portofino/issues/428)

## [5.2.0] – 2020-11-30

### Added

- User interface extensibility improvements:
  - Custom pages in HTML and JavaScript with no Angular knowledge required (example in demo-tt)
  - All pages can load custom JavaScript (example in demo-tt)
  - Page templates can include several sections where child pages can be embedded (like Portofino 4)
  - demo-tt profile page (example of custom component)
  - Progressive Web Application (PWA) using angular-pwa, example in demo-tt
- Full support for POJO-based persistence:
  - Export generated classes to actions and shared code
  - Export generated classes to the file system so that your IDE can pick them up
  - Allow to configure the entity mode of each database mapping from the UI (the default is still map-based)
- Authentication improvements:
  - Better support for external auth (e.g., in a microservices setting). Built-in support for authentication
    against KeyCloak.
  - application/json login endpoint (in addition to the existing form-based endpoint).
  - Better handling of token expiration in the client.
- Per-database Hibernate properties.
- Support annotations on databases.
- Quartz jobs now run with a working Shiro environment.
- Ability for extensions and user code to "mount" actions to arbitrary mount points in the action tree.

### Changed
- Angular updated to version 11
- Groovy updated to version 3
- Various other dependencies updated for security & bug fixes
- CRUD REST API versioning with X-Portofino-API-Version header
- CRUD REST API changes (legacy behavior is still the default):
  - Bulk update (PUT) optionally returns list of modified IDs
  - Bulk delete (DELETE) optionally returns list of deleted IDs
- Generated Security.groovy is now based on annotations on the model rather than hard-coded values

### Removed
- Maven profile "portofino-development" which has been superseded by Docker
- Maven profile "no-frontend-build" (use -P-build-frontend)
- `PortofinoRoot.mount` method and related methods, replaced by `ActionLogic.mount`. This is a breaking API change for
  extensions making use of the removed methods, but none are known to us (also because the methods are undocumented and
  only used internally by portofino-upstairs).

### Fixed

- Newly created CRUD pages don't have create/update buttons ([#406](https://github.com/ManyDesigns/Portofino/issues/406))
- Page configuration is broken for pages using the default template implicitly ([#423](https://github.com/ManyDesigns/Portofino/issues/423)).
- User self-registration is broken in several ways ([#414](https://github.com/ManyDesigns/Portofino/issues/414)).
  Note that **this involves a breaking API change** in `Security.groovy`, specifically, the signature of the method
  `saveSelfRegisteredUser` has changed to return both the token and the email of the newly saved user.
  However, typical `Security.groovy` files (generated by the wizard) don't override that method, so most users shouldn't
  have to do anything.

## [5.1.4] – 2020-07-04

### Added
- "Fat jar" deployment option (with embedded Tomcat)
- Restored the profile section of demo-tt with an example custom Angular component
- Included the MariaDB JDBC driver in the binary distribution

### Changed
- Improved wizard and UI
- Postgres driver version updated
- Include explicit 'tt' schema in SQL queries in demo-tt (plays nicer with Postgres)
- Mail module as a separate artifact

### Fixed
- CRUD pages generated by wizard don't have buttons ([#372](https://github.com/ManyDesigns/Portofino/issues/372))
- Broken blob download URL and code ([#384](https://github.com/ManyDesigns/Portofino/issues/384))
- Pressing the Enter key on a search field does not perform a search ([#383](https://github.com/ManyDesigns/Portofino/issues/383))

## [5.1.3] – 2020-05-17

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
- JWT filter eats the Authorization header even if it's not a JWT ([#367](https://github.com/ManyDesigns/Portofino/issues/367))
- OpenAPI manifest only shows root resource ([#369](https://github.com/ManyDesigns/Portofino/issues/369)) 

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

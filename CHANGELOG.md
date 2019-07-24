# Changelog
All notable changes to this project from version 5.0.0 upwards will be documented in this file. 
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

### Added
- Numbers and dates can be searched with ranges (like in Portofino 4) as well as with an exact value..
- Change password dialog.
- Forgot password dialog.
- Quartz jobs are reloaded automatically when the code changes, without restarting the application, and open Hibernate sessions are closed even in case of exceptions.

### Changed
- Angular version updated.
- Liquibase version updated.

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
- Select fields with no value.
- Create new page at the top level was broken.

### Security
- Improved code quality and security using snyk, lgtm and SpotBugs to find vulnerabilities and brittle code.

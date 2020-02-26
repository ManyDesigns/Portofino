## Developing and Deploying Portofino Applications with Docker

In this directory we find a common structure of Docker files and other resources that, combined with support from
Portofino 5.1.0 onwards, make it possible to:
 - **run and debug the application locally in Docker containers** without installing Tomcat and the database system(s)
   required by the application;
 - **deploy the application with Docker containers** in production or in other environments.
 
### Structure of the Docker Images 

The scheme we've identified comprises these Docker images:
 - One for each database
 - Three for the application:
   - One including the backend and the frontend as a single Tomcat web application
   - One including only the backend (Tomcat)
   - One including only the frontend (served by nginx)
 - One that extends the application's all-in-one image with additional support for development and debugging.
 
Unsurprisingly, the `database` directory contains files to build the database image(s), while the `application`
directory contains the resources to build the application image and the `application/debug` directory the development
and debug image.

### Software versions

The version of Java and Tomcat are specified in the `FROM` line of the file `application/Dockerfile`. Please refer to
https://hub.docker.com/_/tomcat/ for the available versions. 

The vendor and version of each database is specified in the `FROM` line of the Docker files in the `database` directory.
Images for popular open source database implementations can be found in [Docker Hub](https://hub.docker.com/). 

The vendor and version of the JDBC drivers that the application uses must be included as Maven dependencies with scope
`provided` in the project's `pom.xml` file and referenced in the `application/assembly.xml` file. An example is included
in the archetype.

### Maven Integration

The aforementioned Docker resources are meant to be used in conjunction with Maven. In particular, the Maven profile
`docker` enables such integration.

Running `mvn -Pdocker package`, i.e., building the application with the `docker` profile, will automatically build the
Docker images. Note that, if we plan on running the application locally via Maven for development, **the `docker`
profile is incompatible with the `portofino-development` profile and actually supersedes it.**

Also note that the frontend is only built if the profile `build-frontend` is active. Normally it is active by default,
but if we activate the `docker` profile, then the default profiles are not activated unless explicitly requested with
`-P`.

The `pom.xml` file also defines the Docker volumes used for persistence. Everything that is not saved in a volume is
lost when the container is deleted. By default, database images don't mount any volume; if we want to persist data
across runs of our application, we have to bind additional volumes (refer to the documentation of each database Docker
image). Please note that the database image is only meant to be used for development, where the accidental loss of
persistent data is only a minor inconvenience. Do not use it in production.

For more information about databases, development and debugging, please refer to the appropriate sections.

### Considerations on Portofino With Docker

Since Docker containers are isolated, in general you cannot access the database(s) on `localhost` from the inside of a
container running the application. Therefore, you'll want to remove database connection parameters from
`database.xml` files and store them in `portofino.properties`, so you'll be able to either override them with
`portofino-local.properties` in the image, or use environment variables placeholders (e.g., `${env:MY_DATABASE_HOST}`).

### About the Databases

Each Docker image of a database system has its own method of initializing the database (i.e., creating users and schemas,
inserting initial data, and so on). Typically, scripts in the directory `docker-entrypoint-initdb.d` will be executed in
order, but please refer to the documentation of your image(s).

For information about persisting database data, please refer to the [Maven Integration](#maven-integration) section.

### Development and Debugging

To launch the application locally for development and debugging, provided that we've built the Docker images, we can use
the following command:

```
mvn -Pdocker docker:run
```

This will launch the application listening on the port specified in `pom.xml` (8080 by default). Remote debugging with
our IDE is possible on the debug port (8000 by default, again specified in `pom.xml`).

The application will read and write the source directory of our project, so that modifications made in the IDE and
modifications made in the running application will be immediately reflected both in the application and in the sources.

Note that Tomcat runs as root in the container; as such, it might create new resources with owner root in our source
directory on *NIX systems. In that case, we'll have to manually change their permissions.

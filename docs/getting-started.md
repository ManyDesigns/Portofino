---
layout: page
title: Getting started with Portofino
---

## Using the Command-Line Interface

### Prerequisites

Let's ensure we have Java 8+ and Maven 3+ installed and available on our PATH. How to check it:

```shell
mvn --version
```

Example output:

```
Apache Maven 3.6.3 (cecedd343002696d0abb50b32b541b8a6ba2883f)
Maven home: /Users/alessio/apache-maven-3.6.3
Java version: 14.0.1, vendor: Oracle Corporation, runtime: /Library/Java/JavaVirtualMachines/jdk-14.0.1.jdk/Contents/Home
Default locale: it_IT, platform encoding: UTF-8
OS name: "mac os x", version: "10.15.7", arch: "x86_64", family: "mac"
```

Having an existing database installation and a schema with some data in it is also recommended, so that we'll build an 
application that can _do something useful_ with our data. However, it's not strictly necessary.

### Installing the CLI

Follow the instructions on https://github.com/alessiostalla/portofino-cli.

### Creating a new Portofino application

Now, assuming we have the `portofino` command in our PATH, we can run: 

```shell
portofino new my-great-project
```

That will generate a new project using the Portofino Maven archetype as a template, 
in a newly created directory called `my-great-project`.

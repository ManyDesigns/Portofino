# ManyDesigns Portofino 5 #

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.manydesigns/portofino/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.manydesigns/portofino)
[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/ManyDesigns/Portofino.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/ManyDesigns/Portofino/context:java)
[![Language grade: JavaScript](https://img.shields.io/lgtm/grade/javascript/g/ManyDesigns/Portofino.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/ManyDesigns/Portofino/context:javascript)
[![Build Status](https://travis-ci.com/ManyDesigns/Portofino.svg?branch=master)](https://travis-ci.com/ManyDesigns/Portofino)
[![Join the chat at https://gitter.im/ManyDesigns-Portofino/Lobby](https://badges.gitter.im/ManyDesigns-Portofino/Lobby.svg)](https://gitter.im/ManyDesigns-Portofino/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Download the latest version of Portofino from SourceForge](https://img.shields.io/sourceforge/dm/portofino.svg)](https://sourceforge.net/projects/portofino/files/latest/download)

Portofino is a tool for building model-driven web APIs and applications.
It's written in Java and extensible using Groovy, and it's distributed under the LGPL
open-source license. It's developed by the company, ManyDesigns, based in Genova, Italy (http://www.manydesigns.com).

The tool can be used to create good looking applications for the Web and mobile devices. The creation process can include:
 - automatic generation through a "wizard" tool that analyses the structure of your existing relational database;
 - manual configuration through a web-based administration interface that lives alongside the application;
 - customization with Groovy (for the backend) and Angular/JavaScript (for the frontend).
 
The result is a fully functional web application with:
 * a responsive web user interface based on Angular Material
 * a customizable backend fully exposed with REST-style APIs
 * authentication, authorization and user management
 * an email subsystem
 * a scheduler and much more.
The application is designed to be incrementally extended and customized,
both graphically and in functionality (e.g., adding new buttons to existing pages).
When existing extension points are not enough, completely custom REST resources and Angular components can be integrated,
while retaining the possibility to use built-in services through dependency injection.

Development can happen "PHP style", i.e. by modifying a live application using only a text editor,
as well as "Java style", by employing an IDE, a build system, release and deployment – but with a quick edit-refresh-test cycle.

Portofino is based on popular and proven open-source libraries such as Hibernate, Groovy, Apache Shiro, Spring, Angular and Angular Material, Jersey JAX-RS.

The home of Portofino is http://portofino.manydesigns.com. There you can find the documentation, pointers to community
resources (forums, wiki, issue tracker), commercial support.

Portofino is translated in several languages, thanks to Manydesigns and various contributors. Languages include English, Italian, French, German, Spanish and Arabic.

## Getting Started ##

To get started, it's useful to begin from the wiki on GitHub: https://github.com/ManyDesigns/Portofino/wiki/Getting-started-with-Portofino-5.

The official compiled distribution is hosted on SourceForge: http://sourceforge.net/projects/portofino.
It requires no installation, just unzip it. It is a bundle of Apache Tomcat, Portofino and JDBC drivers for popular
open source databases.

This repository contains the source code of the framework. If you want to build it, see
https://github.com/ManyDesigns/Portofino/wiki/Building-from-source. It's not necessary to build Portofino in order to use it.

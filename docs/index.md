---
layout: home
title: ManyDesigns Portofino
---

## From database to REST API in 30 seconds

Portofino 6 is the next generation of ManyDesigns Portofino, the result of decades of experience in model-driven, 
low-code application frameworks.

Portofino connects to your databases, constructs an enriched model of your data, and exposes it with a REST API. You can
customize and extend the API declaratively and, when needed, by coding in Java or Groovy.

Authentication and authorization are built-in (using Apache Shiro) or can be managed externally with your favorite 
implementation.

<a class="btn btn-success" role="button" href="getting-started">Getting Started</a> 
<a class="btn btn-primary" role="button" href="#what-sets-portofino-apart">What sets Portofino apart</a>

## What sets Portofino apart

Portofino is characterized by being **feature-rich, model-driven, and reflective**. Let's see what we mean by that.

* **Feature-rich:** Portofino is the result of many years of production usage. If you encounter a problem, chances are 
  that Portofino comes with a beaten path to arrive at a solution. Master-detail? Logical deletion of records? 
  Viewing but not modifying historical data? We got you covered!
* **Model-driven:** in traditional software applications, everything is code. User management? Code! Access to the 
  database? Code! **Your business rules? Code.** Developers like that, because they can read code and reason about it. 
  However, when you need answers to questions like, *when do we apply that discount?*, or, *where do we store user profiles, 
  and who can look at them?*, then you'll need a developer to "decode the code" and reconstruct the answer.
  **In Portofino, business information is stored in a high-level model of your application, not in code.** This model
  is human-readable as text, and can easily be exported to other formats, including diagrams.
* **Reflective:** traditional model-driven software engineering is heavily based on code generation. Someone authors the
  model with specific tools, then generates the code, and finally developers work on the generated code.
  This is not a lean process, and it doesn't play well with agile development practices. Just adding a field on a form
  may require authoring a new version of the model, running the code generator again, and having a developer add the 
  field to the form. This may require different people, with different schedules, using different tools. 
  And, in such a scenario, usually the generated code cannot consult the model – the information, again, is lost, too
  "encoded" to be practically retrievable.
  
  Portofino, instead, doesn't do code generation – the model is consulted on the fly, and it's authored directly in the
  same application, either by automatically importing the database, or using command-line or GUI tools that are
  familiar to developers.

## Which problems it solves

That's all nice and good, but what can we *do* with Portofino in practice? Here are some examples.

* **Give a legacy database a modern REST API.** Perhaps you cannot afford to rewrite that legacy application at the core
  of your business, or perhaps you don't *want* to, because it works just fine. At the same time, to integrate it in a 
  modern environment, you need APIs, and maybe a microservices architecture. **With Portofino, you can quickly 
  build an API for your database, and deploy it as a Spring Boot application,** without touching legacy code.
* **Build fully or partially generated forms** driven by a model of your data. Modify which fields are visible, which 
  are writable, validation rules, labels in multiple languages, etc. – and share all that information among the backend, 
  the frontend, REST APIs, web services, etc.!
* **Build modern, service-oriented business applications** that make it easy to track business rules and requirements.

## Features

### Built-in Data Access

Adding new views of your data (queries) with new options amounts to writing configuration files. Each view is a full
SCRUD resource, i.e. it comes with the following operations out of the box:
- **Search** with criteria involving multiple fields and operators (e.g., "name starts with P and age is greater than 18")
- **Create** a new object (database row)
- **Read** an object given its ID
- **Update** a single object or multiple objects in a single API call
- **Delete** one or more objects

Operations that modify data have **extensive validation capabilities** that report to the client which fields have issues and why.

### Heavily Customizable

If the above generic operations are not adequate for the API you're building, you can easily integrate new custom 
operations with light coding in Groovy or Java. Or you can call into any other JVM languages, JavaScript included, 
from those two.

Portofino is built on top of Spring and Hibernate and is designed to be a nice citizen in a microservices environment.

## Client Libraries

Portofino exposes REST APIs, so any REST client can consume its services. However, a few client libraries specifically
designed for Portofino exist, such as:

 * [portofino-commander](https://github.com/alessiostalla/portofino-commander) designed for Portofino 6, may also work with 5
 * [portofino-js](https://manydesigns.github.io/portofino-js/) designed for Portofino 5, may also work with 6

## UI

Portofino 6 doesn't come with a built-in UI, unlike its predecessors. The UI space is way more fragmented than it was 
when Portofino 4 and 5 were designed. Today it's simply out of our capacity to build a GUI that is suitable for most
users, and as dynamic and customizable as Portofino 4/5 were. Already Portofino 5 was too complex and only targeted 
Angular.

That said, web developers can build great applications on top of the APIs generated by Portofino, using their UI
libraries of choice. Also, there are a few ready-made UI solutions that explicitly target Portofino:
 * Portofino Angular UI (extracted from Portofino 5 and adapted for Portofino 6)
 * portofino-react-admin 

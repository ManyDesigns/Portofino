grammar Model;

importDeclaration: IMPORT IDENTIFIER ('.' wildcard='*')?;

databasePersistence: database*;

database: DATABASE name=IDENTIFIER '(' connectionProperty* ')'
    (';' | '{' schema* '}');

connectionProperty: name=IDENTIFIER '=' value=literal;
schema: SCHEMA name=IDENTIFIER ('(' physicalName=STRING ')')?;

standaloneDomain: domain;

domain: annotation* DOMAIN name=IDENTIFIER (';' | '{' (domain | entity | relationship)* '}');

standaloneEntity: entity;

entity: annotation* ENTITY name=IDENTIFIER '{'
  property*
  relationshipProperty*
'}';

property: annotation* name=IDENTIFIER (':' type)?;

relationship: annotation* name=IDENTIFIER ':' a=type '-->' b=type;

relationshipProperty: annotation* name=IDENTIFIER '-->' type;

type: IDENTIFIER nullable='?'?;

annotation: '@' name=IDENTIFIER annotationParams?;

annotationParams: '(' (literal | (IDENTIFIER '=' literal (',' IDENTIFIER '=' literal)*)) ')';

literal: BOOLEAN | NUMBER | STRING;

BOOLEAN: 'true' | 'false';
NUMBER: ('+' | '-')?[0-9]+('.'[0-9]+);
STRING: '"' ('\\'('"'|'\\')|.)*? '"';

SCHEMA: 'schema';
IMPORT: 'import';
ENTITY: 'entity';
DOMAIN: 'domain';
DATABASE: 'database';

IDENTIFIER: IDENTIFIER_COMPONENT ('.' IDENTIFIER_COMPONENT)*;
WS: (' ' | '\t' | '\n' | '\r') -> channel(HIDDEN);

fragment IDENTIFIER_COMPONENT: [a-zA-Z_][a-zA-Z0-9_]*;

grammar Model;

standaloneDomain: importDeclaration* domain;
domain: annotation* DOMAIN name=simpleIdentifier (';' | '{' (domain | entity | relationship)* '}')?;

standaloneEntity: importDeclaration* entity;
entity: annotation* ENTITY name=simpleIdentifier '{'
  ID '{' idProperties+=property+ '}'
  properties+=property*
  relationshipProperty*
'}';

importDeclaration: IMPORT identifier ('.' wildcard='*')?;

property: annotation* name=simpleIdentifier (':' type)?;

relationship: annotation* name=identifier ':' a=type '-->' b=type;

relationshipProperty: annotation* name=identifier '-->' type multiplicity?;
multiplicity: atLeast=NUMBER ('..' (atMost=NUMBER | unbounded='*')) | any='*';

type: name=identifier nullable='?'?;

annotation: '@' name=identifier annotationParams?;

annotationParams: '(' (literal | (simpleIdentifier '=' literal (',' simpleIdentifier '=' literal)*)) ')';

literal: BOOLEAN | NUMBER | STRING;

identifier: simpleIdentifier ('.' simpleIdentifier)*;
simpleIdentifier: IDENTIFIER_COMPONENT | ID | SCHEMA | IMPORT | ENTITY | DOMAIN | DATABASE | BOOLEAN;

BOOLEAN: 'true' | 'false';
NUMBER: ('+' | '-')?([0-9]+('.'[0-9]*)?|[0-9]*'.'[0-9]+);
STRING: '"' ('\\'('"'|'\\')|.)*? '"';

SCHEMA: 'schema';
IMPORT: 'import';
ID: 'id';
ENTITY: 'entity';
DOMAIN: 'domain';
DATABASE: 'database';

IDENTIFIER_COMPONENT: [a-zA-Z_][a-zA-Z0-9_]*;

WS: (' ' | '\t' | '\n' | '\r') -> channel(HIDDEN);

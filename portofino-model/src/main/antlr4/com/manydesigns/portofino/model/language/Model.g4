grammar Model;

databasePersistence: database*;

database: ('database') name=IDENTIFIER '(' connectionProperty* ')' ('{'
  schema*
'}')?;

connectionProperty: name=IDENTIFIER '=' value=literal;
schema: 'schema' name=IDENTIFIER ('(' physicalName=STRING ')')?;

standaloneDomain: domain;

domain: annotation* 'domain' name=IDENTIFIER '{'
    (domain | entity | relationship)*
'}';

standaloneEntity: entity;

entity: annotation* 'entity' name=IDENTIFIER '{'
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
STRING: '"' ('\\"'|.)*? '"';
IDENTIFIER: IDENTIFIER_COMPONENT ('.' IDENTIFIER_COMPONENT)*;
WS: (' ' | '\t' | '\n' | '\r') -> skip;

fragment IDENTIFIER_COMPONENT: [a-zA-Z_][a-zA-Z0-9_]*;

grammar Model;

databasePersistence: database*;

database: ('database') name=IDENTIFIER '(' connectionProperty* ')' ('{'
  schema*
'}')?;

connectionProperty: name=IDENTIFIER '=' value=literal;
schema: 'schema' name=IDENTIFIER ('(' physicalName=STRING ')')?;

standaloneDomain: domain;

domain: 'domain' name=IDENTIFIER '{'
    (domain | entity)*
'}';

standaloneEntity: entity;

entity: annotation* 'entity' name=IDENTIFIER '{'
  property*
  relationship*
'}';

importDeclaration: 'import' IMPORT;

property: annotation* name=IDENTIFIER (':' type)?;

relationship: annotation* name=IDENTIFIER '-->' type;

type: IDENTIFIER nullable='?'?;

annotation: '@' name=IDENTIFIER annotationParams?;

annotationParams: '(' (literal | (IDENTIFIER '=' literal (',' IDENTIFIER '=' literal)*)) ')';

literal: BOOLEAN | NUMBER | STRING;

BOOLEAN: 'true' | 'false';
NUMBER: ('+' | '-')?[0-9]+('.'[0-9]+);
STRING: '"' ('\\"'|.)*? '"';
IMPORT: IDENTIFIER '.*'?;
IDENTIFIER: IDENTIFIER_COMPONENT ('.' IDENTIFIER_COMPONENT)*;
WS: (' ' | '\t' | '\n' | '\r') -> skip;

fragment IDENTIFIER_COMPONENT: [a-zA-Z_][a-zA-Z_0-9]*;

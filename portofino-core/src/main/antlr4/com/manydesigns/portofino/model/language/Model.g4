grammar Model;

standaloneDomain: importDeclaration* domain EOF;
domain: annotation* DOMAIN name=simpleIdentifier (';' | '{' (domain | entity | relationship)* '}')?;

standaloneEntity: importDeclaration* entity EOF;
entity: annotation* ENTITY name=simpleIdentifier '{'
  (ID '{' idProperties+=property+ '}')?
  properties+=property*
  relationshipProperty*
'}';
property: annotation* name=simpleIdentifier (':' type)? (NOT_NULLABLE)?;

standaloneObject: object EOF;
object: OBJECT name=simpleIdentifier ':' objectBody;
objectBody: className=identifier '{' properties+=propertyAssignment* '}';
propertyAssignment: name=simpleIdentifier '=' propertyValue;
propertyValue: literal | objectBody | owner=identifier '.' field=identifier | propertyListValue;
propertyListValue: '[' (propertyValue (',' propertyValue)*)? ']';

importDeclaration: IMPORT name=identifier (AS alias=simpleIdentifier)? ';'?;

relationship: annotation* name=identifier ':' a=type '-->' b=type;

relationshipProperty: annotation* name=identifier '-->' type relationshipMappings? RANGE?;
relationshipMappings: '(' (relationshipMapping (',' relationshipMapping)*) ')';
relationshipMapping: otherName=simpleIdentifier '=' ownName=simpleIdentifier;

type: name=identifier;

annotation: '@' type annotationParams?;

annotationParams: '(' (literal | (simpleIdentifier '=' literal (',' simpleIdentifier '=' literal)*)) ')';
annotationParamValue: literal | '[' (annotationParamValue (',' annotationParamValue)*)? ']';

literal: BOOLEAN | UNSIGNED_INTEGER | SIGNED_INTEGER | REAL | STRING;

identifier: simpleIdentifier ('.' simpleIdentifier)*;
simpleIdentifier: IDENTIFIER_COMPONENT | ID | SCHEMA | IMPORT | ENTITY | DOMAIN | DATABASE | BOOLEAN;

RANGE: UNSIGNED_INTEGER ('..' (UNSIGNED_INTEGER | '*')) | '*';
BOOLEAN: 'true' | 'false';
UNSIGNED_INTEGER: [0-9]+;
SIGNED_INTEGER: ('+' | '-') UNSIGNED_INTEGER;
REAL: ('+' | '-')?([0-9]+('.'[0-9]*)?|[0-9]*'.'[0-9]+);
STRING: '"' ('\\'('"'|'\\')|.)*? '"';
NOT_NULLABLE: '!';

SCHEMA: 'schema';
OBJECT: 'object';
IMPORT: 'import';
ID: 'id';
ENTITY: 'entity';
DOMAIN: 'domain';
DATABASE: 'database';
AS: 'as';

IDENTIFIER_COMPONENT: [a-zA-Z_][a-zA-Z0-9_]*;

WS: (' ' | '\t' | '\n' | '\r') -> channel(HIDDEN);

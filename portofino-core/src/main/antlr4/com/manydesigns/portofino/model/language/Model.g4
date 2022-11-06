grammar Model;

standaloneDomain: importDeclaration* domain EOF;
domain: annotation* DOMAIN name=identifier (';' | '{' (domain | entity | enum | relationship)* '}')?;

standaloneEntity: importDeclaration* entity EOF;
entity: annotation* ENTITY name=identifier (COLON baseEntity=identifier) '{'
  (ID '{' idProperties+=property+ '}')?
  (properties+=property | relationshipProperty)*
'}';
property: annotation* name=identifier (':' type)? (NOT_NULLABLE)?;

enum: ENUM name=identifier '{' values+=identifier+ '}';

standaloneObject: importDeclaration* object EOF;
object: OBJECT name=identifier ':' objectBody;
objectBody: className=identifier '{' properties+=propertyAssignment* '}';
propertyAssignment: name=identifier ('=' propertyValue | '-->' referredObject=fqn);
propertyValue: literal | objectBody | enumValue=identifier | propertyListValue;
propertyListValue: '[' (propertyValue (',' propertyValue)*)? ']';

importDeclaration: IMPORT name=fqn (AS alias=identifier)? ';'?;

relationship: annotation* name=identifier ':' a=type '-->' b=type;

relationshipProperty: annotation* name=identifier '-->' type relationshipMappings? RANGE?;
relationshipMappings: '(' (relationshipMapping (',' relationshipMapping)*) ')';
relationshipMapping: otherName=identifier '=' ownName=identifier;

type: name=fqn;

annotation: '@' type annotationParams?;

annotationParams: '(' (literal | (identifier '=' literal (',' identifier '=' literal)*)) ')';
annotationParamValue: literal | '[' (annotationParamValue (',' annotationParamValue)*)? ']';

literal: BOOLEAN | UNSIGNED_INTEGER | SIGNED_INTEGER | REAL | STRING;

fqn: identifier ('.' identifier)*;
identifier: IDENTIFIER_COMPONENT | ID | SCHEMA | IMPORT | ENTITY | DOMAIN | DATABASE | BOOLEAN;

RANGE: UNSIGNED_INTEGER ('..' (UNSIGNED_INTEGER | '*')) | '*';
BOOLEAN: 'true' | 'false';
UNSIGNED_INTEGER: [0-9]+;
SIGNED_INTEGER: ('+' | '-') UNSIGNED_INTEGER;
REAL: ('+' | '-')?([0-9]+('.'[0-9]*)?|[0-9]*'.'[0-9]+);
STRING: '"' ('\\'('"'|'\\')|.)*? '"';
NOT_NULLABLE: '!';
COLON: ':';

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

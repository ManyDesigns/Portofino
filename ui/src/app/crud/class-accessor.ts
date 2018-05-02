namespace portofino.accessors {

  export class ClassAccessor {
    name: string;
    properties: Property[];
    keyProperties: Property[];
  }

  export class Property {
    name: string;
    type: string;
  }

  export class Annotation {
    type: string;
    properties: object[];
  }
}


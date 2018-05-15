export class ClassAccessor {
  name: string;
  properties: Property[];
  keyProperties: Property[];
}

export class Property {
  name: string;
  type: string;
  annotations: Annotation[];
  modifiers: string[];
}

export class Annotation {
  type: string;
  properties: object[];
}

export function getAnnotation(property: Property, type: string): Annotation {
    return property.annotations.find(value => value.type == type);
  }

export function isSearchable(property: Property) {
  const annotation = getAnnotation(property, "com.manydesigns.elements.annotations.Searchable");
  return annotation && annotation.properties["value"];
}


export class ClassAccessor {
  name: string;
  properties: Property[];
  keyProperties: string[];
}

export class Property {
  name: string;
  type: string;
  annotations: Annotation[];
  modifiers: string[];
  key: boolean;

  editable: boolean;
}

export class Annotation {
  type: string;
  properties: object[];
}

export function getAnnotation(property: Property, type: string): Annotation {
  return property.annotations.find(value => value.type == type);
}

export function isEnabled(property: Property) {
  const annotation = getAnnotation(property, "com.manydesigns.elements.annotations.Enabled");
  return annotation && annotation.properties["value"];
}

export function isUpdatable(property: Property) {
  const annotation = getAnnotation(property, "com.manydesigns.elements.annotations.Updatable");
  return annotation && annotation.properties["value"];
}

export function isInsertable(property: Property) {
  const annotation = getAnnotation(property, "com.manydesigns.elements.annotations.Insertable");
  return annotation && annotation.properties["value"];
}

export function isSearchable(property: Property) {
  const annotation = getAnnotation(property, "com.manydesigns.elements.annotations.Searchable");
  return annotation && annotation.properties["value"];
}

export function isInSummary(property: Property) {
  const annotation = getAnnotation(property, "com.manydesigns.elements.annotations.InSummary");
  return annotation && annotation.properties["value"];
}

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
  label: string;
  key: boolean;
  kind: string;

  editable: boolean;
  selectionProvider: any;
}

export class Annotation {
  type: string;
  properties: object[];
}

export function getAnnotation(property: Property, type: string): Annotation {
  return property.annotations.find(value => value.type == type);
}

export function isBooleanProperty(property: Property) {
  return property.type == 'java.lang.Boolean'
}

export function isStringProperty(property: Property) {
  return property.type == 'java.lang.String'
}

export function isNumericProperty(property: Property) {
  return property.type == 'java.lang.Long'
}

export function isDateProperty(property: Property) {
  return property.type == 'java.util.Date' || property.type == 'java.sql.Timestamp';
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

export function isRequired(property: Property) {
  const annotation = getAnnotation(property, "com.manydesigns.elements.annotations.Required");
  return annotation && annotation.properties["value"];
}

export function isMultiline(property: Property) {
  const annotation = getAnnotation(property, "com.manydesigns.elements.annotations.Multiline");
  return annotation && annotation.properties["value"];
}

export function isRichText(property: Property) {
  const annotation = getAnnotation(property, "com.manydesigns.elements.annotations.RichText");
  return annotation && annotation.properties["value"];
}

export function isBlob(property: Property) {
  return getAnnotation(property, "com.manydesigns.elements.annotations.FileBlob") ||
         getAnnotation(property, "com.manydesigns.elements.annotations.DatabaseBlob");
}

export function deriveKind(property: Property) {
  if(isBlob(property)) {
    return "blob";
  }
  if(isNumericProperty(property)) {
    return "number";
  }
  if(isDateProperty(property)) {
    return "date";
  }
  if(isStringProperty(property)) {
    return "string";
  }
  if(isBooleanProperty(property)) {
    return "boolean";
  }
  throw `${property.name}: unsuppored property type ${property.type}`
}

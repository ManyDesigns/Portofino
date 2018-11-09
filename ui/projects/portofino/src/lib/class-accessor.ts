import {ValidatorFn, Validators} from "@angular/forms";

export class ClassAccessor {
  name: string;
  properties: Property[];
  keyProperties: string[];
}

export class Property {
  name: string;
  type: string;
  annotations: Annotation[] = [];
  modifiers: string[] = [];
  label: string;
  key: boolean;
  get kind() {
    return deriveKind(this);
  }

  editable: boolean;
  selectionProvider: any;

  static create(values: Property | any): Property {
    return Object.assign(new Property(), values)
  }
}

export class Annotation {
  type: string;
  properties: object[];
}

export const ANNOTATION_REQUIRED = "com.manydesigns.elements.annotations.Required";

export function getAnnotation(property: Property, type: string): Annotation {
  return property.annotations.find(value => value.type == type);
}

export function isBooleanProperty(property: Property) {
  return property.type == 'java.lang.Boolean' || property.type == 'boolean'
}

export function isStringProperty(property: Property) {
  return property.type == 'java.lang.String' || property.type == 'string'
}

export function isNumericProperty(property: Property) {
  return property.type == 'java.lang.Long' || property.type == 'java.lang.Integer'
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
  const annotation = getAnnotation(property, ANNOTATION_REQUIRED);
  return annotation && annotation.properties["value"];
}

export function isMultiline(property: Property) {
  const annotation = getAnnotation(property, "com.manydesigns.elements.annotations.Multiline");
  return annotation && annotation.properties["value"];
}

export function isPassword(property: Property) {
  const annotation = getAnnotation(property, "com.manydesigns.elements.annotations.Password");
  return !!annotation;
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

export function getValidators(property: Property): ValidatorFn[] {
  let validators = [];
  //Required on checkboxes means that they must be checked, which is not what we want
  if (isRequired(property) && property.kind != 'boolean') {
    validators.push(Validators.required);
  }
  const maxLength = getAnnotation(property, "com.manydesigns.elements.annotations.MaxLength");
  if (maxLength) {
    validators.push(Validators.maxLength(maxLength.properties["value"]));
  }
  const maxValue =
    getAnnotation(property, "com.manydesigns.elements.annotations.MaxDecimalValue") ||
    getAnnotation(property, "com.manydesigns.elements.annotations.MaxIntValue");
  if (maxValue) {
    validators.push(Validators.max(maxValue.properties["value"]));
  }
  const minValue =
    getAnnotation(property, "com.manydesigns.elements.annotations.MinDecimalValue") ||
    getAnnotation(property, "com.manydesigns.elements.annotations.MinIntValue");
  if (minValue) {
    validators.push(Validators.max(minValue.properties["value"]));
  }
  return validators;
}

import {ValidatorFn, Validators} from "@angular/forms";
import {map} from "rxjs/operators";

export const loadClassAccessor = map((c: ClassAccessor) => {
  if(!c) {
    return c;
  }
  if(c.initSelectionProviders) {
    c.initSelectionProviders();
    return c;
  } else {
    return ClassAccessor.create(c);
  }
});

export class ClassAccessor {
  name: string;
  properties: Property[];
  keyProperties: string[];

  initSelectionProviders() {
    this.properties.forEach(p => {
      const select = getAnnotation(p, "com.manydesigns.elements.annotations.Select");
      if (select) {
        p.selectionProvider = Object.assign(new SelectionProvider(), {displayMode: select.properties.displayMode});
        for (let i in select.properties.values) {
          p.selectionProvider.options.push({v: select.properties.values[i], l: select.properties.labels[i], s: false});
        }
      }
    });
  }

  static create(values: ClassAccessor | any): ClassAccessor {
    const ca = Object.assign(new ClassAccessor(), values);
    ca.initSelectionProviders();
    return ca;
  }
}

export class Property {
  name: string;
  type = 'string';
  annotations: Annotation[] = [];
  modifiers: string[] = [];
  label: string;
  key: boolean;
  get kind() {
    return deriveKind(this);
  }

  editable: boolean;
  selectionProvider: SelectionProvider;

  static create(values: Property | any): Property {
    return Object.assign(new Property(), values)
  }

  required(value: boolean = true): Property {
    return this.withAnnotation(ANNOTATION_REQUIRED, { value: value });
  }

  withAnnotation(type: string, properties: any = {}) {
    const annotation = getAnnotation(this, type);
    if(annotation) {
      annotation.properties = properties;
    } else {
      this.annotations.push(new Annotation(type, properties));
    }
    return this;
  }

  withSelectionProvider(sp: SelectionProvider | any) {
    this.selectionProvider = Object.assign(new SelectionProvider(), sp);
    return this;
  }
}

export class Annotation {
  type: string;
  properties: any;

constructor(type?: string, properties?: any) {
    this.type = type;
    this.properties = properties;
  }
}

export class SelectionProvider {
  name?: string;
  index: number = 0;
  displayMode: string = "DROPDOWN";
  url?: string;
  nextProperty?: string;
  updateDependentOptions: () => void = () => {};
  loadOptions: (value?: string) => void = () => {};
  options: SelectionOption[] = [];
}

export class SelectionOption {
  v: string;
  l: string;
  s: boolean;
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
  return property.type == 'java.lang.Long' || property.type == 'java.lang.Integer' ||
         property.type == 'java.lang.Float' || property.type == 'java.lang.Double' ||
         property.type == 'java.math.BigInteger' || property.type == 'java.math.BigDecimal' ||
         property.type == 'number'
}

export function isDateProperty(property: Property) {
  return property.type == 'java.util.Date' || property.type == 'java.sql.Date' || property.type == 'java.sql.Timestamp';
}

export function isEnabled(property: Property) {
  const annotation = getAnnotation(property, "com.manydesigns.elements.annotations.Enabled");
  //By default, properties are enabled
  return !annotation || annotation.properties.value;
}

export function isUpdatable(property: Property) {
  const annotation = getAnnotation(property, "com.manydesigns.elements.annotations.Updatable");
  return annotation && annotation.properties.value;
}

export function isInsertable(property: Property) {
  const annotation = getAnnotation(property, "com.manydesigns.elements.annotations.Insertable");
  return annotation && annotation.properties.value;
}

export function isSearchable(property: Property) {
  const annotation = getAnnotation(property, "com.manydesigns.elements.annotations.Searchable");
  return annotation && annotation.properties.value;
}

export function isInSummary(property: Property) {
  const annotation = getAnnotation(property, "com.manydesigns.elements.annotations.InSummary");
  return annotation && annotation.properties.value;
}

export function isRequired(property: Property) {
  const annotation = getAnnotation(property, ANNOTATION_REQUIRED);
  return annotation && annotation.properties.value;
}

export function isMultiline(property: Property) {
  const annotation = getAnnotation(property, "com.manydesigns.elements.annotations.Multiline");
  return annotation && annotation.properties.value;
}

export function isPassword(property: Property) {
  const annotation = getAnnotation(property, "com.manydesigns.elements.annotations.Password");
  return !!annotation;
}

export const RICH_TEXT_ANNOTATION = "com.manydesigns.elements.annotations.RichText";

export function isRichText(property: Property) {
  const annotation = getAnnotation(property, RICH_TEXT_ANNOTATION);
  return annotation && annotation.properties.value;
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
  throw `${property.name}: unsupported property type ${property.type}`
}

export function getValidators(property: Property): ValidatorFn[] {
  let validators = [];
  //Required on checkboxes means that they must be checked, which is not what we want
  if (isRequired(property) && property.kind != 'boolean') {
    validators.push(Validators.required);
  }
  const maxLength = getAnnotation(property, "com.manydesigns.elements.annotations.MaxLength");
  if (maxLength) {
    validators.push(Validators.maxLength(maxLength.properties.value));
  }
  const maxValue =
    getAnnotation(property, "com.manydesigns.elements.annotations.MaxDecimalValue") ||
    getAnnotation(property, "com.manydesigns.elements.annotations.MaxIntValue");
  if (maxValue) {
    validators.push(Validators.max(maxValue.properties.value));
  }
  const minValue =
    getAnnotation(property, "com.manydesigns.elements.annotations.MinDecimalValue") ||
    getAnnotation(property, "com.manydesigns.elements.annotations.MinIntValue");
  if (minValue) {
    validators.push(Validators.max(minValue.properties.value));
  }
  return validators;
}

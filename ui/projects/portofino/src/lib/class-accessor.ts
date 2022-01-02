import {ValidatorFn, Validators} from "@angular/forms";

export const loadClassAccessor = (c: ClassAccessor) => {
  if(!c) {
    return c;
  }
  if(c.initSelectionProviders) {
    c.initSelectionProviders();
    return c;
  } else {
    return ClassAccessor.create(c);
  }
};

export const BOOLEAN_TYPE = "boolean";
export const DATE_TYPE = "date";
export const NUMBER_TYPE = "number";
export const STRING_TYPE = "string";

export class ClassAccessor {
  name: string;
  properties: Property[] = [];
  keyProperties: string[] = [];

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

  static getProperty(self: ClassAccessor, name: string) {
    for(const p in self.properties) {
      let property = self.properties[p];
      if(property.name == name) {
        return property
      }
    }
  }

  static create(values: ClassAccessor | any): ClassAccessor {
    const ca = Object.assign(new ClassAccessor(), values);
    ca.initSelectionProviders();
    return ca;
  }

  static forObject(object: any, options: {
    name?: string, ownProperties?: boolean,
    properties: { [name: string]: Property | any }
  } = { properties: {} }): ClassAccessor {
    let accessor = new ClassAccessor();
    accessor.name = options.name;
    for(let p in object) {
      if(options.ownProperties && !object.hasOwnProperty(p)) {
        continue;
      }
      let value = object[p];
      if(value && value.value) {
        value = value.value; //Handle objects returned by Elements that have value and displayValue
      }
      let property;
      let defaultValues: any = { name: p, label: p.charAt(0).toUpperCase() + p.slice(1) };
      if(typeof(value) === NUMBER_TYPE) {
        defaultValues.type = NUMBER_TYPE;
      }
      //We don't handle arrays and objects for now
      if(options.properties.hasOwnProperty(p)) {
        if(options.properties[p]) {
          property = Property.create({ ...defaultValues, ...options.properties[p] });
        }
      } else {
        if(defaultValues.type || (typeof(value) == STRING_TYPE)) {
          property = Property.create(defaultValues);
        }
      }
      if(property) {
        accessor.properties.push(property);
      }
    }
    return accessor;
  }
}

export class Property {
  name: string;
  type = STRING_TYPE;
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

  static get(owner: ClassAccessor, name: string) {
    return ClassAccessor.getProperty(owner, name);
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
  return property.type == 'java.lang.Boolean' || property.type == BOOLEAN_TYPE
}

export function isStringProperty(property: Property) {
  return property.type == 'java.lang.String' || property.type == STRING_TYPE
}

export function isNumericProperty(property: Property) {
  return property.type == 'java.lang.Long' || property.type == 'java.lang.Integer' ||
         property.type == 'java.lang.Float' || property.type == 'java.lang.Double' ||
         property.type == 'java.math.BigInteger' || property.type == 'java.math.BigDecimal' ||
         property.type == NUMBER_TYPE
}

export function isDateProperty(property: Property) {
  return property.type == 'java.util.Date' ||
         property.type == 'java.sql.Date' || property.type == 'java.sql.Timestamp' ||
         property.type == DATE_TYPE;
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
    return NUMBER_TYPE;
  }
  if(isDateProperty(property)) {
    return DATE_TYPE;
  }
  if(isStringProperty(property)) {
    return STRING_TYPE;
  }
  if(isBooleanProperty(property)) {
    return BOOLEAN_TYPE;
  }
  throw `${property.name}: unsupported property type ${property.type}`
}

export function getValidators(property: Property): ValidatorFn[] {
  let validators = [];
  //Required on checkboxes means that they must be checked, which is not what we want
  if (isRequired(property) && property.kind != BOOLEAN_TYPE) {
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
    validators.push(Validators.min(minValue.properties.value));
  }
  return validators;
}

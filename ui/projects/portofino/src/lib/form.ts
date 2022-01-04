import {
  ClassAccessor,
  deriveKind,
  getAnnotation,
  getValidators,
  isBlob,
  isBooleanProperty,
  isDateProperty,
  isEnabled,
  isMultiline,
  isRichText,
  Property
} from "./class-accessor";
import {
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  ComponentFactoryResolver,
  Directive,
  EventEmitter,
  Host,
  Input,
  OnDestroy,
  OnInit,
  Optional,
  Output,
  QueryList,
  Type,
  ViewChildren,
  ViewContainerRef
} from "@angular/core";
import {
  AbstractFormGroupDirective,
  ControlContainer,
  FormArray,
  FormControl,
  FormGroup,
  FormGroupDirective,
  ValidationErrors,
  ValidatorFn
} from "@angular/forms";
import {FieldFactoryComponent} from "./fields/field.factory";
import { DateTime } from "luxon";

export type FormElement =
  Field|FieldSet|{name: string, component: Type<any>, dependencies?: object}|{html: string}|FormList;

export class Form {
  contents: FormElement[] = [];
  editable: boolean = true;
  /** The URL from which to download blobs and other resources. */
  baseUrl: string;
  selectableFields: boolean = false;
  //TODO parent form for defaults? For fieldsets

  constructor(contents: FormElement[] = []) {
    this.contents = contents;
  }

  static fromClassAccessor(ca: ClassAccessor, setup: FormSetup = {}) {
    const form = new Form();
    if(setup.properties) {
      setup.properties.forEach(name => {
        const property = ca.properties.find(p => p.name == name);
        if(property) {
          Form.setupProperty(form, property, setup);
        }
      })
    } else {
      ca.properties.forEach(property => {
        Form.setupProperty(form, property, setup);
      });
    }
    return form;
  }

  //This has to be static, because we want to copy forms with form = {...form}
  static setupProperty(form: Form, property: Property, setup) {
    if(!isEnabled(property)) {
      return;
    }
    try {
      //TODO if list FormList else
      form.contents.push(Field.fromProperty(property, setup.object || {}));
    } catch (e) {
      if(setup.error) {
        setup.error(e);
      } else {
        console.error(e);
      }
    }
  }
}

export class FormSetup { object?: any; properties?: string[]; }

export class BlobFile {
  lastModified: number;
  lastModifiedDate: Date;
  name: string;
  size: number;
  type: string;
  code: string;
}

export class Field {

  constructor(property: Property = undefined, initialState = undefined) {
    this.property = property;
    this.initialState = initialState;
  }

  property: Property;
  initialState: any;
  editable: boolean = true;
  type: Type<any>;
  context = {};

  get name() {
    return this.property.name;
  }

  mergeContext(context: any) {
    return {...this.context, ...context};
  }

  static fromProperty(property: Property | any, object = {}, disabled?: boolean) {
    if(!(property instanceof Property)) {
      property = Property.create(property);
    }
    deriveKind(property); //Cause an exception to be thrown early if the type is not supported
    let value =
      (object[property.name] && object[property.name].hasOwnProperty("value")) ?
        object[property.name].value :
        object[property.name];
    if(!value) {
      //value is undefined
    } else if (isDateProperty(property)) {
      value = DateTime.fromMillis(value);
    } else if (isBlob(property)) {
      const portofinoBlob = value;
      value = new BlobFile();
      value.code = portofinoBlob.code;
      value.size = portofinoBlob.size;
      value.name = portofinoBlob.filename;
      value.type = portofinoBlob.contentType;
      value = [value];
    } else if(disabled && !isBooleanProperty(property) && object[property.name].displayValue) {
      value = object[property.name].displayValue;
    }
    let field = new Field(property, { value: value, disabled: disabled });
    field.editable = !disabled;
    return field;
  }
}

export class FieldSetSetup extends FormSetup { name?: string; label?: string; visible?: boolean; }

export class FieldSet {
  name: string;
  label: string;
  contents: Form;
  visible = true;

  static fromClassAccessor(ca: ClassAccessor, setup: FieldSetSetup = {}) {
    const fieldSet = new FieldSet();
    setup = Object.assign({ object: {}, visible: true }, setup);
    fieldSet.name = setup.name || ca.name;
    fieldSet.label = setup.label || fieldSet.name;
    fieldSet.visible = setup.visible;
    fieldSet.contents = Form.fromClassAccessor(ca, setup);
    return fieldSet;
  }
}

class FormList {
  name: string;
  contents: Form[];
}

@Directive({
  selector: '[portofino-dynamic-form-component]'
})
export class DynamicFormComponentDirective {

  @Input()
  name: string;

  constructor(public viewContainerRef: ViewContainerRef) { }

}

@Component({
  selector: 'portofino-form',
  templateUrl: '../../assets/form.component.html',
  styleUrls: ['../../assets/form.component.scss']
})
export class FormComponent implements OnInit, AfterViewInit, OnDestroy {
  private _controls: FormGroup;
  @Input()
  set controls(controls: FormGroup) {
    this._controls = controls;
    if(controls && this.form) {
      this.reset(this.form);
    }
  }
  get controls() {
    return this._controls;
  }
  private _form: Form;
  @Input()
  set form(form: Form) {
    this._form = form;
    if(this.controls && form) {
      this.reset(form);
    }
  }
  get form(): Form {
    return this._form;
  }
  @ViewChildren(FieldFactoryComponent)
  fields: QueryList<FieldFactoryComponent>;
  @ViewChildren(FormComponent)
  fieldSets: QueryList<FormComponent>;
  @ViewChildren(DynamicFormComponentDirective)
  dynamicComponents: QueryList<DynamicFormComponentDirective>;
  @Output()
  formReset = new EventEmitter();

  constructor(
    protected componentFactoryResolver: ComponentFactoryResolver, protected changeDetector: ChangeDetectorRef,
    @Optional() @Host() protected controlContainer: ControlContainer) {}

  ngOnInit(): void {
    if(!this.controls) {
      if(this.controlContainer instanceof AbstractFormGroupDirective ||
         this.controlContainer instanceof FormGroupDirective) {
        this.controls = this.controlContainer.control;
      } else {
        this.controls = new FormGroup({});
      }
    }
  }

  ngAfterViewInit(): void {
    //TODO efficiency - make sure to only init once
    if(this.form) {
      this.reset(this.form)
    }
  }

  ngOnDestroy(): void {
    this.formReset.complete();
  }

  protected reset(form: Form) {
    this.setupForm(form, this.controls);
    this.formReset.emit(this);
  }

  protected setupForm(form: Form, formGroup: FormGroup) {
    let controlNames = [];
    form.contents.forEach(v => {
      if (v instanceof Field) {
        controlNames.push(v.name);
        this.setupField(v, form.selectableFields, formGroup);
      } else if (v instanceof FieldSet) {
        controlNames.push(v.name);
        this.setupFieldSet(v, formGroup);
      } else if(v.hasOwnProperty('component')) {
        if(this.dynamicComponents) {
          controlNames.push(v["name"]);
          this.setupDynamicComponent(v, formGroup);
        }
      } else if(v instanceof FormList) {
        controlNames.push(v.name);
        this.setupFormList(v, formGroup);
      } else if(!v.hasOwnProperty("html")) {
        throw { message: "BUG! Unsupported form element", element: v }
      }
    });
    let controlsToDelete = [];
    for (let controlName in formGroup.controls) {
      if(controlNames.indexOf(controlName) < 0) {
        controlsToDelete.push(controlName);
      }
    }
    controlsToDelete.forEach(name => formGroup.removeControl(name));
  }

  protected setupFormList(v: FormList, formGroup: FormGroup) {
    let control = formGroup.get(v.name);
    if (control instanceof FormArray) {
      const formArray = control as FormArray;
      v.contents.forEach((f, i) => {
        let formGroup = new FormGroup({});
        this.setupForm(f, formGroup);
        if(i >= formArray.length) {
          formArray.insert(i, formGroup);
        } else {
          formArray.setControl(i, formGroup); //TODO maybe reuse existing?
        }
        formGroup.setParent(formArray);
      });
    } else {
      control = new FormArray([]);
      v.contents.forEach(f => {
        let formGroup = new FormGroup({});
        this.setupForm(f, formGroup);
        (control as FormArray).push(formGroup);
        formGroup.setParent(control as FormArray);
      });
      formGroup.setControl(v.name, control);
      control.setParent(formGroup);
    }
  }

  protected setupDynamicComponent(v, formGroup: FormGroup) {
    //TODO name -> formGroup, which is optional. If not provided, component.form = formGroup
    const control = new FormGroup({});
    const name = v['name'];
    formGroup.setControl(name, control);
    control.setParent(formGroup);
    let componentFactory = this.componentFactoryResolver.resolveComponentFactory(v['component']);
    const viewContainerRef = this.dynamicComponents.find(c => c.name == name).viewContainerRef;
    viewContainerRef.clear();
    const component = viewContainerRef.createComponent(componentFactory).instance;
    component['form'] = control;
    if (v['dependencies']) {
      for (const dep in v['dependencies']) {
        component[dep] = v['dependencies'][dep];
      }
    }
    this.changeDetector.detectChanges();
  }

  protected setupFieldSet(v: FieldSet, formGroup: FormGroup) {
    let control = formGroup.get(v.name);
    if (control instanceof FormGroup) {
      this.setupForm(v.contents, control as FormGroup);
    } else {
      control = new FormGroup({});
      this.setupForm(v.contents, control as FormGroup);
      formGroup.setControl(v.name, control);
      control.setParent(formGroup);
    }
  }

  protected setupField(v: Field, selectable: boolean, formGroup: FormGroup) {
    const property = v.property;
    const passwordAnnotation = getAnnotation(property, "com.manydesigns.elements.annotations.Password");
    const passwordConfirmationRequired = passwordAnnotation ? passwordAnnotation.properties.confirmationRequired : false;
    const control = formGroup.get(property.name);
    const initialState = this.computeInitialState(v);

    if(passwordConfirmationRequired && !initialState.disabled && !selectable) {
      //TODO reset?
      const subGroup = new FormGroup({});
      subGroup.setControl("password", new FormControl(initialState, getValidators(property)));
      subGroup.setControl("confirmPassword", new FormControl(initialState, getValidators(property)));
      subGroup.setValidators(samePasswordChecker("password", "confirmPassword"));
      formGroup.setControl(property.name, subGroup);
      subGroup.setParent(formGroup);
    } else if (control instanceof FormControl) {
      control.reset(initialState);
    } else {
      const newControl = new FormControl(initialState, getValidators(property));
      formGroup.setControl(property.name, newControl);
      newControl.setParent(formGroup);
    }
  }

  protected computeInitialState(v: Field) {
    const selectionProvider = v.property.selectionProvider;
    let initialState = v.initialState;
    const disabled = !this.form.editable || !v.editable;
    if(initialState && initialState.hasOwnProperty("value")) {
      initialState = Object.assign({}, initialState, { disabled: disabled });
    } else {
      initialState = { value: initialState, disabled: disabled };
    }
    if(initialState.value && selectionProvider && selectionProvider.options && selectionProvider.options.length > 0) {
      const selectedOption = selectionProvider.options.find(
        o => o.v === initialState.value.v || o.v === initialState.value);
      if(selectedOption) {
        return Object.assign(initialState, { value: selectedOption });
      }
    }
    return initialState;
  }

  get allFields() {
    //TODO perhaps it would be better to return a QueryList<FieldComponent>
    const allFields = this.fields.toArray();
    this.fieldSets.forEach(fieldSet => {
      const fields = fieldSet.allFields;
      for (let f in fields) {
        allFields.push(fields[f]);
      }
    });
    return allFields;
  }

  isField(obj) {
    return obj instanceof Field;
  }

  isFieldSet(obj) {
    return obj instanceof FieldSet;
  }

  isFormList(obj) {
    return obj instanceof FormList;
  }

  isMultiline(property) {
    return isMultiline(property) || isRichText(property);
  }

}

export function samePasswordChecker(passwordField, confirmationField): ValidatorFn {
  return (control: FormGroup): ValidationErrors | null => {
    const password = control.get(passwordField);
    const confirmPassword = control.get(confirmationField);
    return password.value !== confirmPassword.value ? {'passwordsDontMatch': true} : null;
  };
}

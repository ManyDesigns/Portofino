import {ClassAccessor, deriveKind, getValidators, isEnabled, Property} from "./class-accessor";
import {
  AfterViewInit, ChangeDetectorRef,
  Component,
  ComponentFactoryResolver, Directive,
  EventEmitter, Host,
  Input, OnInit, Optional, Output,
  QueryList, Type,
  ViewChildren, ViewContainerRef
} from "@angular/core";
import {AbstractFormGroupDirective, ControlContainer, FormControl, FormGroup, FormGroupDirective} from "@angular/forms";
import {FieldFactoryComponent} from "./fields/field.factory";

export class Form {
  contents: (Field|FieldSet|{name: string, component: Type<any>, dependencies ?: object}|{html: string})[] = [];
  editable: boolean = true;
  /** The URL from which to download blobs and other resources. */
  baseUrl: string;
  selectableFields: boolean = false;
  //TODO parent form for defaults? For fieldsets

  constructor(contents = []) {
    this.contents = contents;
  }

  static fromClassAccessor(ca: ClassAccessor, object = {}) {
    const form = new Form();
    ca.properties.forEach(property => {
      if(!isEnabled(property)) {
        return;
      }
      try {
        form.contents.push(Field.fromProperty(property, object));
      } catch (e) {
        //Continue
        console.error(e);
      }
    });
    return form;
  }
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

  mergeContext(context: any) {
    return {...this.context, ...context};
  }

  static fromProperty(property: Property | any, object = {}) {
    if(!(property instanceof Property)) {
      property = Property.create(property);
    }
    deriveKind(property); //Cause an exception to be thrown early if the type is not supported
    return new Field(property, object[property.name]);
  }
}

export class FieldSet {
  name: string;
  label: string;
  contents: Form;

  static fromClassAccessor(ca: ClassAccessor, setup: {name?: string, label?: string, object?: any} = {}) {
    const fieldSet = new FieldSet();
    if(!setup) {
      setup = { object: {}};
    }
    fieldSet.name = setup.name || ca.name;
    fieldSet.label = setup.label || fieldSet.name;
    fieldSet.contents = Form.fromClassAccessor(ca, setup.object || {});
    return fieldSet;
  }
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
  templateUrl: './form.component.html'
})
export class FormComponent implements OnInit, AfterViewInit {
  private _controls: FormGroup;
  @Input()
  set controls(controls: FormGroup) {
    this._controls = controls;
    if(controls && this.form) {
      this.reset(this.form, false);
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
    if(this.form) {
      this.reset(this.form)
    }
  }

  protected reset(form: Form, andControls = true) {
    this.setupForm(form, this.controls);
    if(andControls) {
      this.controls = Object.assign(new FormGroup({}), this.controls);
    }
    this.formReset.emit(this);
  }

  protected setupForm(form: Form, formGroup: FormGroup) {
    let controlNames = [];
    form.contents.forEach(v => {
      if (v instanceof Field) {
        const property = v.property;
        controlNames.push(property.name);
        const control = formGroup.get(property.name);
        if (control instanceof FormControl) {
          control.reset(v.initialState);
        } else {
          formGroup.setControl(property.name, new FormControl(v.initialState, getValidators(property)));
        }
      } else if (v instanceof FieldSet) {
        controlNames.push(v.name);
        let control = formGroup.get(v.name);
        if (control instanceof FormGroup) {
          this.setupForm(v.contents, control as FormGroup);
        } else {
          control = new FormGroup({});
          this.setupForm(v.contents, control as FormGroup);
          formGroup.setControl(v.name, control);
        }
      } else if(this.dynamicComponents && v.hasOwnProperty('component')) {
        const control = new FormGroup({});
        const name = v['name'];
        controlNames.push(name);
        formGroup.setControl(name, control);
        let componentFactory = this.componentFactoryResolver.resolveComponentFactory(v['component']);
        const viewContainerRef = this.dynamicComponents.find(c => c.name == name).viewContainerRef;
        viewContainerRef.clear();
        const component = viewContainerRef.createComponent(componentFactory).instance;
        component['form'] = control;
        if(v['dependencies']) {
          for(const dep in v['dependencies']) {
            component[dep] = v['dependencies'][dep];
          }
        }
        this.changeDetector.detectChanges();
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

}

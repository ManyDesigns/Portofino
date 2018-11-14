import {FieldComponent} from "./fields/field.component";
import {getValidators, Property} from "./class-accessor";
import {
  AfterViewInit, ChangeDetectorRef,
  Component,
  ComponentFactoryResolver, Directive,
  EventEmitter,
  Input, NgZone,
  OnInit,
  Output,
  QueryList,
  Type,
  ViewChildren, ViewContainerRef
} from "@angular/core";
import {FormControl, FormGroup} from "@angular/forms";

export class Form {
  contents: (Field|FieldSet|{name: string, component: Type<any>, dependencies ?: object}|{html: string})[] = [];
  editable: boolean;
  /** The URL from which to download blobs and other resources. */
  baseUrl: string;
  selectableFields: boolean;
  //TODO parent form for defaults? For fieldsets
}

export class Field {
  property: Property;
  initialState: any;
  editable: boolean = true;
}

export class FieldSet {
  name: string;
  label: string;
  contents: Form;
}

@Directive({
  selector: '[portofino-dynamic-form-component]'
})
export class DynamicFormComponentDirective {

  constructor(public viewContainerRef: ViewContainerRef) { }

}

@Component({
  selector: 'portofino-form',
  templateUrl: './form.component.html'
})
export class FormComponent implements AfterViewInit {
  @Input()
  controls: FormGroup;
  @ViewChildren(FieldComponent)
  fields: QueryList<FieldComponent>;
  @ViewChildren(FormComponent)
  fieldSets: QueryList<FormComponent>;
  @ViewChildren(DynamicFormComponentDirective)
  dynamicComponents: QueryList<DynamicFormComponentDirective>;
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
  @Output()
  formReset = new EventEmitter();

  constructor(protected componentFactoryResolver: ComponentFactoryResolver, protected changeDetector: ChangeDetectorRef) {}

  ngAfterViewInit(): void {
    if(!this.controls) {
      this.controls = new FormGroup({});
    }
    if(this.form) {
      this.reset(this.form)
    }
  }

  protected reset(form: Form) {
    this.setupForm(form, this.controls);
    this.formReset.emit(this);
  }

  protected setupForm(form: Form, formGroup: FormGroup) {
    let dynamicComponentIndex = 0;
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
        controlNames.push(v['name']);
        formGroup.setControl(v['name'], control);
        let componentFactory = this.componentFactoryResolver.resolveComponentFactory(v['component']);
        const viewContainerRef = this.dynamicComponents.toArray()[dynamicComponentIndex].viewContainerRef;
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

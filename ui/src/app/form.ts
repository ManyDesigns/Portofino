import {getAnnotation, isRequired, Property} from "./class-accessor";
import {Component, EventEmitter, Input, OnInit, Output, Type} from "@angular/core";
import {FormControl, FormGroup, Validators} from "@angular/forms";

export class Form {
  contents: (Field|FieldSet|{component: Type<any>}|{html: string})[] = [];
  editable: boolean;
  /** The URL from which to download blobs and other resources. */
  baseUrl: string;
  selectableFields: boolean;
  //TODO parent form for defaults? For fieldsets
}

export class Field {
  property: Property;
  initialState: any;
  editable: boolean;
}

export class FieldSet {
  name: string;
  label: string;
  contents: Form;
}

@Component({
  selector: 'portofino-form',
  templateUrl: './form.component.html'
})
export class FormComponent implements OnInit {
  @Input()
  controls: FormGroup;
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

  ngOnInit(): void {
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
    //TODO remove fields that are no longer present
    form.contents.forEach(v => {
      if (v instanceof Field) {
        const property = v.property;
        const control = formGroup.get(property.name);
        if (control instanceof FormControl) {
          control.reset(v.initialState);
        } else {
          formGroup.removeControl(property.name);
          formGroup.registerControl(property.name, new FormControl(v.initialState, this.getValidators(property)));
        }
      } else if (v instanceof FieldSet) {
        let control = formGroup.get(v.name);
        if (!(control instanceof FormGroup)) {
          formGroup.removeControl(v.name);
          control = new FormGroup({});
          this.setupForm(v.contents, control as FormGroup);
          formGroup.registerControl(v.name, control);
        }
      }
    });
  }

  protected getValidators(property: Property) {
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
}

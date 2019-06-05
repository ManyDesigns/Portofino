import {ChangeDetectorRef, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {PortofinoService} from "../portofino.service";
import {
  ClassAccessor,
  isBlob, isBooleanProperty,
  isDateProperty,
  isEnabled,
  Property, SelectionOption
} from "../class-accessor";
import moment from 'moment-with-locales-es6';
import {AbstractControl, FormArray, FormControl, FormGroup} from "@angular/forms";
import {BlobFile, Configuration, SelectionProvider} from "./crud.common";
import {Observable} from "rxjs";
import {Field, Form, FormComponent} from "../form";
import {NotificationService} from "../notifications/notification.service";
import {Button, ButtonInfo, getButtons} from "../buttons";
import {WithButtons} from "../button.component";

export abstract class BaseDetailComponent implements WithButtons {

  @Input()
  classAccessor: ClassAccessor;
  @Input()
  selectionProviders: SelectionProvider[];
  @Input()
  configuration: Configuration;
  @Input()
  sourceUrl: string;
  @Output()
  close = new EventEmitter();

  readonly formDefinition = new Form();
  readonly form = new FormGroup({});
  @ViewChild(FormComponent, { static: false })
  formComponent: FormComponent;
  properties: Property[] = [];
  object;
  protected saving = false;

  @Input()
  parentButtons: ButtonInfo[] = [];
  @Input()
  parent: any;

  protected constructor(
    protected http: HttpClient, protected portofino: PortofinoService,
    protected changeDetector: ChangeDetectorRef, protected notificationService: NotificationService) {}

  protected initClassAccessor() {
    this.classAccessor.properties.forEach(property => {
      if (this.filterProperty(property)) {
        return;
      }
      property = Object.assign(new Property(), property);
      property.editable = this.isEditable(property);
      this.properties.push(property);
    });
  }

  protected filterProperty(property): boolean {
    return !isEnabled(property);
  }

  abstract isEditable(property: Property): boolean;

  abstract isEditEnabled(): boolean;

  protected setupForm(object) {
    this.object = object;
    this.formDefinition.contents = [];
    this.properties.forEach(p => {
      let value;
      const disabled = !this.isEditEnabled() || !this.isEditable(p);
      if(!object[p.name]) {
        //value is undefined
      } else if (isDateProperty(p)) {
        value = moment(object[p.name].value);
      } else if (isBlob(p) && object[p.name].value) {
        const portofinoBlob = object[p.name].value;
        value = new BlobFile();
        value.code = portofinoBlob.code;
        value.size = portofinoBlob.size;
        value.name = portofinoBlob.filename;
        value.type = portofinoBlob.contentType;
        value = [value];
      } else if(disabled && !isBooleanProperty(p) && object[p.name].displayValue) {
        value = object[p.name].displayValue;
      } else {
        value = object[p.name].value;
      }
      const field = new Field();
      field.property = p;
      field.initialState = { value: value, disabled: disabled };
      field.editable = !disabled;
      this.formDefinition.contents.push(field);
    });
    this.formDefinition.editable = this.isEditEnabled();
    if(this.formComponent) {
      this.formComponent.form = this.formDefinition;
    }
  }

  onFormReset() {
    if(this.isEditEnabled()) {
      this.setupSelectionProviders();
    }
  }

  protected setupSelectionProviders() {
    this.selectionProviders.forEach(sp => {
      sp.fieldNames.forEach((name, index) => {
        const property = this.properties.find(p => p.name == name);
        if (!property) {
          return;
        }
        const spUrl = `${this.sourceUrl}/:selectionProvider/${sp.name}/${index}`;
        property.selectionProvider = {
          name: sp.name,
          index: index,
          displayMode: sp.displayMode,
          url: spUrl,
          nextProperty: null,
          updateDependentOptions: () => {
            const nextProperty = property.selectionProvider.nextProperty;
            if (nextProperty) {
              this.loadSelectionOptions(this.properties.find(p => p.name == nextProperty));
            }
          },
          loadOptions: value => {
            this.loadSelectionOptions(property, value);
          },
          options: []
        };
        const control = this.form.get(property.name);
        if (control.enabled) {
          const value = this.object[property.name];
          if (value && value.value != null) {
            control.setValue({v: value.value, l: value.displayValue});
          }
        }
        if (index < sp.fieldNames.length - 1) {
          property.selectionProvider.nextProperty = sp.fieldNames[index + 1];
        }
      });
    });
  }

  protected loadSelectionOptions(property: Property, autocomplete: string = null) {
    const url = property.selectionProvider.url;
    let params = new HttpParams();
    if(property.selectionProvider.displayMode == 'AUTOCOMPLETE') {
      if(autocomplete) {
        params = params.set(`labelSearch`, autocomplete);
      } else {
        this.setSelectionOptions(property, []);
        return;
      }
    }
    this.http.get<SelectionOption[]>(url, { params: params }).subscribe(
      options => {
        this.setSelectionOptions(property, options);
      });
  }

  protected setSelectionOptions(property: Property, options: SelectionOption[]) {
    property.selectionProvider.options = options;
    this.clearDependentSelectionValues(property);
    let selected = options.find(o => o.s);
    const control = this.form.get(property.name);
    if(!selected && control.value) {
      selected = options.find(o => o.v == control.value.v);
    }
    if (selected) {
      control.setValue(selected);
    }
  }

  protected clearDependentSelectionValues(property: Property) {
    const nextProperty = property.selectionProvider.nextProperty;
    if (nextProperty) {
      this.clearSelectionValues(this.properties.find(p => p.name == nextProperty));
    }
  }

  protected clearSelectionValues(property: Property) {
    this.form.get(property.name).setValue(null);
    property.selectionProvider.options = [];
    const nextProperty = property.selectionProvider.nextProperty;
    if(nextProperty) {
      this.clearSelectionValues(this.properties.find(p => p.name == nextProperty));
    }
  }

  protected getObjectToSave(): any {
    let formData = new FormData();
    this.properties.filter(p => p.editable).forEach(p => {
      let value = this.form.get(p.name).value;
      if(p.selectionProvider && value) {
        value = value.v;
      }
      if(value == null) {
        value = "";
      }
      if (isDateProperty(p) && value) {
        formData.append(p.name, value.valueOf());
      } else if(isBlob(p)) {
        if(value && value.length > 0) {
          const file = value[0];
          if(file.code) {
            formData.append(p.name + '_operation', '_keep');
            formData.append(p.name + '_code', file.code);
          } else {
            formData.append(p.name + '_operation', '_modify');
            formData.append(p.name, file.slice(), file.name);
          }
        } else {
          formData.append(p.name + '_operation', '_delete');
        }
      } else {
        formData.append(p.name, value);
      }
    });
    return formData;
  }

  protected triggerValidationForAllFields(control: AbstractControl) {
    if (control instanceof FormControl) {
      control.markAsTouched({ onlySelf: true });
    } else if (control instanceof FormGroup) {
      Object.keys(control.controls).forEach(field => {
        this.triggerValidationForAllFields(control.get(field));
      });
    } else if(control instanceof FormArray) {
      control.controls.forEach(c => this.triggerValidationForAllFields(c));
    }

  }

  @Button({
    color: 'primary', icon: 'save', text: 'Save',
    presentIf: BaseDetailComponent.isSaveButtonPresent, enabledIf: BaseDetailComponent.isSaveButtonEnabled
  })
  save(): void {
    if(this.saving) {
      return;
    }
    this.saving = true;
    if(!this.isFormValid()) {
      this.triggerValidationForAllFields(this.form);
      this.notificationService.error('There are validation errors').subscribe();
      this.saving = false;
      return;
    }
    let object = this.getObjectToSave();
    this.doSave(object).subscribe(
      () =>  {
        this.saving = false;
        this.close.emit(object);
      },
      (error) => {
        this.saving = false;
        if(error.status == 500 && error.error) { //TODO introduce a means to check that it is actually a validation error
          let errorsFound = 0;
          for(let p in error.error) {
            let property = error.error[p];
            if(property.errors) {
              let control = this.form.controls[p];
              control.markAsTouched({ onlySelf: true });
              control.setErrors({ 'server-side': property.errors }, { emitEvent: false });
              errorsFound++;
            }
          }
          if(errorsFound > 0) {
            this.notificationService.error('There are validation errors').subscribe();
            this.changeDetector.detectChanges();
          }
        }
      });
  }

  static isSaveButtonPresent(self: BaseDetailComponent) {
    return self.isEditEnabled();
  }

  static isSaveButtonEnabled(self: BaseDetailComponent) {
    return self.isFormValid();
  }

  getButtons(list = 'default') {
    return getButtons(this, list);
  }

  protected abstract doSave(object): Observable<Object>;

  protected isFormValid(): boolean {
    return this.form.valid;
  }
}

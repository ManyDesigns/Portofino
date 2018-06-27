import {EventEmitter, Input, Output} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {PortofinoService} from "../portofino.service";
import {Configuration, SelectionOption, SelectionProvider} from "./crud.component";
import {ClassAccessor, getAnnotation, isEnabled, isRequired, Property} from "../class-accessor";
import * as moment from "moment";
import {AbstractControl, FormControl, FormGroup, Validators} from "@angular/forms";
import {debounceTime} from "rxjs/operators";

export abstract class BaseDetailComponent {

  @Input()
  classAccessor: ClassAccessor;
  @Input()
  selectionProviders: SelectionProvider[];
  @Input()
  configuration: Configuration;
  @Output()
  close = new EventEmitter();

  form: FormGroup;
  properties: Property[] = [];
  object;

  protected constructor(protected http: HttpClient, protected portofino: PortofinoService) { }

  protected initClassAccessor() {
    this.classAccessor.properties.forEach(property => {
      if (!isEnabled(property)) {
        return;
      }
      property = {...property};
      this.properties.push(property);
      property.editable = this.isEditable(property);
    });
  }

  protected abstract isEditable(property: Property): boolean;

  protected abstract isEditEnabled(): boolean;

  protected setupForm(object) {
    this.object = object;
    const formControls = {};
    this.properties.forEach(p => {
      let value;
      const disabled = !this.isEditEnabled() || !this.isEditable(p);
      if(!object[p.name]) {
        //value is undefined
      } else if (this.portofino.isDate(p)) {
        value = moment(object[p.name].value);
      } else if(disabled && object[p.name].displayValue) {
        value = object[p.name].displayValue;
      } else {
        value = object[p.name].value;
      }
      const formState = { value: value, disabled: disabled };
      if(this.form) {
        this.form.get(p.name).reset(formState);
      } else {
        formControls[p.name] = new FormControl(formState, this.getValidators(p));
      }
    });
    if(!this.form) {
      this.form = new FormGroup(formControls);
    }

    if(!this.isEditEnabled()) {
      return;
    }
    this.selectionProviders.forEach(sp => {
      sp.fieldNames.forEach((name, index) => {
        const property = this.properties.find(p => p.name == name);
        if(!property) {
          return;
        }
        const spUrl = `${this.portofino.apiPath + this.configuration.source}/:selectionProvider/${sp.name}/${index}`;
        property.selectionProvider = {
          name: sp.name,
          index: index,
          displayMode: sp.displayMode,
          url: spUrl,
          nextProperty: null,
          updateDependentOptions: () => {
            const nextProperty = property.selectionProvider.nextProperty;
            if(nextProperty) {
              this.loadSelectionOptions(this.properties.find(p => p.name == nextProperty));
            }
          },
          options: []
        };
        if(property.selectionProvider.displayMode == 'AUTOCOMPLETE') {
          const autocomplete = this.form.get(property.name);
          const value = this.object[property.name];
          autocomplete.setValue({ v: value.value, l: value.displayValue });
          autocomplete.valueChanges.pipe(debounceTime(500)).subscribe(value => {
            if(autocomplete.dirty && value != null && value.hasOwnProperty("length")) {
              this.loadSelectionOptions(property, value);
            }
          });
        } else if(index == 0) {
          this.loadSelectionOptions(property);
        }
        if(index < sp.fieldNames.length - 1) {
          property.selectionProvider.nextProperty = sp.fieldNames[index + 1];
        }
      });
    });
  }

  protected getValidators(property: Property) {
    let validators = [];
    if (isRequired(property)) {
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

  protected loadSelectionOptions(property: Property, autocomplete: string = null) {
    const url = property.selectionProvider.url;
    let params = new HttpParams();
    if(property.selectionProvider.displayMode == 'AUTOCOMPLETE') {
      if(autocomplete) {
        params = params.set(`labelSearch`, autocomplete);
      } else {
        this.setSelectOptions(property, []);
        return;
      }
    }
    this.http.get<SelectionOption[]>(url, { params: params }).subscribe(
      options => {
        this.setSelectOptions(property, options);
      });
  }

  protected setSelectOptions(property: Property, options) {
    property.selectionProvider.options = options;
    this.clearDependentSelectionValues(property);
    const selected = options.find(o => o.s);
    if (selected) {
      this.form.get(property.name).setValue(selected.v);
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
    let object = {};
    this.properties.filter(p => p.editable).forEach(p => {
      let value = this.form.get(p.name).value;
      if(p.selectionProvider && value) {
        value = value.v;
      }
      if (this.portofino.isDate(p) && value) {
        object[p.name] = value.valueOf();
      } else {
        object[p.name] = value;
      }
    });
    return object;
  }
}

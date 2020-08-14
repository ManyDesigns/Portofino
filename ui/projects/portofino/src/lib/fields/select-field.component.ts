import {Component, Input} from '@angular/core';
import {FieldComponent} from "./field.component";
import {debounceTime} from "rxjs/operators";
import {FormGroup} from "@angular/forms";

@Component({
  selector: 'portofino-select-field',
  templateUrl: '../../../assets/fields/select-field.component.html'
})
export class SelectFieldComponent extends FieldComponent {

  @Input()
  debounceTime = 500;

  ngOnInit() {
    super.ngOnInit();
    if (this.property.selectionProvider.displayMode == 'AUTOCOMPLETE') {
      this.control.valueChanges.pipe(debounceTime(this.debounceTime)).subscribe(value => {
        if (this.control.dirty && value != null && value.hasOwnProperty("length")) {
          this.property.selectionProvider.loadOptions(value);
        }
      });
    } else if(this.enabled && this.property.selectionProvider.index == 0) {
      this.property.selectionProvider.loadOptions();
    }
  }

  getOptionLabel(option) {
    if (option && option.l) {
      return option.l;
    }
    return option;
  }

  trackByOptionValue(index, option) {
    return option.v;
  }

  safeValue(form: FormGroup) {
    if(!form || !form.controls || !form.controls[this.property.name] || !form.controls[this.property.name].value) {
      return '';
    }
    const value = form.controls[this.property.name].value;
    return value.hasOwnProperty('l') ? value.l : value
  }
}

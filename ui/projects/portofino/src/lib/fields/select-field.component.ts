import {Component, Input} from '@angular/core';
import {FieldComponent} from "./field.component";
import {debounceTime} from "rxjs/operators";

@Component({
  selector: 'portofino-select-field',
  templateUrl: './select-field.component.html'
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
}

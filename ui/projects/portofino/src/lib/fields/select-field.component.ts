import {Component} from '@angular/core';
import {FieldComponent} from "./field.component";

@Component({
  selector: 'portofino-select-field',
  templateUrl: './select-field.component.html'
})
export class SelectFieldComponent extends FieldComponent {

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

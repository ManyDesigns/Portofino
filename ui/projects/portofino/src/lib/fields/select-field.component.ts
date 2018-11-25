import {Component} from '@angular/core';
import {FieldComponent} from "./field.component";
import {PortofinoService} from "../portofino.service";
import {AuthenticationService} from "../security/authentication.service";

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

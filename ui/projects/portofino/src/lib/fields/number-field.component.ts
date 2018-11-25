import {Component} from '@angular/core';
import {FieldComponent} from "./field.component";
import {PortofinoService} from "../portofino.service";
import {AuthenticationService} from "../security/authentication.service";

@Component({
  selector: 'portofino-number-field',
  templateUrl: './number-field.component.html'
})
export class NumberFieldComponent extends FieldComponent {}

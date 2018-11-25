import {Component} from '@angular/core';
import {FieldComponent} from "./field.component";
import {PortofinoService} from "../portofino.service";
import {AuthenticationService} from "../security/authentication.service";

@Component({
  selector: 'portofino-boolean-field',
  templateUrl: './boolean-field.component.html'
})
export class BooleanFieldComponent extends FieldComponent {}

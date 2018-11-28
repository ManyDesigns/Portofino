import {Component} from '@angular/core';
import {FieldComponent} from "./field.component";
import {isMultiline, isPassword, isRichText} from "../class-accessor";

@Component({
  selector: 'portofino-text-field',
  templateUrl: './text-field.component.html',
  styleUrls: ['./text-field.component.css']
})
export class TextFieldComponent extends FieldComponent {

  isMultiline() {
    return isMultiline(this.property);
  }

  isPassword() {
    return isPassword(this.property);
  }

  isRichText() {
    return isRichText(this.property);
  }
}

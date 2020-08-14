import {Component} from '@angular/core';
import {FieldComponent} from "./field.component";
import {getAnnotation, isMultiline, isPassword, isRichText} from "../class-accessor";
import { ErrorStateMatcher } from "@angular/material/core";
import {FormControl, FormGroupDirective, NgForm} from "@angular/forms";

@Component({
  selector: 'portofino-text-field',
  templateUrl: '../../../assets/fields/text-field.component.html',
  styleUrls: ['../../../assets/fields/text-field.component.scss']
})
export class TextFieldComponent extends FieldComponent {

  isMultiline() {
    return isMultiline(this.property);
  }

  isPassword() {
    return isPassword(this.property);
  }

  isPasswordConfirmationRequired() {
    if(this.control.disabled || this.selectable) {
      return false;
    }
    const annotation = getAnnotation(this.property, "com.manydesigns.elements.annotations.Password");
    return annotation ? annotation.properties.confirmationRequired : false
  }

  isRichText() {
    return isRichText(this.property);
  }

  readonly crossFieldErrorMatcher: ErrorStateMatcher = {
    isErrorState(control: FormControl | null, form: FormGroupDirective | NgForm | null): boolean {
      return control.dirty && form.invalid;
    }
  };
}

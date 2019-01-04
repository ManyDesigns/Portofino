import {Component, Directive, ElementRef, forwardRef, Input, Renderer2} from "@angular/core";
import {FieldComponent} from "./field.component";
import {getAnnotation} from "../class-accessor";
import {
  AbstractControl,
  ControlValueAccessor,
  NG_VALIDATORS,
  NG_VALUE_ACCESSOR,
  ValidationErrors,
  Validator
} from "@angular/forms";
import moment from "moment-with-locales-es6";

@Component({
  selector: 'portofino-date-time-field',
  templateUrl: './date-time-field.component.html'
})
export class DateTimeFieldComponent extends FieldComponent {

  isDateOnly() {
    const df = getAnnotation(this.property, 'com.manydesigns.elements.annotations.DateFormat');
    //This mirrors the logic in AbstractDateField.java
    if(!df) {
      return true;
    }
    const datePattern = df.properties["value"] as string;
    if(!datePattern) {
      return true;
    }
    return datePattern.indexOf("HH") < 0 && datePattern.indexOf("mm") < 0 && datePattern.indexOf("ss") < 0;
  }

  get dateFormat() {
    const df = getAnnotation(this.property, 'com.manydesigns.elements.annotations.DateFormat');
    return this.convertDateFormat(df ? df.properties["value"] as string : null);
  }

  protected convertDateFormat(format: string) {
    if(!format) {
      return 'DD/MM/YYYY';
    }
    return format.replace(/y/g, "Y").replace(/d/g, "D");
  }

}

@Directive({
  selector: '[dateTimeField]',
  host: {
    '(input)': '$any(this).handleInput($event.target.value)',
    '(blur)': 'onTouched()',
  },
  providers: [
    { provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => DateTimeValueAccessor ), multi: true },
    { provide: NG_VALIDATORS, useExisting: forwardRef(() => DateTimeValueAccessor ), multi: true }]
})
export class DateTimeValueAccessor implements ControlValueAccessor, Validator {

  onChange = (_: any) => {};
  onTouched = () => {};

  @Input()
  dateFormat: string;

  constructor(protected renderer: Renderer2, protected elementRef: ElementRef) {}

  writeValue(value: any): void {
    const formatted = value ? value.format(this.dateFormat) : '';
    this.renderer.setProperty(this.elementRef.nativeElement, 'value', formatted);
  }

  registerOnChange(fn: (_: any) => void): void { this.onChange = fn; }
  registerOnTouched(fn: () => void): void { this.onTouched = fn; }

  setDisabledState(isDisabled: boolean): void {
    this.renderer.setProperty(this.elementRef.nativeElement, 'disabled', isDisabled);
  }

  handleInput(value: any): void {
    this.onChange(moment(value, this.dateFormat, true));
  }

  registerOnValidatorChange(fn: () => void): void {}

  validate(control: AbstractControl): ValidationErrors | null {
    if(control.value && !control.value.isValid()) {
      return {
        'date-format': this.dateFormat
      };
    } else {
      return null;
    }
  }
}

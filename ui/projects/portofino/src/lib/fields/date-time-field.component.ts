import {Component, Directive, ElementRef, forwardRef, Host, Input, Optional, Renderer2, SkipSelf} from "@angular/core";
import {FieldComponent} from "./field.component";
import {getAnnotation} from "../class-accessor";
import {
  AbstractControl, ControlContainer,
  ControlValueAccessor,
  NG_VALIDATORS,
  NG_VALUE_ACCESSOR,
  ValidationErrors,
  Validator
} from "@angular/forms";
import { DateTime } from "luxon";
import {PortofinoService} from "../portofino.service";

@Component({
  selector: 'portofino-date-time-field',
  templateUrl: '../../../assets/fields/date-time-field.component.html'
})
export class DateTimeFieldComponent extends FieldComponent {

  constructor(
    @Optional() @Host() @SkipSelf() controlContainer: ControlContainer,
    protected portofino: PortofinoService) {
    super(controlContainer);
  }

  isDateOnly() {
    const df = getAnnotation(this.property, 'com.manydesigns.elements.annotations.DateFormat');
    //This mirrors the logic in AbstractDateField.java
    if(!df) {
      return true;
    }
    const datePattern = df.properties.value as string;
    if(!datePattern) {
      return true;
    }
    return datePattern.indexOf("HH") < 0 && datePattern.indexOf("mm") < 0 && datePattern.indexOf("ss") < 0;
  }

  get dateFormat() {
    const df = getAnnotation(this.property, 'com.manydesigns.elements.annotations.DateFormat');
    return DateTimeFieldComponent.convertDateFormat(df ? df.properties.value as string : null);
  }

  get locale() {
    return this.portofino.currentLocale.key;
  }

  public static convertDateFormat(format: string) {
    if(!format) {
      return 'dd/MM/yyyy';
    }
    return format.replace(/Y/g, "y").replace(/D/g, "d");
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

  @Input()
  locale: string;

  constructor(protected renderer: Renderer2, protected elementRef: ElementRef) {}

  writeValue(value: any): void {
    let formatted;
    if(value) {
      if(this.locale) {
        value = value.setLocale(this.locale);
      }
      formatted = value.toFormat(this.dateFormat);
    } else {
      formatted = "";
    }
    this.renderer.setProperty(this.elementRef.nativeElement, 'value', formatted);
  }

  registerOnChange(fn: (_: any) => void): void { this.onChange = fn; }
  registerOnTouched(fn: () => void): void { this.onTouched = fn; }

  setDisabledState(isDisabled: boolean): void {
    this.renderer.setProperty(this.elementRef.nativeElement, 'disabled', isDisabled);
  }

  handleInput(value: any): void {
    this.onChange(DateTime.fromFormat(value, this.dateFormat));
  }

  registerOnValidatorChange(fn: () => void): void {}

  validate(control: AbstractControl): ValidationErrors | null {
    if(control.value?.invalidReason) {
      return {
        'date-format': this.dateFormat
      };
    } else {
      return null;
    }
  }
}

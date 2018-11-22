import {
  Component,
  Directive,
  ElementRef,
  forwardRef,
  Inject,
  InjectionToken,
  Input,
  OnInit,
  Renderer2,
  Type
} from '@angular/core';
import {getAnnotation, isMultiline, isPassword, isRequired, isRichText, Property} from "../class-accessor";
import {PortofinoService} from "../portofino.service";
import {
  ControlValueAccessor,
  FormControl,
  FormGroup, NG_VALIDATORS,
  NG_VALUE_ACCESSOR, ValidationErrors,
  Validator
} from "@angular/forms";
import {AbstractControl} from "@angular/forms/src/model";
import moment from 'moment-es6'

export const FIELD_FACTORY = new InjectionToken('Field Factory');

@Component({
  selector: 'portofino-field',
  templateUrl: './field.component.html',
  styleUrls: ['./field.component.css']
})
export class FieldComponent implements OnInit {

  @Input()
  enabled: boolean = true;
  @Input()
  property: Property;
  @Input()
  form: FormGroup;
  @Input()
  selectable: boolean;
  @Input()
  objectUrl: string;

  control: AbstractControl;
  selector: FormControl;
  @Input()
  context = {};

  constructor(public portofino: PortofinoService, @Inject(FIELD_FACTORY) public factory) { }

  getOptionLabel(option) {
    if (option && option.l) {
      return option.l;
    }
    return option;
  }

  isRequired() {
    return isRequired(this.property);
  }

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

  trackByOptionValue(index, option) {
    return option.v;
  }

  ngOnInit() {
    this.control = this.form.get(this.property.name);
    if(this.selectable && this.enabled) {
      this.selector = new FormControl();
      const validator = this.control.validator;
      this.control.clearValidators();
      this.control.updateValueAndValidity();
      this.selector.valueChanges.subscribe(v => {
        if(v) {
          this.control.setValidators(validator);
        } else {
          this.control.clearValidators();
        }
        this.property.editable = v;
        this.control.updateValueAndValidity({ emitEvent: false });
        this.control.markAsDirty();
      });
      this.control.valueChanges.subscribe(ch => {
        if((ch !== undefined && ch !== null) ||
           (this.selector.value !== undefined && this.selector.value !== null)) {
          this.selector.setValue(this.enabled);
        }
      });
    }
  }

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

@Directive({
  selector: '[dateTimeField]',
  host: {
    '(input)': '$any(this).handleInput($event.target.value)',
    '(blur)': 'onTouched()',
  },
  providers: [
    { provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => CustomDateTimeAccessor ), multi: true },
    { provide: NG_VALIDATORS, useExisting: forwardRef(() => CustomDateTimeAccessor ), multi: true }]
})
export class CustomDateTimeAccessor implements ControlValueAccessor, Validator {

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

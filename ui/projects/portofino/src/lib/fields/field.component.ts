import {
  Component,
  Directive,
  ElementRef,
  forwardRef, Host,
  Inject,
  InjectionToken,
  Input,
  OnInit, Optional,
  Renderer2, SkipSelf, Type
} from '@angular/core';
import {isMultiline, isPassword, isRequired, isRichText, Property} from "../class-accessor";
import {PortofinoService} from "../portofino.service";
import {
  AbstractFormGroupDirective,
  ControlContainer,
  ControlValueAccessor,
  FormControl,
  FormGroup, FormGroupDirective, NG_VALIDATORS,
  NG_VALUE_ACCESSOR, ValidationErrors,
  Validator
} from "@angular/forms";
import {AbstractControl} from "@angular/forms/src/model";
import moment from 'moment-es6'

export const FIELD_FACTORY = new InjectionToken('Field Factory');

@Component({
  selector: 'portofino-field',
  templateUrl: './field.component.html'
})
export class FieldComponent implements OnInit {

  @Input()
  enabled: boolean = true;
  @Input()
  property: Property;
  @Input()
  form: FormGroup;
  @Input()
  selectable: boolean = false;

  control: AbstractControl;
  @Input()
  selector: FormControl;

  @Input()
  type: Type<any>;
  @Input()
  context = {};
  field: FieldComponent;

  get fieldComponentType() {
    return this.type ? this.type : this.factory ? this.factory.get(this) : null;
  }

  constructor(@Inject(FIELD_FACTORY) public factory,
              @Optional() @Host() @SkipSelf() protected controlContainer: ControlContainer) {}

  isRequired() {
    return isRequired(this.property);
  }

  ngOnInit() {
    if(!this.form) {
      if(this.controlContainer instanceof AbstractFormGroupDirective ||
         this.controlContainer instanceof FormGroupDirective) {
        this.form = this.controlContainer.control;
      } else {
        throw "A form must be provided to field " + this.property.name
      }
    }
    this.control = this.form.get(this.property.name);
    if(!this.control) {
      throw "No control found named " + this.property.name
    }
    if(!this.selector && this.selectable && this.enabled) {
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

}

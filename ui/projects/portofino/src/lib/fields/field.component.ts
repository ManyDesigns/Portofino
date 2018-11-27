import {
  Component,
  Directive,
  ElementRef,
  forwardRef, Host,
  Inject,
  InjectionToken,
  Input,
  OnInit, Optional,
  Renderer2, SkipSelf
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

export class FormElement {

}

@Component({
  selector: 'portofino-field',
  templateUrl: './field.component.html',
  providers: [{ provide: FormElement, useExisting: forwardRef(() => FieldComponent ) }]
})
export class FieldComponent extends FormElement implements OnInit {

  @Input()
  enabled: boolean = true;
  @Input()
  property: Property;
  @Input()
  form: FormGroup;
  @Input()
  selectable: boolean = false;

  control: AbstractControl;
  selector: FormControl;
  @Input()
  context = {};

  constructor(public portofino: PortofinoService, @Inject(FIELD_FACTORY) public factory,
              @Optional() @Host() @SkipSelf() protected controlContainer: ControlContainer) {
    super();
  }

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

}

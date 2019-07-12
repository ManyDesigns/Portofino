import {Component, Input, OnInit} from '@angular/core';
import {isDateProperty, isNumericProperty, isRequired, Property} from "../../class-accessor";
import {PortofinoService} from "../../portofino.service";
import {FormControl, FormGroup} from "@angular/forms";
import {debounceTime} from "rxjs/operators";

@Component({
  selector: 'portofino-crud-search-field',
  templateUrl: './search-field.component.html',
  styleUrls: ['./search-field.component.css']
})
export class SearchFieldComponent implements OnInit {

  @Input()
  property: Property;
  @Input()
  form: FormGroup;
  @Input()
  debounceTime = 500;

  private _ranged = false;

  get ranged() {
    return this._ranged;
  }

  set ranged(value: boolean) {
    this._ranged = value;
    const group = this.form.get(this.property.name) as FormGroup;
    if(value) {
      group.get('exact').reset();
    } else {
      group.get('min').reset();
      group.get('max').reset();
    }
  }

  toggleRanged() {
    this.ranged = !this.ranged;
  }

  constructor(public portofino: PortofinoService) { }

  getOptionLabel(option) {
    return option ? option.l : null;
  }

  get required() {
    return isRequired(this.property);
  }

  ngOnInit() {
    if(this.rangeSearchSupported) {
      const group = new FormGroup({
        exact: new FormControl(), min: new FormControl(), max: new FormControl()
      });
      this.form.setControl(this.property.name, group);
    } else {
      const control = new FormControl();
      this.form.setControl(this.property.name, control);
      if (this.property.selectionProvider) {
        if (this.property.selectionProvider.displayMode == 'AUTOCOMPLETE') {
          control.valueChanges.pipe(debounceTime(this.debounceTime)).subscribe(value => {
            if (control.dirty && value != null && value.hasOwnProperty("length")) {
              this.property.selectionProvider.loadOptions(value);
            }
          });
        } else if(this.property.selectionProvider.index == 0) {
          this.property.selectionProvider.loadOptions();
        }
      }
    }
  }

  get rangeSearchSupported() {
    return !this.property.selectionProvider && (isNumericProperty(this.property) || isDateProperty(this.property));
  }

}

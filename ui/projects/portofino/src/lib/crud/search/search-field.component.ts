import {Component, Input, OnInit} from '@angular/core';
import {isRequired, Property} from "../../class-accessor";
import {PortofinoService} from "../../portofino.service";
import {FormGroup} from "@angular/forms";
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

  constructor(public portofino: PortofinoService) { }

  getOptionLabel(option) {
    return option ? option.l : null;
  }

  get required() {
    return isRequired(this.property);
  }

  ngOnInit() {
    if (this.property.selectionProvider) {
      if (this.property.selectionProvider.displayMode == 'AUTOCOMPLETE') {
        const control = this.form.get(this.property.name);
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

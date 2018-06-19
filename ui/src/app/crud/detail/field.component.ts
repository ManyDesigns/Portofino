import {Component, Input, OnInit} from '@angular/core';
import {Property} from "../../class-accessor";
import {PortofinoService} from "../../portofino.service";
import {FormGroup} from "@angular/forms";

@Component({
  selector: 'portofino-crud-field',
  templateUrl: './field.component.html',
  styleUrls: ['./field.component.css']
})
export class FieldComponent implements OnInit {

  @Input()
  property: Property;
  @Input()
  form: FormGroup;

  constructor(public portofino: PortofinoService) { }

  getOptionLabel(option) {
    return option ? option.l : null;
  }

  ngOnInit() {}

}

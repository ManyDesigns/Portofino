import {Component, Input, OnInit} from '@angular/core';
import {Property} from "../../class-accessor";
import {PortofinoService} from "../../portofino.service";

@Component({
  selector: 'portofino-crud-field',
  templateUrl: './field.component.html',
  styleUrls: ['./field.component.css']
})
export class FieldComponent implements OnInit {

  @Input()
  property: Property;

  @Input()
  object;

  constructor(public portofino: PortofinoService) { }

  ngOnInit() {
  }

}

import {Component, Input, OnInit} from '@angular/core';
import {Property} from "../../class-accessor";
import {PortofinoService} from "../../portofino.service";
import {FormGroup} from "@angular/forms";

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

  constructor(public portofino: PortofinoService) { }

  ngOnInit() {
  }

}

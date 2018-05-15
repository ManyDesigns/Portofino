import {Component, Input, OnInit} from '@angular/core';
import {Property} from "../class-accessor";
import {CrudComponent} from "../crud.component";

@Component({
  selector: 'portofino-crud-search-field',
  templateUrl: './search-field.component.html',
  styleUrls: ['./search-field.component.css']
})
export class SearchFieldComponent implements OnInit {

  @Input()
  property: Property;

  @Input()
  parent: CrudComponent;

  constructor() { }

  ngOnInit() {
  }

}

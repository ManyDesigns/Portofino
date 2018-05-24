import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Property} from "../../class-accessor";
import {CrudComponent} from "../crud.component";
import {PortofinoService} from "../../portofino.service";
import {SearchComponent} from "./search.component";

@Component({
  selector: 'portofino-crud-search-field',
  templateUrl: './search-field.component.html',
  styleUrls: ['./search-field.component.css']
})
export class SearchFieldComponent implements OnInit {

  @Input()
  property: Property;

  @Input()
  searchQuery;

  constructor(public portofino: PortofinoService) { }

  ngOnInit() {
  }

}

import {Component, Input, OnInit} from '@angular/core';
import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {PortofinoService} from "../portofino.service";
import {ClassAccessor, isSearchable, Property} from "./class-accessor";

@Component({
  selector: 'portofino-crud',
  templateUrl: './crud.component.html',
  styleUrls: ['./crud.component.css']
})
export class CrudComponent implements OnInit {

  @Input() config: any;

  classAccessor: ClassAccessor;
  classAccessorPath = '/:classAccessor';

  searchFields: Property[];
  searchValues = {};

  constructor(private http: HttpClient, public portofino: PortofinoService) { }

  ngOnInit() {
    this.http.get<ClassAccessor>(this.portofino.apiPath + this.config.path + this.classAccessorPath).subscribe(
      classAccessor => this.init(classAccessor)
    );
  }

  protected init(classAccessor: ClassAccessor) {
    this.classAccessor = classAccessor;
    this.searchFields = [];
    this.classAccessor.properties.forEach(property => {
      if(isSearchable(property)) {
        this.searchFields.push(property);
        console.log("searchable", property);
      }
    });
  }

  getFieldId(prefix: string, field: Property) {
    return `crud-${prefix}-${this.classAccessor.name}-${field.name}`
  }

  search() {
    console.log("search", this.searchValues)
  }

  clearSearch() {
    this.searchValues = {}
  }

}

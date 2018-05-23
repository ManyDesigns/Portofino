import {Component, Input, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {PortofinoService} from "../portofino.service";
import {ClassAccessor, Property} from "../class-accessor";

@Component({
  selector: 'portofino-crud',
  templateUrl: './crud.component.html',
  styleUrls: ['./crud.component.css']
})
export class CrudComponent implements OnInit {

  @Input()
  config: any;

  configuration: Configuration;
  classAccessor: ClassAccessor;
  classAccessorPath = '/:classAccessor';
  configurationPath = '/:configuration';

  @Input()
  pageSize: number;

  searchVisible = true;
  createVisible = false;

  constructor(private http: HttpClient, public portofino: PortofinoService) { }

  ngOnInit() {
    const baseUrl = this.portofino.apiPath + this.config.path;
    this.http.get<ClassAccessor>(baseUrl + this.classAccessorPath).subscribe(
      classAccessor => this.http.get<Configuration>(baseUrl + this.configurationPath).subscribe(
        configuration => {
          this.classAccessor = classAccessor;
          this.configuration = {...this.config, ...configuration};
        }
      )
    );
  }

  getFieldId(prefix: string, field: Property) {
    return `crud-${prefix}-${this.classAccessor.name}-${field.name}`
  }

}

class Configuration {
  rowsPerPage: number;
  path: string;
}

import {Component, Input, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {PortofinoService} from "../portofino.service";
import {ClassAccessor, isEnabled, isInSummary, isSearchable, isUpdatable, Property} from "../class-accessor";
import {PageConfiguration, PortofinoComponent} from "../portofino.component";

@Component({
  selector: 'portofino-crud',
  templateUrl: './crud.component.html',
  styleUrls: ['./crud.component.css']
})
export class CrudComponent implements OnInit {

  private static __componentRegistration = PortofinoComponent.components.crud = CrudComponent;

  @Input()
  configuration: any;

  classAccessor: ClassAccessor;
  classAccessorPath = '/:classAccessor';
  configurationPath = '/:configuration';

  @Input()
  pageSize: number;

  searchVisible = false;
  createVisible = false;
  editVisible = false;

  id: string;

  constructor(private http: HttpClient, public portofino: PortofinoService) { }

  ngOnInit() {
    const baseUrl = this.portofino.apiPath + this.configuration.source;
    this.http.get<ClassAccessor>(baseUrl + this.classAccessorPath).subscribe(
      classAccessor => this.http.get<Configuration>(baseUrl + this.configurationPath).subscribe(
        configuration => this.init(classAccessor, configuration)
      )
    );
  }

  protected init(classAccessor, configuration) {
    this.classAccessor = classAccessor;
    this.configuration = {...configuration, ...this.configuration};
    this.classAccessor.properties.forEach(p => {
      p.key = (this.classAccessor.keyProperties.find(k => k == p.name) != null);
    });
    this.searchVisible = true;
  }

  createNew() {
    this.searchVisible = false;
    this.editVisible = false;
    this.createVisible = true;
  }

  openDetail(id: string) {
    this.id = id;
    this.searchVisible = false;
    this.editVisible = true;
    this.createVisible = false;
  }

  closeDetail() {
    this.searchVisible = true;
    this.createVisible = false;
    this.editVisible = false;
    this.id = null;
  }
}

export class Configuration {
  rowsPerPage: number;
  source: string;
}

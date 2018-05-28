import {Component, Input, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {PortofinoService} from "../portofino.service";
import {ClassAccessor, isEnabled, isInSummary, isSearchable, isUpdatable, Property} from "../class-accessor";
import {Page, PageConfiguration, PortofinoComponent} from "../portofino.component";
import {Router} from "@angular/router";

@Component({
  selector: 'portofino-crud',
  templateUrl: './crud.component.html',
  styleUrls: ['./crud.component.css']
})
export class CrudComponent extends Page implements OnInit {

  private static __componentRegistration = PortofinoComponent.components.crud = CrudComponent;

  @Input()
  configuration: PageConfiguration & any;

  classAccessor: ClassAccessor;
  classAccessorPath = '/:classAccessor';
  configurationPath = '/:configuration';

  @Input()
  pageSize: number;

  searchVisible = false;
  createVisible = false;
  editVisible = false;

  id: string;

  constructor(private http: HttpClient, public portofino: PortofinoService, private router: Router) {
    super();
  }

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
    if(this.id) {
      this.showDetail();
    } else {
      this.showSearch();
    }
  }

  showCreate() {
    this.searchVisible = false;
    this.editVisible = false;
    this.createVisible = true;
  }

  showDetail() {
    this.searchVisible = false;
    this.editVisible = true;
    this.createVisible = false;
  }

  showSearch() {
    this.searchVisible = true;
    this.createVisible = false;
    this.editVisible = false;
  }

  goToSearch() {
    this.router.navigateByUrl(this.path);
  }

  consumePathFragment(fragment: string): boolean {
    const child = this.configuration.children.find(c => c.path == fragment);
    if(child) {
      return true;
    }
    if(this.id) {
      this.id = `${this.id}/${fragment}`;
    } else {
      this.id = fragment;
    }
  }
}

export class Configuration {
  rowsPerPage: number;
  source: string;
}

import {Component, Input, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {PortofinoService} from "../portofino.service";
import {ClassAccessor} from "../class-accessor";
import {Page, PageChild, PageConfiguration, PortofinoComponent} from "../portofino.component";
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
  selectionProviders: SelectionProvider[];
  classAccessorPath = '/:classAccessor';
  configurationPath = '/:configuration';
  selectionProvidersPath = '/:selectionProviders';

  @Input()
  pageSize: number;

  searchVisible = false;
  createVisible = false;
  detailVisible = false;

  id: string;

  constructor(private http: HttpClient, public portofino: PortofinoService, private router: Router) {
    super();
  }

  ngOnInit() {
    const baseUrl = this.portofino.apiPath + this.configuration.source;
    this.http.get<ClassAccessor>(baseUrl + this.classAccessorPath).subscribe(
      classAccessor => this.http.get<Configuration>(baseUrl + this.configurationPath).subscribe(
        configuration => this.http.get<SelectionProvider[]>(baseUrl + this.selectionProvidersPath).subscribe(
          sps => this.init(classAccessor, configuration, sps))));
  }

  protected init(classAccessor, configuration, selectionProviders: SelectionProvider[]) {
    this.classAccessor = classAccessor;
    this.selectionProviders = selectionProviders;
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
    this.detailVisible = false;
    this.createVisible = true;
  }

  showDetail() {
    this.searchVisible = false;
    this.detailVisible = true;
    this.createVisible = false;
  }

  showSearch() {
    this.searchVisible = true;
    this.createVisible = false;
    this.detailVisible = false;
  }

  goToSearch() {
    if(this.detailVisible) {
      this.router.navigateByUrl(this.path);
    } else {
      this.showSearch();
    }
  }

  consumePathSegment(segment: string): boolean {
    const child = this.children.find(c => c.path == segment);
    if(child) {
      return true;
    }
    if(this.id) {
      this.id = `${this.id}/${segment}`;
    } else {
      this.id = segment;
    }
  }

  get children(): PageChild[] {
    if(this.id) {
      return this.configuration.detailChildren || []
    } else {
      return this.configuration.children
    }
  }
}

export class Configuration {
  rowsPerPage: number;
  source: string;
}

export class SelectionProvider {
  name: string;
  fieldNames: string[];
  displayMode: string;
  searchDisplayMode: string;
}

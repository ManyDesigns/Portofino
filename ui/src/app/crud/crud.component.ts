import {Component, Input, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {PortofinoService} from "../portofino.service";
import {ClassAccessor} from "../class-accessor";
import {Page, PageChild, PageConfiguration, PortofinoComponent} from "../portofino.component";
import {Router} from "@angular/router";
import {SelectionModel} from "@angular/cdk/collections";

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
  operationsPath = '/:operations';

  @Input()
  pageSize: number;

  view: CrudView;

  createEnabled: boolean;
  editEnabled: boolean;
  deleteEnabled: boolean;
  bulkEditEnabled: boolean;
  bulkDeleteEnabled: boolean;

  id: string;
  selection: string[];

  constructor(private http: HttpClient, public portofino: PortofinoService, private router: Router) {
    super();
  }

  ngOnInit() {
    const baseUrl = this.portofino.apiPath + this.configuration.source;
    this.http.get<ClassAccessor>(baseUrl + this.classAccessorPath).subscribe(
      classAccessor => this.http.get<Configuration>(baseUrl + this.configurationPath).subscribe(
        configuration => this.http.get<SelectionProvider[]>(baseUrl + this.selectionProvidersPath).subscribe(
          sps => this.init(classAccessor, configuration, sps))));
    this.http.get<Operation[]>(baseUrl + this.operationsPath).subscribe(ops => {
      this.createEnabled = ops.some(op => op.signature == "POST" && op.available);
      this.editEnabled = ops.some(op => op.signature == "PUT" && op.available);
      this.deleteEnabled = ops.some(op => op.signature == "DELETE" && op.available);
      this.bulkEditEnabled = ops.some(op => op.signature == "PUT :bulk?ids" && op.available);
      this.bulkDeleteEnabled = ops.some(op => op.signature == "DELETE :bulk?ids" && op.available);
    });

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
    this.view = CrudView.CREATE;
  }

  showDetail() {
    this.view = CrudView.DETAIL;
  }

  showSearch() {
    this.view = CrudView.SEARCH;
  }

  showBulkEdit(selection: string[]) {
    this.selection = selection;
    this.view = CrudView.BULK_EDIT;
  }

  isCreateView() {
    return this.view == CrudView.CREATE;
  }

  isDetailView() {
    return this.view == CrudView.DETAIL;
  }

  isSearchView() {
    return this.view == CrudView.SEARCH;
  }

  isBulkEditView() {
    return this.view == CrudView.BULK_EDIT;
  }

  goToSearch() {
    if(this.view == CrudView.DETAIL) {
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
  options: SelectionOption[];
}

export class SelectionOption {
  v: string;
  l: string;
  s: boolean;
}

export class Operation {
  name: string;
  signature: string;
  available: boolean;
}
 export enum CrudView {
   SEARCH, DETAIL, CREATE, BULK_EDIT
 }

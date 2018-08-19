import {Component, Input, OnInit, ViewChild} from '@angular/core';
import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {PortofinoService} from "../portofino.service";
import {ClassAccessor} from "../class-accessor";
import {PortofinoComponent} from "../portofino-app.component";
import {Router} from "@angular/router";
import {SearchComponent} from "./search/search.component";
import {Button, Operation, Page, PageChild, PageConfiguration} from "../page.component";
import {Configuration, SelectionProvider} from "./crud.common";

@Component({
  selector: 'portofino-crud',
  templateUrl: './crud.component.html',
  styleUrls: ['./crud.component.css']
})
@PortofinoComponent({ name: 'crud' })
export class CrudComponent extends Page implements OnInit {

  @Input()
  configuration: PageConfiguration & any;
  sourceUrl: string;

  classAccessor: ClassAccessor;
  selectionProviders: SelectionProvider[];
  classAccessorPath = '/:classAccessor';
  selectionProvidersPath = '/:selectionProviders';

  @Input()
  pageSize: number;
  @ViewChild(SearchComponent)
  search: SearchComponent;

  id: string;
  view: CrudView;

  createEnabled: boolean;
  bulkEditEnabled: boolean;
  bulkDeleteEnabled: boolean;

  selection: string[];

  constructor(protected http: HttpClient, public portofino: PortofinoService, private router: Router) {
    super(portofino, http);
  }

  ngOnInit() {
    this.sourceUrl = this.computeBaseSourceUrl();
    this.http.get<ClassAccessor>(this.sourceUrl + this.classAccessorPath).subscribe(
      classAccessor => this.http.get<Configuration>(this.sourceUrl + this.configurationPath).subscribe(
        configuration => this.http.get<SelectionProvider[]>(this.sourceUrl + this.selectionProvidersPath).subscribe(
          sps => this.init(classAccessor, configuration, sps))));
    this.http.get<Operation[]>(this.sourceUrl + this.operationsPath).subscribe(ops => {
      this.createEnabled = ops.some(op => op.signature == "POST" && op.available);
      this.bulkEditEnabled = ops.some(op => op.signature == "PUT" && op.available);
      this.bulkDeleteEnabled = ops.some(op => op.signature == "DELETE" && op.available);
    });
  }

  computeBaseSourceUrl() {
    return super.computeSourceUrl();
  }

  computeSourceUrl() {
    const baseSourceUrl = this.computeBaseSourceUrl();
    if(this.id) {
      return baseSourceUrl + '/' + this.id;
    } else {
      return baseSourceUrl;
    }
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

  static createEnabled(self: CrudComponent) {
    return self.createEnabled;
  }

  @Button({
    color: 'accent', presentIf: CrudComponent.createEnabled, icon: 'add', text: 'Create new'
  })
  showCreate() {
    this.allowEmbeddedComponents = false;
    this.view = CrudView.CREATE;
  }

  showDetail() {
    this.allowEmbeddedComponents = true;
    this.view = CrudView.DETAIL;
  }

  showSearch() {
    this.allowEmbeddedComponents = true;
    this.view = CrudView.SEARCH;
  }

  static bulkEditPresent(self: CrudComponent) {
    return self.bulkEditEnabled;
  }

  static bulkButtonsEnabled(self: CrudComponent) {
    return self.search && self.search.getSelectedIds().length > 0;
  }

  @Button({
    color: 'primary', icon: 'edit', text: 'Edit',
    presentIf: CrudComponent.bulkEditPresent,
    enabledIf: CrudComponent.bulkButtonsEnabled
  })
  showBulkEdit() {
    this.selection = this.search.getSelectedIds();
    this.allowEmbeddedComponents = false;
    this.view = CrudView.BULK_EDIT;
  }

  static bulkDeletePresent(self: CrudComponent) {
    return self.bulkDeleteEnabled;
  }

  @Button({
    color: 'warn', icon: 'delete', text: 'Delete',
    presentIf: CrudComponent.bulkDeletePresent,
    enabledIf: CrudComponent.bulkButtonsEnabled
  })
  bulkDelete() {
    let params = new HttpParams();
    this.search.getSelectedIds().forEach(id => params = params.append("id", id));
    this.http.delete(this.sourceUrl, { params: params }).subscribe(() => this.search.refreshSearch());
  }

  onEditModeChange(event) {
    this.allowEmbeddedComponents = !event;
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
      this.router.navigateByUrl(this.baseUrl);
    } else {
      this.showSearch();
    }
  }

  consumePathSegment(segment: string): boolean {
    const child = this.getChild(segment);
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

export enum CrudView {
  SEARCH, DETAIL, CREATE, BULK_EDIT
}

import {Component, Input, OnInit, ViewChild} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {PortofinoService} from "../portofino.service";
import {ClassAccessor} from "../class-accessor";
import {PortofinoComponent} from "../portofino-app.component";
import {Router} from "@angular/router";
import {SearchComponent} from "./search/search.component";
import {Button, Operation, Page, PageChild, PageConfiguration} from "../page.component";
import {Configuration, SelectionProvider} from "./crud.common";

export abstract class CrudPage extends Page {
  id: string;
  sourceUrl: string;
  abstract computeSource();
}

@Component({
  selector: 'portofino-crud',
  templateUrl: './crud.component.html',
  styleUrls: ['./crud.component.css']
})
@PortofinoComponent({ name: 'crud' })
export class CrudComponent extends CrudPage implements OnInit {

  @Input()
  configuration: PageConfiguration & any;

  classAccessor: ClassAccessor;
  selectionProviders: SelectionProvider[];
  classAccessorPath = '/:classAccessor';
  selectionProvidersPath = '/:selectionProviders';

  @Input()
  pageSize: number;
  @ViewChild(SearchComponent)
  search: SearchComponent;

  view: CrudView;

  createEnabled: boolean;
  bulkEditEnabled: boolean;
  bulkDeleteEnabled: boolean;

  selection: string[];

  constructor(private http: HttpClient, public portofino: PortofinoService, private router: Router) {
    super();
  }

  ngOnInit() {
    this.sourceUrl = this.computeSource();
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

  computeSource() {
    let source = "";
    if(!this.configuration.source || !this.configuration.source.startsWith('/')) {
      let parent = this.parent;
      while(parent) {
        if(parent instanceof CrudPage) {
          source = parent.computeSource();
          if(parent.id) {
            source += `/${parent.id}`;
          }
          source += '/';
          break;
        } else {
          parent = parent.parent;
        }
      }
    }
    if(!source) {
      source = this.portofino.apiPath;
    }
    return (source + (this.configuration.source ? this.configuration.source : ''))
      //replace double slash, but not in http://
      .replace(new RegExp("([^:])//"), '$1/');
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

  @Button({
    color: 'accent', enabledIf: (self) => self.createEnabled, icon: 'add', text: 'Create new'
  })
  showCreate() {
    this.view = CrudView.CREATE;
  }

  showDetail() {
    this.view = CrudView.DETAIL;
  }

  showSearch() {
    this.view = CrudView.SEARCH;
  }

  @Button({
    color: 'primary', icon: 'edit', text: 'Edit',
    presentIf: (self) => self.bulkEditEnabled,
    enabledIf: (self) => self.search && self.search.getSelectedIds().length > 0
  })
  showBulkEdit() {
    this.selection = this.search.getSelectedIds();
    this.view = CrudView.BULK_EDIT;
  }

  @Button({
    color: 'warn', icon: 'delete', text: 'Delete',
    presentIf: (self) => self.bulkDeleteEnabled,
    enabledIf: (self) => self.search && self.search.getSelectedIds().length > 0
  })
  bulkDelete() {
    let params = new HttpParams();
    this.search.getSelectedIds().forEach(id => params = params.append("id", id));
    this.http.delete(this.sourceUrl, { params: params }).subscribe(() => this.search.refreshSearch());
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

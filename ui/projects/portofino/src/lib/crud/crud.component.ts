import {Component, EventEmitter, Input, Output, Type} from '@angular/core';
import {HttpParams} from '@angular/common/http';
import {ClassAccessor} from "../class-accessor";
import {PortofinoComponent} from "../portofino-app.component";
import {Operation, Page, PageChild, PageConfiguration, PageSettingsPanel} from "../page";
import {Configuration, SelectionProvider} from "./crud.common";
import {Button} from "../buttons";
import {SelectionModel} from "@angular/cdk/collections";
import {SearchComponent} from "./search/search.component";
import {DetailComponent} from "./detail/detail.component";
import {CreateComponent} from "./detail/create.component";
import {BulkEditComponent} from "./bulk/bulk-edit.component";

@Component({
  selector: 'portofino-crud',
  templateUrl: './crud.component.html',
  styleUrls: ['./crud.component.css']
})
@PortofinoComponent({ name: 'crud' })
export class CrudComponent extends Page {

  @Input()
  configuration: PageConfiguration & Configuration & any;
  sourceUrl: string;

  classAccessor: ClassAccessor;
  selectionProviders: SelectionProvider[];
  classAccessorPath = '/:classAccessor';
  selectionProvidersPath = '/:selectionProviders';

  @Input()
  pageSize: number;
  @Output()
  readonly refreshSearch = new EventEmitter();
  readonly selection = new SelectionModel<any>(true, []);

  id: string;
  view: CrudView;

  createEnabled: boolean;
  bulkEditEnabled: boolean;
  bulkDeleteEnabled: boolean;

  //Sub-components, configurable
  @Input()
  searchComponent: Type<any> = SearchComponent;
  @Input()
  searchComponentContext = {};
  @Input()
  detailComponent: Type<any> = DetailComponent;
  @Input()
  detailComponentContext = {};
  @Input()
  createComponent: Type<any> = CreateComponent;
  @Input()
  createComponentContext = {};
  @Input()
  bulkEditComponent: Type<any> = BulkEditComponent;
  @Input()
  bulkEditComponentContext = {};

  get rowsPerPage() {
    return this.configuration.rowsPerPage ? this.configuration.rowsPerPage : 10;
  }

  initialize() {
    this.sourceUrl = this.computeBaseSourceUrl();
    this.loadConfiguration().subscribe(
      () => this.http.get<ClassAccessor>(this.sourceUrl + this.classAccessorPath).subscribe(
        classAccessor => this.http.get<SelectionProvider[]>(this.sourceUrl + this.selectionProvidersPath).subscribe(
          sps => this.http.get<Operation[]>(this.sourceUrl + this.operationsPath).subscribe(ops => {
            this.init(classAccessor, sps, ops);
          }))));
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

  protected init(classAccessor, selectionProviders: SelectionProvider[], ops: Operation[]) {
    this.createEnabled = this.operationAvailable(ops, "POST");
    this.bulkEditEnabled = this.operationAvailable(ops, "PUT");
    this.bulkDeleteEnabled = this.operationAvailable(ops, "DELETE");
    this.classAccessor = classAccessor;
    this.selectionProviders = selectionProviders;
    this.classAccessor.properties.forEach(p => {
      p.key = (this.classAccessor.keyProperties.find(k => k == p.name) != null);
    });
    this.subscribe(this.route.queryParams,params => {
      if(params.hasOwnProperty('create') && this.createEnabled && !this.embedded) {
        this.showCreate();
      } else if(this.id) {
        this.showDetail();
      } else {
        this.showSearch();
      }
    });
    this.subscribe(this.portofino.localeChange, _ => {
      if(this.view == CrudView.SEARCH) {
        this.refreshSearch.emit();
      } //else TODO
    });
  }

  static createEnabled(self: CrudComponent) {
    return self.createEnabled;
  }

  @Button({
    list: 'search-results', color: 'accent', presentIf: CrudComponent.createEnabled, icon: 'add', text: 'Create new'
  })
  navigateToCreate() {
    if(this.embedded) {
      this.showCreate();
    } else {
      this.router.navigate(['.'], {
        relativeTo: this.route,
        queryParams: { create: "x" }});
    }
  }

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
    return self.getSelectedIds().length > 0;
  }

  getSelectedIds(): string[] {
    return this.selection.selected.map(row => row.__rowKey);
  }

  @Button({
    list: 'search-results',
    color: 'primary', icon: 'edit', text: 'Edit',
    presentIf: CrudComponent.bulkEditPresent,
    enabledIf: CrudComponent.bulkButtonsEnabled
  })
  showBulkEdit() {
    this.allowEmbeddedComponents = false;
    this.view = CrudView.BULK_EDIT;
  }

  static bulkDeletePresent(self: CrudComponent) {
    return self.bulkDeleteEnabled;
  }

  @Button({
    list: 'search-results',
    color: 'warn', icon: 'delete', text: 'Delete',
    presentIf: CrudComponent.bulkDeletePresent,
    enabledIf: CrudComponent.bulkButtonsEnabled
  })
  bulkDelete() {
    let params = new HttpParams();
    this.getSelectedIds().forEach(id => params = params.append("id", id));
    this.http.delete(this.sourceUrl, { params: params }).subscribe(() => this.refreshSearch.emit());
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
    if(!this.embedded && (this.view == CrudView.DETAIL || this.view == CrudView.CREATE)) {
      this.reloadBaseUrl();
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
    return false;
  }

  get children(): PageChild[] {
    if(this.id) {
      return this.configuration.detailChildren || []
    } else {
      return this.configuration.children
    }
  }

  //Configuration

  get configurationUrl() {
    return this.computeBaseSourceUrl() + this.configurationPath;
  }

  get configurationProperties() {
    return ["name", "database", "query", "searchTitle", "createTitle", "readTitle", "editTitle", "variable",
            "largeResultSet", "rowsPerPage"]
  }

  protected getPageSettingsPanel(): PageSettingsPanel {
    return new CrudPageSettingsPanel(this);
  }

  protected getActionConfigurationToSave(): any {
    const configurationToSave = super.getActionConfigurationToSave();
    const settingsPanel = (<CrudPageSettingsPanel>this.settingsPanel);
    configurationToSave.properties = settingsPanel.properties;
    configurationToSave.selectionProviders = settingsPanel.selectionProviders.map(sp => {
      const enabled = sp.selectionProviderName != null;
      return {
        enabled: enabled,
        selectionProviderName: enabled ? sp.selectionProviderName : sp.availableSelectionProviders ? sp.availableSelectionProviders[0] : null,
        displayModeName: sp.displayModeName,
        searchDisplayModeName: sp.searchDisplayModeName
      }
    });
    return configurationToSave;
  }
}

export enum CrudView {
  SEARCH, DETAIL, CREATE, BULK_EDIT
}

@Component({
  selector: 'portofino-crud-search-holder',
  template: `<ng-container *ngxComponentOutlet="component; context: context"></ng-container>`,
})
export class SearchComponentHolder extends SearchComponent {
  @Input()
  component: Type<any>;
  @Input()
  context = {};
  ngOnInit(): void {}
  ngOnDestroy(): void {}
}

@Component({
  selector: 'portofino-crud-detail-holder',
  template: `<ng-container *ngxComponentOutlet="component; context: context"></ng-container>`,
})
export class DetailComponentHolder extends DetailComponent {
  @Input()
  component: Type<any>;
  @Input()
  context = {};
  ngOnInit(): void {}
}

@Component({
  selector: 'portofino-crud-create-holder',
  template: `<ng-container *ngxComponentOutlet="component; context: context"></ng-container>`,
})
export class CreateComponentHolder extends CreateComponent {
  @Input()
  component: Type<any>;
  @Input()
  context = {};
  ngOnInit(): void {}
}

@Component({
  selector: 'portofino-crud-bulk-edit-holder',
  template: `<ng-container *ngxComponentOutlet="component; context: context"></ng-container>`,
})
export class BulkEditComponentHolder extends BulkEditComponent {
  @Input()
  component: Type<any>;
  @Input()
  context = {};
  ngOnInit(): void {}
}

export class CrudPageSettingsPanel extends PageSettingsPanel {

  properties = [];
  selectionProviders = [];

  protected setupConfigurationForm(ca: ClassAccessor, config: any) {
    super.setupConfigurationForm(ca, config);
    this.properties = [];
    this.selectionProviders = [];
    if(!config) {
      return;
    }
    const crud = this.page as CrudComponent;
    crud.http.get<any>(crud.sourceUrl + '/:allSelectionProviders').subscribe(sps => {
      this.selectionProviders = sps;
    });
    config.properties.forEach(p => {
      this.properties.push({
        enabled: p.property.enabled, name: p.property.name, label: p.property.label,
        insertable: p.property.insertable, updatable: p.property.updatable,
        inSummary: p.property.inSummary, searchable: p.property.searchable
      });
    })
  }
}

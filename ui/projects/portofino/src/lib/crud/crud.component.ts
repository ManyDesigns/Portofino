import {Component, EventEmitter, Input, Output, Type} from '@angular/core';
import {HttpParams} from '@angular/common/http';
import {ClassAccessor, loadClassAccessor, SelectionProvider as Selection} from "../class-accessor";
import {PortofinoComponent} from "../page.factory";
import {Operation, Page, PageConfiguration, PageSettingsPanel} from "../page";
import {Configuration, SelectionProvider} from "./crud.common";
import {Button} from "../buttons";
import {SelectionModel} from "@angular/cdk/collections";
import {SearchComponent} from "./search/search.component";
import {DetailComponent} from "./detail/detail.component";
import {CreateComponent} from "./detail/create.component";
import {BulkEditComponent} from "./bulk/bulk-edit.component";
import {mergeMap} from "rxjs/operators";
import {Field, FieldSet} from "../form";
import {BehaviorSubject} from "rxjs";

@Component({
  selector: 'portofino-crud',
  templateUrl: './crud.component.html',
  styleUrls: ['./crud.component.scss']
})
@PortofinoComponent({ name: 'crud', defaultActionClass: "com.manydesigns.portofino.resourceactions.crud.CrudAction" })
export class CrudComponent extends Page {

  @Input()
  configuration: PageConfiguration & Configuration & any;
  sourceUrl: string;

  classAccessor: ClassAccessor;
  selectionProviders: SelectionProvider[];
  classAccessorPath = '/:classAccessor';
  selectionProvidersPath = '/:selectionProvider';

  @Input()
  pageSize: number;
  @Output()
  readonly refreshSearch = new EventEmitter();
  readonly selection = new SelectionModel<any>(true, []);
  @Output()
  readonly editMode = new BehaviorSubject<boolean>(false);

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

  error: any;

  get rowsPerPage() {
    return this.configuration.rowsPerPage ? this.configuration.rowsPerPage : 10;
  }

  initialize() {
    this.sourceUrl = this.computeBaseSourceUrl();
    this.loadConfiguration().pipe(
      mergeMap(() => this.http.get<ClassAccessor>(this.sourceUrl + this.classAccessorPath).pipe(loadClassAccessor)),
      mergeMap(classAccessor => {
        this.classAccessor = classAccessor;
        return this.http.get<SelectionProvider[]>(this.sourceUrl + this.selectionProvidersPath);
      }),
      mergeMap(sps => {
        this.selectionProviders = sps;
        return this.http.get<Operation[]>(this.sourceUrl + this.operationsPath);
      })).subscribe(
        ops => {
        const bulkOpsEnabled = ops.some(op => op.name == "Bulk operations" && op.available);
        this.createEnabled = this.operationAvailable(ops, "POST");
        this.bulkEditEnabled = this.operationAvailable(ops, "PUT") && bulkOpsEnabled;
        this.bulkDeleteEnabled = this.operationAvailable(ops, "DELETE") && bulkOpsEnabled;
        this.init();
      },
      () => this.error = this.translate.instant("This page is not configured correctly."));
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

  protected init() {
    this.classAccessor.properties.forEach(p => {
      p.key = (this.classAccessor.keyProperties.find(k => k == p.name) != null);
    });
    if(this.route) {
      this.subscribe(this.route.queryParams,params => {
        if(params.hasOwnProperty('create') && this.createEnabled && !this.embedded) {
          this.showCreate();
        } else if(this.id) {
          this.showDetail();
          if(params.hasOwnProperty('edit') && !this.embedded) {
            this.editMode.next(true);
          }
        } else {
          this.showSearch();
        }
      });
    }
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
    this.returnUrl = null;
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
      this.refreshSearch.emit();
    }
  }

  goToParent() {
    if(this.view == CrudView.DETAIL || this.view == CrudView.CREATE) {
      this.goToSearch();
    } else {
      super.goToParent();
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

  get childrenProperty(): string {
    if(this.id) {
      return "detailChildren";
    } else {
      return "children";
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
    const url = `${this.page.portofino.apiRoot}portofino-upstairs/database/connections`;
    this.page.http.get<any[]>(url).subscribe(s => {
      const confFieldset = this.formDefinition.contents.find(f => f['name'] == 'configuration') as FieldSet;
      const dbField = confFieldset.contents.contents.find(f => f instanceof Field && f.name == 'database') as Field;
      dbField.property.selectionProvider = new Selection();
      s.forEach(c => {
        const selected = this.page.configuration.database == c.name;
        const value = { l: c.name, v: c.name, s: selected };
        dbField.property.selectionProvider.options.push(value);
        if(selected) {
          config.database = value;
          dbField.initialState = value;
        }
      });
      this.formDefinition = {...this.formDefinition}; //To cause the form component to reload the form
    });
    const crud = this.page as CrudComponent;
    crud.http.get<any>(crud.sourceUrl + '/:allSelectionProviders').subscribe(sps => {
      this.selectionProviders = sps;
    });
    config.properties.forEach(p => {
      this.properties.push(Object.assign({}, p.property));
    });
    if(crud.classAccessor) {
      crud.classAccessor.properties.forEach(p => {
        if(!this.properties.find(p2 => p2.name == p.name)) {
          this.properties.push({
            enabled: p.key, name: p.name, label: null,
            insertable: false, updatable: false,
            inSummary: p.key, searchable: false, annotations: []
          });
        }
      });
    }
  }

  getActionConfigurationToSave(): any {
    const configurationToSave = super.getActionConfigurationToSave();
    configurationToSave.properties = this.properties;
    configurationToSave.selectionProviders = this.selectionProviders.map(sp => {
      const enabled = sp.selectionProviderName != null;
      return {
        enabled: enabled,
        selectionProviderName: enabled ? sp.selectionProviderName : sp.availableSelectionProviders ? sp.availableSelectionProviders[0] : null,
        displayModeName: sp.displayModeName,
        searchDisplayModeName: sp.searchDisplayModeName
      }
    });
    configurationToSave.database = configurationToSave.database ? configurationToSave.database.v : null;
    return configurationToSave;
  }

  getPageConfigurationToSave(formValue): PageConfiguration {
    const superConf: any = super.getPageConfigurationToSave(formValue);
    const config = Object.assign({}, this.page.configuration, formValue);
    superConf.detailChildren = config.detailChildren;
    return superConf;
  }
}

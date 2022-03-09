import {Component, EventEmitter, Input, Output, Type} from '@angular/core';
import {HttpParams} from '@angular/common/http';
import {ClassAccessor, loadClassAccessor, SelectionProvider as Selection} from "../../class-accessor";
import {Operation, Page, PageConfiguration, PageSettingsPanel, PortofinoComponent} from "../../page";
import {Configuration, SelectionProvider} from "./crud.common";
import {Button} from "../../buttons";
import {SelectionModel} from "@angular/cdk/collections";
import {SearchComponent} from "./search/search.component";
import {DetailComponent} from "./detail/detail.component";
import {CreateComponent} from "./detail/create.component";
import {BulkEditComponent} from "./bulk/bulk-edit.component";
import {mergeMap} from "rxjs/operators";
import {Field, FieldSet} from "../../form";
import {BehaviorSubject} from "rxjs";

@Component({
  selector: 'portofino-crud',
  templateUrl: '../../../../assets/pages/crud/crud.component.html'
})
@PortofinoComponent({ name: 'crud', defaultActionClass: "com.manydesigns.portofino.resourceactions.crud.CrudAction" })
export class CrudComponent extends Page {

  @Input()
  configuration: PageConfiguration & Configuration & any;
  sourceUrl: string;

  classAccessor: ClassAccessor;
  selectionProviders: SelectionProvider[];
  operations: Operation[];
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
    return this.configuration?.rowsPerPage ? this.configuration.rowsPerPage : 10;
  }

  initialize() {
    if(this.configuration.detailChildren) {
      //Legacy children with no embedded section
      this.configuration.detailChildren.forEach(c => {
        if(c.embedded) {
          if(!c.embeddedIn) {
            c.embeddedIn = "default";
          }
          delete c.embedded;
        }
      });
    }

    this.sourceUrl = this.computeBaseSourceUrl();
    this.loadConfiguration().pipe(
      mergeMap(() => this.http.get<ClassAccessor>(this.sourceUrl + this.classAccessorPath)),
      mergeMap(classAccessor => {
        this.initClassAccessor(classAccessor);
        return this.http.get<SelectionProvider[]>(this.sourceUrl + this.selectionProvidersPath);
      }),
      mergeMap(sps => {
        this.selectionProviders = sps;
        return this.http.get<Operation[]>(this.sourceUrl + this.operationsPath);
      })).subscribe(
        ops => {
          this.initOperations(ops);
          this.start();
          super.initialize();
        },
      e => {
          this.error = this.translate.instant("This page is not configured correctly.");
          console?.log(e);
      });
  }

  protected initOperations(operations: Operation[]) {
    this.operations = operations;
    const bulkOpsEnabled = this.operations.some(op => op.name == "Bulk operations" && op.available);
    this.createEnabled = this.operationAvailable(this.operations, "POST");
    this.bulkEditEnabled = this.operationAvailable(this.operations, "PUT") && bulkOpsEnabled;
    this.bulkDeleteEnabled = this.operationAvailable(this.operations, "DELETE") && bulkOpsEnabled;
  }

  protected initClassAccessor(classAccessor: ClassAccessor) {
    this.classAccessor = loadClassAccessor(classAccessor);
    this.classAccessor.properties.forEach(p => {
      p.key = (this.classAccessor.keyProperties.find(k => k == p.name) != null);
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

  protected start() {
    if(this.route) {
      this.subscribe(this.route.queryParams,params => {
        if(params.hasOwnProperty('create') && this.createEnabled && !this.embedded) {
          this.showCreate();
        } else if(this.id) {
          this.showDetail();
          if(params.hasOwnProperty('edit') && !this.embedded) {
            this.enableDetailEditMode();
          }
        } else {
          this.showSearch();
        }
      });
    } else {
      this.showSearch();
    }
    this.subscribe(this.portofino.localeChange, _ => {
      if(this.view == CrudView.SEARCH) {
        this.refreshSearch.emit();
      } //else TODO
    });
  }

  protected enableDetailEditMode() {
    this.editMode.next(true);
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
        queryParams: { create: "" }});
    }
  }

  showCreate() {
    this.allowEmbeddedComponents = false;
    this.view = CrudView.CREATE;
  }

  openDetail(id) {
    this.id = id;
    this.showDetail();
  }

  showDetail() {
    this.allowEmbeddedComponents = true;
    this.view = CrudView.DETAIL;
  }

  showSearch() {
    this.allowEmbeddedComponents = true;
    this.id = null;
    this.returnUrl = null;
    this.view = CrudView.SEARCH;
  }

  reset() {
    this.allowEmbeddedComponents = true;
    this.id = null;
    this.returnUrl = null;
    this.view = null;
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
    if(this.getSelectedIds().length == 1) {
      this.openDetail(this.getSelectedIds()[0]);
      this.enableDetailEditMode();
    } else {
      this.allowEmbeddedComponents = false;
      this.view = CrudView.BULK_EDIT;
    }
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

  isOpenDetailInSamePage() {
    return this.embedded && this.configuration.openDetailInSamePageWhenEmbedded
  }

  ngOnDestroy() {
    super.ngOnDestroy();
    this.editMode.complete();
    this.refreshSearch.complete();
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
  ngOnInit(): void {
    //Skip
  }
  ngOnDestroy(): void {
    //Skip
  }
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
  ngOnInit(): void {
    //Skip
  }
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
  ngOnInit(): void {
    //Skip
  }
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
  ngOnInit(): void {
    //Skip
  }
}

export class CrudPageSettingsPanel extends PageSettingsPanel {

  properties = [];
  selectionProviders = [];

  isValid(): boolean {
    return super.isValid();
  }

  protected setupPageConfigurationForm(pageConfiguration) {
    super.setupPageConfigurationForm(pageConfiguration);
    this.formDefinition.contents.push(Field.fromProperty({
      name: 'openDetailInSamePageWhenEmbedded',
      label: 'Open detail in the same page when embedded',
      type: 'boolean'},
      pageConfiguration));
  }

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
      this.properties.push(Object.assign({}, p.property ? p.property : p['virtual-property']));
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

  getPageConfigurationToSave(formValue = this.form.value): PageConfiguration {
    const pageConf: any = super.getPageConfigurationToSave(formValue);
    const config = Object.assign({}, this.page.configuration, formValue);
    pageConf.detailChildren = config.detailChildren;
    pageConf.openDetailInSamePageWhenEmbedded = config.openDetailInSamePageWhenEmbedded;
    return pageConf;
  }
}

import {Component, EventEmitter, Input, Output, Type} from '@angular/core';
import {HttpParams} from '@angular/common/http';
import {ClassAccessor, loadClassAccessor, SelectionProvider as Selection} from "../../class-accessor";
import {PortofinoComponent} from "../../page.factory";
import {Operation, Page, PageConfiguration, PageSettingsPanel} from "../../page";
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

export const DEFAULT_CRUD_TEMPLATE = `
<portofino-page-layout [page]="this">
  <ng-template #content>
    <mat-error *ngIf="error">Error: {{error|json}}</mat-error>
    <portofino-crud-search-holder *ngIf="isSearchView()"
      [component]="searchComponent" [context]="searchComponentContext"
      [classAccessor]="classAccessor" [pageSize]="rowsPerPage" [sourceUrl]="sourceUrl" [baseUrl]="url"
      [selectionProviders]="selectionProviders" [selectionEnabled]="bulkDeleteEnabled || bulkEditEnabled"
      [selection]="selection" [refresh]="refreshSearch" (openDetail)="openDetail($event)"
      [openDetailInSamePage]="isOpenDetailInSamePage()"
      [parentButtons]="getButtons('search-results')" [parent]="page">
    </portofino-crud-search-holder>
    <portofino-crud-detail-holder *ngIf="isDetailView()"
      [component]="detailComponent" [context]="detailComponentContext"
      [id]="id" [classAccessor]="classAccessor" [configuration]="configuration" [selectionProviders]="selectionProviders"
      [sourceUrl]="sourceUrl" (close)="goBack()" (editModeChanges)="onEditModeChange($event)"
      [parentButtons]="getButtons('detail')" [editOrView]="editMode" [parent]="page">
    </portofino-crud-detail-holder>
    <portofino-crud-create-holder *ngIf="isCreateView()"
      [component]="createComponent" [context]="createComponentContext"
      [classAccessor]="classAccessor" [configuration]="configuration" [selectionProviders]="selectionProviders"
      [sourceUrl]="sourceUrl" (close)="goBack()"
      [parentButtons]="getButtons('create')" [parent]="page">
    </portofino-crud-create-holder>
    <portofino-crud-bulk-edit-holder *ngIf="isBulkEditView()"
      [component]="bulkEditComponent" [context]="bulkEditComponentContext"
      [classAccessor]="classAccessor" [configuration]="configuration" [selectionProviders]="selectionProviders"
      [ids]="getSelectedIds()" [sourceUrl]="sourceUrl" (close)="goToSearch()"
      [parentButtons]="getButtons('bulk-edit')" [parent]="page">
    </portofino-crud-bulk-edit-holder>
  </ng-template>
  <ng-template #extraConfiguration>
    <fieldset>
      <legend>{{'Properties' | translate}}</legend>
      <table class="mat-table">
        <tr class="mat-header-row">
          <th class="mat-header-cell">
            <div class="mat-sort-header-container">
              <button class="mat-sort-header-button">{{'Enabled' | translate}}</button>
            </div>
          </th>
          <th class="mat-header-cell">
            <div class="mat-sort-header-container">
              <button class="mat-sort-header-button">{{'Name' | translate}}</button>
            </div>
          </th>
          <th class="mat-header-cell">
            <div class="mat-sort-header-container">
              <button class="mat-sort-header-button">{{'Label' | translate}}</button>
            </div>
          </th>
          <th class="mat-header-cell">
            <div class="mat-sort-header-container">
              <button class="mat-sort-header-button">{{'Insertable' | translate}}</button>
            </div>
          </th>
          <th class="mat-header-cell">
            <div class="mat-sort-header-container">
              <button class="mat-sort-header-button">{{'Updatable' | translate}}</button>
            </div>
          </th>
          <th class="mat-header-cell">
            <div class="mat-sort-header-container">
              <button class="mat-sort-header-button">{{'In Summary' | translate}}</button>
            </div>
          </th>
          <th class="mat-header-cell">
            <div class="mat-sort-header-container">
              <button class="mat-sort-header-button">{{'Searchable' | translate}}</button>
            </div>
          </th>
        </tr>
        <tr *ngFor="let p of settingsPanel['properties']" class="mat-row">
          <td class="mat-cell"><mat-checkbox [(ngModel)]="p.enabled"></mat-checkbox></td>
          <td class="mat-cell">{{p.name}}</td>
          <td class="mat-cell"><mat-form-field><input matInput [(ngModel)]="p.label"></mat-form-field></td>
          <td class="mat-cell"><mat-checkbox [(ngModel)]="p.insertable"></mat-checkbox></td>
          <td class="mat-cell"><mat-checkbox [(ngModel)]="p.updatable"></mat-checkbox></td>
          <td class="mat-cell"><mat-checkbox [(ngModel)]="p.inSummary"></mat-checkbox></td>
          <td class="mat-cell"><mat-checkbox [(ngModel)]="p.searchable"></mat-checkbox></td>
        </tr>
      </table>
    </fieldset>
    <fieldset>
      <legend>{{'Selection providers' | translate}}</legend>
      <table class="mat-table">
        <tr class="mat-header-row">
          <th class="mat-header-cell">
            <div class="mat-sort-header-container">
              <button class="mat-sort-header-button">{{'Properties' | translate}}</button>
            </div>
          </th>
          <th class="mat-header-cell">
            <div class="mat-sort-header-container">
              <button class="mat-sort-header-button">{{'Selection Provider' | translate}}</button>
            </div>
          </th>
          <th class="mat-header-cell">
            <div class="mat-sort-header-container">
              <button class="mat-sort-header-button">{{'Display Mode' | translate}}</button>
            </div>
          </th>
          <th class="mat-header-cell">
            <div class="mat-sort-header-container">
              <button class="mat-sort-header-button">{{'Search Display Mode' | translate}}</button>
            </div>
          </th>
        </tr>
        <tr *ngFor="let sp of settingsPanel['selectionProviders']" class="mat-row">
          <td class="mat-cell">
            {{sp.fieldNames.join(", ")}}
          </td>
          <td class="mat-cell">
            <mat-select [(ngModel)]="sp.selectionProviderName">
              <mat-option [value]="null">{{"None" | translate}}</mat-option>
              <mat-option *ngFor="let option of sp.availableSelectionProviders" [value]="option">{{option}}</mat-option>
            </mat-select>
          </td>
          <td class="mat-cell">
            <mat-select [(ngModel)]="sp.displayModeName">
              <mat-option value="DROPDOWN">{{"Drop down" | translate}}</mat-option>
              <mat-option value="RADIO">{{"Radio buttons" | translate}}</mat-option>
              <mat-option value="AUTOCOMPLETE">{{"Autocomplete" | translate}}</mat-option>
            </mat-select>
          </td>
          <td class="mat-cell">
            <mat-select [(ngModel)]="sp.searchDisplayModeName">
              <mat-option value="DROPDOWN">{{"Drop down" | translate}}</mat-option>
              <mat-option value="RADIO">{{"Radio buttons" | translate}}</mat-option>
              <mat-option value="AUTOCOMPLETE">{{"Autocomplete" | translate}}</mat-option>
              <mat-option value="MULTIPLESELECT">{{"Multiple select" | translate}}</mat-option>
              <mat-option value="CHECKBOX">{{"Check boxes" | translate}}</mat-option>
            </mat-select>
          </td>
        </tr>
      </table>
    </fieldset>
  </ng-template>
</portofino-page-layout>`

@Component({
  selector: 'portofino-crud',
  template: DEFAULT_CRUD_TEMPLATE
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
            this.enableDetailEditMode();
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
    super.initialize();
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

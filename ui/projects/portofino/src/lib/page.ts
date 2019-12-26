import {
  AfterViewInit, ChangeDetectorRef,
  Component,
  ContentChild,
  EventEmitter, Injectable,
  InjectionToken,
  Input, OnDestroy, Optional, TemplateRef, Type, ViewChild
} from "@angular/core";
import {ClassAccessor, loadClassAccessor, Property} from "./class-accessor";
import {FormGroup} from "@angular/forms";
import {PortofinoService} from "./portofino.service";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Field, FieldSet, Form} from "./form";
import {ActivatedRoute, Router} from "@angular/router";
import {AuthenticationService, NO_AUTH_HEADER} from "./security/authentication.service";
import {declareButton, getButtons, WithButtons} from "./buttons";
import {Observable, of, PartialObserver, Subscription} from "rxjs";
import {catchError, map, mergeMap} from "rxjs/operators";
import {NotificationService} from "./notifications/notification.service";
import {TranslateService} from "@ngx-translate/core";

export const NAVIGATION_COMPONENT = new InjectionToken('Navigation Component');

@Injectable()
export class PageService {
  page: Page;
  error;
  readonly pageLoad = new EventEmitter<Page | string>();
  readonly pageLoaded = new EventEmitter<Page>();
  readonly pageLoadError = new EventEmitter<any>();

  reset() {
    this.error = null;
    this.page = null;
  }

  loadPage(page: Page | string) {
    this.pageLoad.emit(page);
  }

  notifyPageLoaded(page: Page) {
    this.page = page;
    this.pageLoaded.emit(page);
  }

  notifyError(error) {
    this.error = error;
    this.pageLoadError.emit(error);
  }
}

@Component({
  selector: 'portofino-default-navigation',
  templateUrl: './navigation.component.html',
  styleUrls: ['./navigation.component.scss']
})
export class DefaultNavigationComponent {
  constructor(public pageService: PageService) {}
}

export class PageConfiguration {
  type?: string;
  actualType?: Type<any>;
  title: string;
  source: string;
  securityCheckPath: string = ':description';
  children: PageChild[] = [];
  icon?: string;
  template?: string;
}

export class PageChild {
  path: string;
  title: string;
  icon: string;
  embedded: boolean;
  showInNavigation: boolean;
  accessible: boolean;
}

export class PageSettingsPanel {
  active: boolean;
  readonly form = new FormGroup({});
  formDefinition = new Form();
  previousConfiguration;
  permissions: Permissions;
  error;
  readonly accessLevels = ["NONE", "VIEW", "EDIT", "DEVELOP", "DENY"];
  callback: (saved: boolean) => void;

  constructor(public page: Page) {}

  show(callback: (saved: boolean) => void = () => {}) {
    this.callback = callback;
    const pageConfiguration = this.page.configuration;
    this.previousConfiguration = Object.assign({}, pageConfiguration);
    this.setupPageConfigurationForm(pageConfiguration);
    this.formDefinition.contents.forEach((f: Field) => {
      f.editable = this.page.portofino.localApiAvailable;
    });
    this.reloadConfiguration();
    this.loadPermissions();
    this.active = true;
  }

  protected setupPageConfigurationForm(pageConfiguration) {
    const titleField = Field.fromProperty(Property.create({
      name: 'title',
      label: 'Title'
    }).required(), pageConfiguration);
    const iconField = Field.fromProperty({name: 'icon', label: 'Icon'}, pageConfiguration);
    if (pageConfiguration.template) {
      const description = this.page.portofino.templates[pageConfiguration.template].description;
      pageConfiguration.template = {v: pageConfiguration.template, l: description, s: true};
    }
    const templates = [];
    for (let key in this.page.portofino.templates) {
      const template = this.page.portofino.templates[key];
      templates.push({
        v: key,
        l: this.page.translate.instant(template.description ? template.description : key),
        s: false
      });
    }
    const templateField = Field.fromProperty(Property.create({
      name: "template",
      label: "Template"
    }).withSelectionProvider({
      options: templates
    }), pageConfiguration);
    this.formDefinition.contents = [titleField, iconField, templateField];
  }

  loadPermissions() {
    const permissionsUrl = this.page.computeSourceUrl() + this.page.permissionsPath;
    this.page.http.get<Permissions>(permissionsUrl).subscribe(p => {
      this.permissions = p;
      this.permissions.groups.forEach(g => {
        if (!g.level) {
          g.level = "inherited";
        }
        g.permissionMap = {};
        g.permissions.forEach(p => {
          g.permissionMap[p] = true;
        });
      });
    });
  }

  isValid() {
    return this.form.valid;
  }

  hide(saved: boolean) {
    this.active = false;
    this.callback(saved);
  }

  get groups() {
    return this.permissions.groups.sort((g1, g2) => g1.name.localeCompare(g2.name));
  }

  protected setupConfigurationForm(ca: ClassAccessor, config: any) {
    const index = this.formDefinition.contents.findIndex(f => f['name'] == 'configuration');
    if(index >= 0) {
      this.formDefinition.contents.splice(index, 1);
    }
    if(ca) {
      const fieldSet = FieldSet.fromClassAccessor(ca, {
        name: 'configuration', label: 'Configuration', object: config, properties: this.page.configurationProperties
      });
      this.formDefinition.contents.push(fieldSet);
    }
    this.formDefinition = {...this.formDefinition}; //To cause the form component to reload the form
  }

  reloadConfiguration() {
    this.page.loadConfiguration().subscribe(conf => {
      this.page.http.get<ClassAccessor>(this.page.configurationUrl + '/classAccessor')
        .pipe(loadClassAccessor)
        .subscribe(ca => {
        this.setupConfigurationForm(ca, conf);
      }, error => {
        this.setupConfigurationForm(null, null);
        this.error = error;
      })
      //this.page.settingsPanel.refreshConfiguration(); TODO
    });
  }

  getActionConfigurationToSave() {
    const configuration = this.form.get('configuration');
    return Object.assign({}, configuration ? configuration.value : {});
  }

  getPageConfigurationToSave(formValue = this.form.value) {
    const config = Object.assign({}, this.page.configuration, formValue);
    const pageConfiguration = new PageConfiguration();
    //Reflection would be nice
    pageConfiguration.children = config.children;
    pageConfiguration.icon = config.icon;
    pageConfiguration.securityCheckPath = config.securityCheckPath;
    pageConfiguration.source = config.source;
    pageConfiguration.template = config.template ? config.template.v : null;
    pageConfiguration.title = config.title;
    pageConfiguration.type = config.type;
    return pageConfiguration;
  }
}

export abstract class Page implements WithButtons, OnDestroy {

  @Input()
  configuration: PageConfiguration & any;
  readonly settingsPanel = this.getPageSettingsPanel();
  path: string;
  baseUrl: string;
  url: string;
  segment: string;
  parent: Page;
  allowEmbeddedComponents: boolean = true;
  embedded = false;
  returnUrl;

  readonly operationsPath = '/:operations';
  readonly configurationPath = '/:configuration';
  readonly permissionsPath = '/:permissions';
  readonly page = this;

  protected readonly subscriptions: Subscription[] = [];

  constructor(
    public portofino: PortofinoService, public http: HttpClient, protected router: Router,
    @Optional() protected route: ActivatedRoute, public authenticationService: AuthenticationService,
    protected notificationService: NotificationService, public translate: TranslateService) {
    //Declarative approach does not work for some reason:
    //"Metadata collected contains an error that will be reported at runtime: Lambda not supported."
    //TODO investigate with newer versions
    this.setupBasicPageButtons();
    if(!this.embedded && this.route) {
      this.subscribe(this.route.params, p => {
          this.returnUrl = p['returnUrl'];
      });
    }
  }

  private setupBasicPageButtons() {
    declareButton({
      color: 'primary', icon: 'save', text: 'Save', list: 'configuration',
      enabledIf: () => this.settingsPanel.isValid()
    }, this, 'saveConfiguration', null);
    declareButton({
      icon: 'arrow_back', text: 'Cancel', list: 'configuration'
    }, this, 'cancelConfiguration', null);
    declareButton({
      color: 'primary', icon: 'save', text: 'Save', list: 'permissions'
    }, this, 'savePermissions', null);
    declareButton({
      icon: 'arrow_back', text: 'Cancel', list: 'permissions'
    }, this, 'cancelPermissions', null);
    declareButton({
      color: 'primary', icon: 'save', text: 'Save', list: 'children', enabledIf: () => this.portofino.localApiAvailable
    }, this, 'saveChildren', null);
    declareButton({
      icon: 'arrow_back', text: 'Cancel', list: 'children'
    }, this, 'cancelChildren', null);
    declareButton({
      icon: 'arrow_back', text: 'Back', list: 'breadcrumbs', presentIf: () => this.canGoBack()
    }, this, 'goBack', null);
  }

  initialize() {}

  protected getPageSettingsPanel() {
    return new PageSettingsPanel(this);
  }

  subscribe<T>(
    observable: Observable<T>, observer: PartialObserver<T> | ((value: T) => void),
    error?: (error: any) => void, complete?: () => void): Subscription {
    let subscription;
    if(observer instanceof Function) {
      subscription = observable.subscribe(observer as (value: T) => void, error, complete);
    } else {
      subscription = observable.subscribe(observer as PartialObserver<T>);
    }
    this.subscriptions.push(subscription);
    return subscription;
  }

  ngOnDestroy() {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }

  consumePathSegment(fragment: string): boolean {
    return true;
  }

  get root(): Page {
    if(this.parent) {
      return this.parent.root;
    } else {
      return this;
    }
  }

  get children(): PageChild[] {
    return this.configuration[this.childrenProperty] || []
  }

  get childrenProperty(): string {
    return "children";
  }

  get embeddedChildren() {
    return this.children.filter(c => this.allowEmbeddedComponents && c.embedded && c.accessible);
  }

  get title() {
    let title = this.configuration.title;
    if(!title && this.parent) {
      let pageChild = this.parent.children.find(c => c.path == this.segment);
      if(pageChild) {
        title = pageChild.title;
      }
    }
    return title;
  }

  get icon() {
    if(this.configuration && this.configuration.icon) {
      return this.configuration.icon;
    } else if(this.parent) {
      let pageChild = this.parent.children.find(c => c.path == this.segment);
      return pageChild ? pageChild.icon : null;
    } else {
      return null;
    }
  }

  getChild(segment: string) {
    return this.children.find(c => c.path == segment);
  }

  getButtons(list = 'default') {
    return getButtons(this, list);
  }

  get template(): TemplateRef<any> {
    const template = this.configuration.template;
    const templateName = template && template.v ? template.v : template;
    if(templateName) {
      const template = this.portofino.templates[templateName];
      if(!template) {
        console.error("Unknown template", templateName);
      }
      return template.template;
    } else {
      return null; //use the default template
    }
  }

  prepare(): Observable<Page> {
    if(this.parent && this.parent.getChild(this.segment) && this.parent.getChild(this.segment).accessible) {
      return of(this);
    }
    return this.checkAccess(true).pipe<Page>(map(() => this)).pipe(catchError(() => of(this)));
  }

  checkAccess(askForLogin: boolean): Observable<any> {
    let headers = new HttpHeaders();
    if(!askForLogin) {
      headers = headers.set(NO_AUTH_HEADER, 'true');
    }
    let sourceUrl = this.computeSourceUrl();
    const securityCheckPath = (this.configuration.securityCheckPath || ':description');
    if(!sourceUrl.endsWith('/') && !securityCheckPath.startsWith('/')) {
      sourceUrl += '/';
    } else if(sourceUrl.endsWith('/') && securityCheckPath.startsWith('/')) {
      sourceUrl = sourceUrl.substring(0, sourceUrl.length - 1);
    }
    return this.http.get<any>(
      sourceUrl + securityCheckPath,
      { headers: headers });
  }

  get accessPermitted(): Observable<boolean> {
    return this.checkAccess(false).pipe(map(() => true), catchError(() => of(false)));
  }

  computeSourceUrl() {
    let source = this.configuration.source || '';
    if(source.startsWith('http://') || source.startsWith('https://')) {
      //Absolute, leave as is
    } else if(!source.startsWith('/')) {
      if(this.parent) {
        source = this.parent.computeSourceUrl() + '/' + source;
      } else {
        source = this.portofino.apiRoot + '/' + source;
      }
    } else {
      source = this.portofino.apiRoot + source;
    }
    source = Page.removeDoubleSlashesFromUrl(source);
    while (source.endsWith("/"))  {
      source = source.substring(0, source.length - 1);
    }
    return source;
  }

  static removeDoubleSlashesFromUrl(url) {
    //Replace all double slash (g flag), but not in http(s)://
    return url.replace(new RegExp("([^:])//", "g"), '$1/');
  }

  operationAvailable(ops: Operation[], signature: string) {
    return ops.some(op => op.signature == signature && op.available);
  }

  loadPageConfiguration(path: string) {
    return this.http.get<PageConfiguration>(this.getConfigurationLocation(path));
  }

  loadChildConfiguration(child: PageChild) {
    return this.loadPageConfiguration(`${this.path}/${child.path}`);
  }

  getConfigurationLocation(path: string = this.path) {
    return Page.removeDoubleSlashesFromUrl(`pages${path}/config.json`);
  }

  configure(callback: (saved: boolean) => void = () => this.embedded ? this.parent.reloadBaseUrl() : this.reloadBaseUrl()) {
    if(!this.authenticationService.isAdmin) {
      return;
    }
    this.settingsPanel.show(callback);
  }

  saveConfiguration() {
    const actionConfiguration = this.settingsPanel.getActionConfigurationToSave();
    const path = this.getConfigurationLocation(this.path);
    let saveConfObservable: Observable<any>;
    if (this.portofino.localApiAvailable) {
      const pageConfiguration = this.settingsPanel.getPageConfigurationToSave();
      this.configuration = pageConfiguration;
      let data = new FormData();
      data.append("pageConfiguration", JSON.stringify(pageConfiguration));
      data.append("actionConfiguration", JSON.stringify(actionConfiguration));
      saveConfObservable = this.http.put(`${this.portofino.localApiPath}/${path}`, data, {
        params: {
          actionConfigurationPath: this.configurationUrl,
          loginPath: this.portofino.loginPath
        }
      });
    } else {
      saveConfObservable = this.http.put(`${this.configurationUrl}`, actionConfiguration);
    }
    saveConfObservable.subscribe(() => {
      this.settingsPanel.hide(true);
    }, () => {
      this.notificationService.error(this.translate.get("Error saving the configuration"));
    });
  }

  protected reloadBaseUrl() {
    if (this.router.url && this.router.url != "/") {
      this.router.navigateByUrl(this.baseUrl);
    } else {
      //this.router.navigate(['.'], {relativeTo: this.route});
      window.location.reload(); //TODO
    }
  }

  cancelConfiguration() {
    this.configuration = this.settingsPanel.previousConfiguration;
    this.settingsPanel.hide(false);
  }


  get configurationUrl() {
    return this.computeSourceUrl() + this.configurationPath;
  }

  public loadConfiguration() {
    return this.http.get(this.configurationUrl).pipe(map(c => {
      this.configuration = {...c, ...this.configuration};
      return c;
    }));
  }

  get configurationProperties() {
    return null;
  }

  savePermissions() {
    const permissionsUrl = this.computeSourceUrl() + this.permissionsPath;
    this.settingsPanel.groups.forEach(g => {
      if(g.level == "inherited") {
        g.level = null;
      }
      g.permissions = [];
      for(let p in g.permissionMap) {
        if(g.permissionMap[p]) {
          g.permissions.push(p);
        }
      }
      delete g.permissionMap;
    });
    this.http.put(permissionsUrl, this.settingsPanel.groups).subscribe(() => {
      this.settingsPanel.hide(true);
    });
  }

  cancelPermissions() {
    this.settingsPanel.hide(false);
  }

  saveChildren() {
    if (!this.portofino.localApiAvailable) {
      throw "Local Portofino API not available"
    }
    const pageConfiguration = this.settingsPanel.getPageConfigurationToSave({});
    let data = new FormData();
    data.append("pageConfiguration", JSON.stringify(pageConfiguration));
    const path = this.getConfigurationLocation(this.path);
    this.http.put(`${this.portofino.localApiPath}/${path}`, data, {
      params: { loginPath: this.portofino.loginPath }}).subscribe(
      () => {
        this.configuration = pageConfiguration;
        this.settingsPanel.hide(true);
      });
  }

  cancelChildren() {
    this.settingsPanel.hide(false);
  }

  checkAccessibility(child: PageChild) {
    this.loadChildConfiguration(child).pipe(mergeMap(config => {
      const dummy = new DummyPage(
        this.portofino, this.http, this.router, this.route, this.authenticationService, this.notificationService, this.translate);
      dummy.parent = this;
      dummy.configuration = config;
      return dummy.accessPermitted;
    })).subscribe(flag => child.accessible = flag);
  }

  goBack() {
    if(this.returnUrl) {
      this.router.navigateByUrl(this.returnUrl);
    } else {
      this.goToParent();
    }
  }

  canGoBack(): boolean {
    return this.returnUrl || this.parent;
  }

  goToParent() {
    if (this.parent) {
      this.router.navigateByUrl(this.parent.url);
    }
  }

  /**
   * Extension point. When a request fails because of insufficient privileges, but the user refuses to log in, this
   * method is called. The page has a chance to handle the event and to return false to prevent the default behaviour
   * from happening.
   */
  handleDeclinedLogin() {
    return true;
  }
}

@Component({
  selector: 'portofino-page-header',
  templateUrl: './page-header.component.html',
  styleUrls: ['./page-header.component.scss']
})
export class PageHeader {
  @Input()
  page: Page;
  constructor(public authenticationService: AuthenticationService, public portofino: PortofinoService) {}
}

@Component({
  selector: 'portofino-templates',
  template: `
    <ng-template #defaultTemplate let-content="content" let-page="page">
      <ng-template [ngTemplateOutlet]="content"></ng-template>
      <portofino-page *ngFor="let child of page.embeddedChildren"
                      [parent]="page" [embedded]="true" [segment]="child.path"></portofino-page>
    </ng-template>
    <ng-template #mainWithTabs let-content="content" let-page="page">
      <ng-template [ngTemplateOutlet]="content"></ng-template>
      <mat-tab-group *ngIf="page.embeddedChildren && page.embeddedChildren.length > 0">
        <mat-tab *ngFor="let child of page.embeddedChildren">
          <ng-template mat-tab-label>
            <mat-icon *ngIf="child.icon">{{child.icon}}</mat-icon>
            {{child.title|translate}}
          </ng-template>
          <portofino-page [parent]="page" [embedded]="true" [segment]="child.path"></portofino-page>
        </mat-tab>
      </mat-tab-group>
    </ng-template>`
})
export class TemplatesComponent implements AfterViewInit {

  templates: { [name: string]: { template: TemplateRef<any>, description?: string }} = {};

  @ViewChild("defaultTemplate", { static: true })
  defaultTemplate: TemplateRef<any>;
  @ViewChild("mainWithTabs", { static: true })
  mainWithTabs: TemplateRef<any>;

  ngAfterViewInit(): void {
    this.templates.defaultTemplate = { template: this.defaultTemplate, description: "The default template" };
    this.templates.mainWithTabs = { template: this.mainWithTabs, description: "Page with embedded pages as tabs" };
  }
}

@Component({
  selector: 'portofino-page-layout',
  templateUrl: './page-layout.component.html',
  styleUrls: ['./page-layout.component.scss']
})
export class PageLayout implements AfterViewInit {
  @Input()
  page: Page;
  @ContentChild("content", { static: false })
  content: TemplateRef<any>;
  @ViewChild("defaultTemplate", { static: true })
  defaultTemplate: TemplateRef<any>;
  @ContentChild("extraConfiguration", { static: false })
  extraConfiguration: TemplateRef<any>;

  template: TemplateRef<any>;

  constructor(protected changeDetector: ChangeDetectorRef) {}

  ngAfterViewInit(): void {
    const template = this.page.template;
    this.template = template ? template : this.defaultTemplate;
    this.changeDetector.detectChanges();
  }
}

export class Operation {
  name: string;
  signature: string;
  parameters: string[];
  available: boolean;
}

export class Permissions {
  groups: Group[];
  permissions: string[];
}

export class Group {
  name: string;
  level: string;
  actualAccessLevel: string;
  permissions: string[];
  permissionMap: {[name: string]: boolean};
}

class DummyPage extends Page {}

import {
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  ContentChild,
  Directive,
  EventEmitter,
  Injectable,
  InjectionToken,
  Input,
  OnDestroy, OnInit,
  Optional,
  TemplateRef,
  Type,
  ViewChild
} from "@angular/core";
import {Annotation, ClassAccessor, loadClassAccessor, Property} from "./class-accessor";
import {FormGroup} from "@angular/forms";
import {PortofinoService, TemplateDescriptor} from "./portofino.service";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Field, FieldSet, Form} from "./form";
import {ActivatedRoute, Router} from "@angular/router";
import {AuthenticationService} from "./security/authentication.service";
import {ButtonInfo, declareButton, getAvailableButtonLists, getButtons, WithButtons} from "./buttons";
import {Observable, of, PartialObserver, Subscription} from "rxjs";
import {catchError, map} from "rxjs/operators";
import {NotificationService} from "./notifications/notification.services";
import {TranslateService} from "@ngx-translate/core";
import {NO_AUTH_HEADER} from "./security/authentication.headers";
import {ThemePalette} from "@angular/material/core";
import * as moment from 'moment';
import {Location} from "@angular/common";

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
  templateUrl: '../../assets/navigation.component.html',
  styleUrls: ['../../assets/navigation.component.scss']
})
export class DefaultNavigationComponent {
  constructor(public pageService: PageService) {}
}

export class PageConfiguration {
  type?: string;
  actualType?: Type<any>;
  title: string;
  source: string;
  children: PageChild[] = [];
  icon?: string;
  template?: string;
  buttons?: { [list: string]: ButtonConfiguration[] };
  script?: string;
}

export class ButtonConfiguration {
  icon?: string;
  text?: string;
  color: ThemePalette;
  method: string;
}

export class PageChild {
  path: string;
  title: string;
  icon?: string;
  // deprecated, use embeddedIn
  embedded?: boolean;
  embeddedIn?: string;
  showInNavigation?: boolean;
  accessible?: boolean;
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
  children = true;
  buttons = true;

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
    if(!this.page.hasSource()) {
      return;
    }
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
    if(this.callback) {
      this.callback(saved);
    }
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
      this.loadActionConfiguration().subscribe(ca => {
        this.setupConfigurationForm(ca, conf);
      }, error => {
        this.setupConfigurationForm(null, null);
        this.error = error;
      })
      //this.page.settingsPanel.refreshConfiguration(); TODO
    });
  }

  protected loadActionConfiguration() {
    if(!this.page.hasSource()) {
      return of(null);
    }
    return this.page.http.get<ClassAccessor>(
      this.page.configurationUrl + '/classAccessor').pipe(map(loadClassAccessor));
  }

  getActionConfigurationToSave() {
    if(!this.page.hasSource()) {
      return null;
    }
    const configuration = this.form.get('configuration');
    return configuration ? Object.assign({}, configuration.value) : null;
  }

  getPageConfigurationToSave(formValue = this.form.value) {
    const config = Object.assign({}, this.page.configuration, formValue);
    const pageConfiguration = new PageConfiguration();
    //Reflection would be nice
    pageConfiguration.buttons = config.buttons;
    pageConfiguration.children = config.children;
    pageConfiguration.icon = config.icon;
    pageConfiguration.source = config.source;
    pageConfiguration.template = config.template ? config.template.v : null;
    pageConfiguration.title = config.title;
    pageConfiguration.type = config.type;
    return pageConfiguration;
  }
}

@Directive()
export abstract class Page implements WithButtons, OnDestroy, OnInit {

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
  navigationMenu: NavigationMenu;

  readonly operationsPath = '/:operations';
  readonly configurationPath = '/:configuration';
  readonly permissionsPath = '/:permissions';
  readonly page = this;

  protected readonly subscriptions: Subscription[] = [];

  constructor(
    public portofino: PortofinoService, public http: HttpClient, protected router: Router,
    @Optional() protected route: ActivatedRoute, public authenticationService: AuthenticationService,
    protected notificationService: NotificationService, public translate: TranslateService,
    @Optional() protected location: Location) {
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

  ngOnInit(): void {
    // This.path is undefined when the page hasn't been created by the page factory and is, therefore, out of the
    // Portofino application
    if(this.path === undefined) {
      // We're using embedded as a way to obtain a behaviour which is more suited to usage outside of Portofino,
      // but this is incorrect and only a shortcut. Ideally, a dedicated flag should exist.
      this.embedded = true;
      this.baseUrl = this.url = this.location?.path() || "";
      this.initialize();
    }
  }

  initialize() {
    this.computeNavigationMenu();
    if(typeof(this.configuration) === 'string') {
      //Play nice with web components
      this.configuration = JSON.parse(this.configuration as string);
    }
    const config = this.configuration;
    if(config && config.script) {
      this.http.get(Page.removeDoubleSlashesFromUrl(`pages${this.path}/${config.script}`), {
        responseType: "text"
      }).subscribe(s => {
        let factory = new Function(`return function(page, forms, moment) { ${s} }`);
        let userFunction = factory();
        const forms = {
          ClassAccessor: ClassAccessor,
          Property: Property,
          Annotation: Annotation,
          Field: Field,
          FieldSet: FieldSet,
          Form: Form,
        };
        userFunction(this, forms, moment);
      }, () => {
        this.notificationService.error(this.translate.get("Could not load page script"));
      });
    }
    this.setupCustomButtons(config);
  }

  protected setupCustomButtons(config) {
    if (config && config.buttons) {
      for (let list in config.buttons) {
        let buttons = config.buttons[list];
        buttons.forEach(b => {
          let info = new ButtonInfo();
          info.list = list;
          info.color = b.color;
          info.icon = b.icon;
          info.text = b.text;
          let methodName = b.method || 'noActionForButton';
          declareButton(info, this, methodName, null);
        });
      }
    }
  }

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

  getEmbeddedChildren(section = "default") {
    return this.children.filter(c => this.allowEmbeddedComponents && c.embeddedIn == section && c.accessible);
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

  //Buttons
  getButtons(list = 'default') {
    return getButtons(this, list);
  }

  protected setupBasicPageButtons() {
    this.declareButton({
      color: 'primary', icon: 'save', text: 'Save', list: 'configuration',
      enabledIf: () => this.settingsPanel.isValid()
    }, 'saveConfiguration');
    this.declareButton({
      icon: 'arrow_back', text: 'Cancel', list: 'configuration'
    }, 'cancelConfiguration',);
    this.declareButton({
      color: 'primary', icon: 'save', text: 'Save', list: 'permissions'
    }, 'savePermissions');
    this.declareButton({
      icon: 'arrow_back', text: 'Cancel', list: 'permissions'
    }, 'cancelPermissions');
    this.declareButton({
      color: 'primary', icon: 'save', text: 'Save', list: 'children', enabledIf: () => this.portofino.localApiAvailable
    }, 'saveChildren');
    this.declareButton({
      icon: 'arrow_back', text: 'Cancel', list: 'children'
    }, 'cancelChildren');
    this.declareButton({
      icon: 'arrow_back', text: 'Back', list: 'breadcrumbs', presentIf: () => this.canGoBack()
    }, 'goBack');
  }

  declareButton(info: ButtonInfo | any, methodName: string) {
    declareButton(info, this, methodName, null);
  }

  getAvailableButtonLists() {
    return getAvailableButtonLists(this);
  }

  noActionForButton(event) {
    if(console) {
      console.error("Not implemented", event);
    }
  }
  //End buttons

  get template(): TemplateDescriptor {
    const template = this.configuration.template;
    const templateName = (template && template.v) ? template.v : template;
    if(templateName) {
      const template = this.portofino.templates[templateName];
      if(!template) {
        console.error("Unknown template", templateName);
      }
      return template;
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
    return this.doCheckAccess(askForLogin, this.computeSecurityCheckUrl(), true);
  }

  get accessibleChildren(): Observable<string[]> {
    return this.doCheckAccess(false, this.computeChildrenSecurityCheckUrl(), []);
  }

  protected doCheckAccess<T>(askForLogin: boolean, securityCheckUrl: string, defaultResult: T): Observable<T> {
    let headers = new HttpHeaders();
    if (!askForLogin) {
      headers = headers.set(NO_AUTH_HEADER, 'true');
    }
    if (securityCheckUrl) {
      return this.http.get(securityCheckUrl, {headers: headers, observe: "response"}).pipe(map(r => r.body as T));
    } else {
      return of(defaultResult);
    }
  }

  protected computeSecurityCheckUrl() {
    return this.computeSourceUrl() + "/:accessible";
  }

  protected computeChildrenSecurityCheckUrl() {
    return this.computeSourceUrl() + "/:accessible-children";
  }

  computeSourceUrl() {
    return Page.defaultComputeSourceUrl(this.portofino.apiRoot, this.parent, this.configuration.source);
  }

  hasSource() {
    return true;
  }

  public static defaultComputeSourceUrl(apiRoot: string, parent: Page, source: string) {
    source = source ? source : '';
    if (source.startsWith('http://') || source.startsWith('https://')) {
      //Absolute, leave as is
    } else {
      if (!source.startsWith('/')) {
        if (parent) {
          source = parent.computeSourceUrl() + '/' + source;
        } else {
          source = apiRoot + '/' + source;
        }
      } else {
        source = apiRoot + source;
      }
    }
    source = Page.removeDoubleSlashesFromUrl(source);
    while (source.endsWith("/")) {
      source = source.substring(0, source.length - 1);
    }
    return source;
  }

  public static defaultComputeSecurityCheckUrl(apiRoot: string, parent: Page, source: string) {
    return Page.defaultComputeSourceUrl(apiRoot, parent, source) + "/:accessible";
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
      if(actionConfiguration) {
        data.append("actionConfiguration", JSON.stringify(actionConfiguration));
      }
      saveConfObservable = this.http.put(`${this.portofino.localApiPath}/${path}`, data, {
        params: {
          actionConfigurationPath: this.configurationUrl
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

  reloadBaseUrl() {
    if (this.router.url && this.router.url != "/" && this.router.url != this.baseUrl) {
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
    if(!this.hasSource()) {
      return of({...this.configuration});
    }
    return this.doLoadConfiguration();
  }

  protected doLoadConfiguration() {
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
    this.http.put(`${this.portofino.localApiPath}/${path}`, data).subscribe(
      () => {
        this.configuration = pageConfiguration;
        this.settingsPanel.hide(true);
      });
  }

  cancelChildren() {
    this.settingsPanel.hide(false);
  }

  goBack() {
    if(this.returnUrl) {
      if(this.url == this.returnUrl) {
        window.location.reload(); //TODO
      } else {
        this.router.navigateByUrl(this.returnUrl);
      }
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

  computeNavigationMenu() {
    const menu = new NavigationMenu();
    menu.current = NavigationMenuItem.from(this);
    if(this.parent) {
      menu.parent = NavigationMenuItem.from(this.parent);
      this.parent.children.forEach(child => {
        if(child.path == this.segment) {
          menu.siblings.push(menu.current);
        } else if(child.accessible && child.showInNavigation) {
          menu.siblings.push(new NavigationMenuItem(this.parent.url + '/' + child.path, child.title, child.icon));
        }
      });
    }
    this.children.forEach(child => {
      if(child.accessible && child.showInNavigation) {
        menu.children.push(new NavigationMenuItem(this.url + '/' + child.path, child.title, child.icon));
      }
    });
    return this.navigationMenu = menu;
  }
}

@Component({
  selector: 'portofino-page-header',
  templateUrl: '../../assets/page-header.component.html',
  styleUrls: ['../../assets/page-header.component.scss']
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
      <portofino-page *ngFor="let child of page.getEmbeddedChildren()"
                      [parent]="page" [embedded]="true" [segment]="child.path"></portofino-page>
    </ng-template>
    <ng-template #mainWithTabs let-content="content" let-page="page">
      <ng-template [ngTemplateOutlet]="content"></ng-template>
      <portofino-page *ngFor="let child of page.getEmbeddedChildren('top')"
                      [parent]="page" [embedded]="true" [segment]="child.path"></portofino-page>
      <mat-tab-group *ngIf="page.getEmbeddedChildren() && page.getEmbeddedChildren().length > 0">
        <mat-tab *ngFor="let child of page.getEmbeddedChildren()">
          <ng-template mat-tab-label>
            <mat-icon *ngIf="child.icon">{{child.icon}}</mat-icon>
            {{child.title|translate}}
          </ng-template>
          <portofino-page [parent]="page" [embedded]="true" [segment]="child.path"></portofino-page>
        </mat-tab>
      </mat-tab-group>
      <portofino-page *ngFor="let child of page.getEmbeddedChildren('bottom')"
                      [parent]="page" [embedded]="true" [segment]="child.path"></portofino-page>
    </ng-template>`
})
export class TemplatesComponent implements AfterViewInit {

  templates: { [name: string]: TemplateDescriptor} = {};

  @ViewChild("defaultTemplate", { static: true })
  defaultTemplate: TemplateRef<any>;
  @ViewChild("mainWithTabs", { static: true })
  mainWithTabs: TemplateRef<any>;

  ngAfterViewInit(): void {
    this.templates.defaultTemplate = { template: this.defaultTemplate, description: "The default template", sections: ["default"] };
    this.templates.mainWithTabs = {
      template: this.mainWithTabs,
      description: "Page with embedded pages as tabs",
      sections: ["top", "default", "bottom"]
    };
  }
}

@Component({
  selector: 'portofino-page-layout',
  templateUrl: '../../assets/page-layout.component.html',
  styleUrls: ['../../assets/page-layout.component.scss']
})
export class PageLayout implements AfterViewInit {
  @Input()
  page: Page;
  @ContentChild("content")
  content: TemplateRef<any>;
  @ViewChild("defaultTemplate", { static: true })
  defaultTemplate: TemplateRef<any>;
  @ContentChild("extraConfiguration")
  extraConfiguration: TemplateRef<any>;

  template: TemplateRef<any>;

  constructor(protected changeDetector: ChangeDetectorRef) {}

  ngAfterViewInit(): void {
    const template = this.page.template;
    this.template = template ? template.template : this.defaultTemplate;
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

export class NavigationMenu {
  parent?: NavigationMenuItem;
  current: NavigationMenuItem;
  readonly siblings: NavigationMenuItem[] = [];
  readonly children: NavigationMenuItem[] = [];
}

export class NavigationMenuItem {
  title: string;
  url: string;
  icon?: string;

  static from(page: Page) {
    return new NavigationMenuItem(page.url, page.title, page.icon);
  }

  constructor(url: string, title: string, icon?: string) {
    this.url = url;
    this.title = title;
    this.icon = icon;
  }
}

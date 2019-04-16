import {
  Component,
  ContentChild,
  EventEmitter,
  Injectable,
  InjectionToken,
  Input, OnDestroy,
  OnInit, Optional,
  TemplateRef, Type
} from "@angular/core";
import {ANNOTATION_REQUIRED, ClassAccessor, loadClassAccessor, Property} from "./class-accessor";
import {FormControl, FormGroup} from "@angular/forms";
import {PortofinoService} from "./portofino.service";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Field, FieldSet, Form} from "./form";
import {ActivatedRoute, Router} from "@angular/router";
import {AuthenticationService, NO_AUTH_HEADER} from "./security/authentication.service";
import {declareButton, getButtons} from "./buttons";
import {BehaviorSubject, merge, Observable, of, PartialObserver, Subscription} from "rxjs";
import {catchError, debounceTime, map} from "rxjs/operators";
import {MatDialog, MatDialogRef} from "@angular/material";
import {FlatTreeControl} from "@angular/cdk/tree";
import {CollectionViewer, SelectionChange} from "@angular/cdk/collections";
import {WithButtons} from "./button.component";
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
  callback: (boolean) => void;

  constructor(public page: Page) {}

  show(callback: (boolean) => void = () => {}) {
    this.callback = callback;
    const titleField = Field.fromProperty(Property.create({name: 'title', label: 'Title'}).required(), this.page.configuration);
    titleField.editable = this.page.portofino.localApiAvailable;
    const iconField = Field.fromProperty({name: 'icon', label: 'Icon'}, this.page.configuration);
    iconField.editable = this.page.portofino.localApiAvailable;
    this.formDefinition.contents = [titleField, iconField];
    this.previousConfiguration = this.page.configuration;
    this.reloadConfiguration();
    this.loadPermissions();
    this.active = true;
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
    return this.form.get('configuration').value;
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
    declareButton({
      color: 'primary', icon: 'save', text: 'Save', list: 'configuration'
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
    if(this.parent) {
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

  prepare(): Observable<Page> {
    return this.checkAccess(true).pipe<Page>(map(() => this));
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
    //replace double slash, but not in http(s)://
    source = source.replace(new RegExp("([^:])//"), '$1/');
    while (source.endsWith("/"))  {
      source = source.substring(0, source.length - 1);
    }
    return source;
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
    //replace double slash, but not in http(s)://
    return `pages${path}/config.json`.replace(new RegExp("([^:])//"), '$1/');
  }

  configure(callback: (boolean) => void = () => this.reloadBaseUrl()) {
    this.settingsPanel.show(callback);
  }

  saveConfiguration() {
    const actionConfiguration = this.settingsPanel.getActionConfigurationToSave();
    const path = this.getConfigurationLocation(this.path);
    let saveConfObservable: Observable<any>;
    if (this.portofino.localApiAvailable) {
      const pageConfiguration = this.getPageConfigurationToSave(this.settingsPanel.form.value);
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

  //TODO refactor into page settings panel
  protected getPageConfigurationToSave(formValue) {
    const config = Object.assign({}, this.configuration, formValue);
    const pageConfiguration = new PageConfiguration();
    //Reflection would be nice
    pageConfiguration.children = config.children;
    pageConfiguration.securityCheckPath = config.securityCheckPath;
    pageConfiguration.source = config.source;
    pageConfiguration.title = config.title;
    pageConfiguration.type = config.type;
    return pageConfiguration;
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
    const pageConfiguration = this.getPageConfigurationToSave({});
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
}

@Component({
  selector: 'portofino-page-header',
  templateUrl: './page-header.component.html',
  styleUrls: ['./page-header.component.css']
})
export class PageHeader {
  @Input()
  page: Page;
  constructor(public authenticationService: AuthenticationService, public portofino: PortofinoService) {}
}

@Component({
  selector: 'portofino-default-page-layout',
  templateUrl: './default-page-layout.component.html',
  styleUrls: ['./default-page-layout.component.css']
})
export class DefaultPageLayout {
  @Input()
  page: Page;
  @ContentChild("content")
  content: TemplateRef<any>;
  @ContentChild("extraConfiguration")
  extraConfiguration: TemplateRef<any>;
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

import {
  AfterViewInit,
  Component,
  ComponentFactoryResolver, ContentChild, Host, Inject,
  InjectionToken, Input, OnDestroy,
  OnInit, TemplateRef,
  ViewChild
} from '@angular/core';
import {ActivatedRoute, Router, UrlSegment} from "@angular/router";
import {PortofinoAppComponent} from "./portofino-app.component";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {EmbeddedContentDirective, MainContentDirective, NavigationDirective} from "./content.directive";
import {Observable, Subscription} from "rxjs";
import {catchError, map, mergeMap} from "rxjs/operators";
import {ThemePalette} from "@angular/material/core/typings/common-behaviors/color";
import {PortofinoService} from "./portofino.service";
import {of} from "rxjs";
import {AuthenticationService, NO_AUTH_HEADER} from "./security/authentication.service";
import {FormControl, FormGroup} from "@angular/forms";
import {ANNOTATION_REQUIRED, ClassAccessor, Property} from "./class-accessor";
import {Field, Form, FormComponent} from "./form";

export const NAVIGATION_COMPONENT = new InjectionToken('Navigation Component');

@Component({
  selector: 'portofino-page',
  templateUrl: './page.component.html',
  styleUrls: ['./page.component.css']
})
export class PageComponent implements AfterViewInit, OnInit, OnDestroy {

  @ViewChild(NavigationDirective)
  navigationHost: NavigationDirective;
  @ViewChild(MainContentDirective)
  contentHost: MainContentDirective;
  @ViewChild(EmbeddedContentDirective)
  embeddedContentHost: EmbeddedContentDirective;

  error;
  navigation: NavigationComponent;

  protected subscription: Subscription;

  constructor(protected route: ActivatedRoute, protected http: HttpClient, protected router: Router,
              protected componentFactoryResolver: ComponentFactoryResolver,
              protected portofino: PortofinoService, @Inject(NAVIGATION_COMPONENT) protected navigationComponent,
              protected authenticationService: AuthenticationService) { }

  ngOnInit() {
    //Dynamically create the navigation component
    let componentFactory = this.componentFactoryResolver.resolveComponentFactory(this.navigationComponent);
    this.navigation = this.navigationHost.viewContainerRef.createComponent(componentFactory).instance as NavigationComponent;
  }

  ngAfterViewInit() {
    this.subscription = this.route.url.subscribe(segment => {
      this.error = null;
      this.navigation.page = null;
      this.loadPageInPath("", null, segment, 0, false);
    });
  }

  ngOnDestroy() {
    if(this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  protected loadPageInPath(path: string, parent: Page, segments: UrlSegment[], index: number, embed: boolean) {
    this.loadPage(path, parent, embed).subscribe(
      (page: Page) => {
        page.baseUrl = '/' + segments.slice(0, index).join('/');
        page.url = page.baseUrl;
        for(let i = index; i < segments.length; i++) {
          let s = segments[i];
          if (page.consumePathSegment(s.path)) {
            path += `/${s.path}`;
            let child = page.getChild(s.path);
            if(child) {
              this.loadPageInPath(path, page, segments, i + 1, false);
              return;
            } else {
              this.error = `Nonexistent child of ${page.url}: ${s.path}`;
              return;
            }
          } else {
            page.url += `/${s.path}`;
          }
        }
        //If we arrive here, there are no more children in the URL to process
        if(!embed) {
          this.navigation.page = page;
          page.children.forEach(child => {
            this.checkAccessibility(page, child);
            if(page.allowEmbeddedComponents && child.embedded) {
              let newSegments = segments.slice(0, segments.length);
              newSegments.push(new UrlSegment(child.path, {}));
              this.loadPageInPath(path + `/${child.path}`, page, newSegments, newSegments.length, true);
            }
          });
          if(page.parent) {
            page.parent.children.forEach(child => {
              this.checkAccessibility(page.parent, child);
            });
          }
        }
      },
      error => this.handleErrorInLoadingPage(path, error));
  }

  checkAccessibility(parent: Page, child: PageChild) {
    let dummy = new DummyPage(this.portofino, this.http, this.router, this.authenticationService);
    dummy.parent = parent;
    this.loadPageConfiguration(parent.path + '/' + child.path).pipe(mergeMap(config => {
      dummy.configuration = config;
      return dummy.accessPermitted;
    })).subscribe(flag => child.accessible = flag);
  }

  private handleErrorInLoadingPage(path, error) {
    console.log("Error in loading page " + path, error);
    this.error = error;
  }

  protected loadPage(path: string, parent: Page, embed: boolean): Observable<Page> {
    return this.loadPageConfiguration(path).pipe(
      mergeMap(config => this.createPageComponent(config, path, parent, embed)));
  }

  protected loadPageConfiguration(path: string) {
    return this.http.get<PageConfiguration>(this.getConfigurationLocation(path));
  }

  protected getConfigurationLocation(path: string) {
    return `pages${path}/config.json`;
  }

  protected createPageComponent(config: PageConfiguration, path: string, parent: Page, embed: boolean): Observable<Page> {
    const componentType = PortofinoAppComponent.components[config.type];
    if (!componentType) {
      this.error = "Unknown component type: " + config.type;
      return new Observable<Page>();
    }

    let componentFactory = this.componentFactoryResolver.resolveComponentFactory(componentType);

    let viewContainerRef;
    if (!embed) {
      viewContainerRef = this.contentHost.viewContainerRef;
      viewContainerRef.clear(); //Remove main component
      if (this.embeddedContentHost) {
        this.embeddedContentHost.viewContainerRef.clear();  //Remove embedded components
      }
    } else {
      viewContainerRef = this.embeddedContentHost.viewContainerRef;
      //TODO insert some kind of separator?
    }

    let componentRef = viewContainerRef.createComponent(componentFactory);
    const component = <Page>componentRef.instance;
    component.configuration = config;
    component.path = path;
    component.configurationLocation = this.getConfigurationLocation(path);
    const lastIndexOf = path.lastIndexOf('/');
    if (lastIndexOf >= 0) {
      component.segment = path.substring(lastIndexOf + 1);
    } else {
      component.segment = path;
    }
    component.parent = parent;
    return component.prepare();
  }
}

export interface NavigationComponent {
  page: Page;
}

@Component({
  selector: 'portofino-default-navigation',
  templateUrl: './navigation.component.html',
  styleUrls: ['./navigation.component.css']
})
export class DefaultNavigationComponent implements NavigationComponent {
  page: Page;
}

export class PageConfiguration {
  type: string;
  title: string;
  children: PageChild[];
  source: string;
  securityCheckPath: string = ':description';
}

export class PageChild {
  path: string;
  title: string;
  embedded: boolean;
  accessible: boolean;
}

@Component({
  selector: 'portofino-page-source-selector',
  templateUrl: 'source-selector.html'
})
export class SourceSelector implements OnInit {
  @Input()
  page: Page;
  @Input()
  property: Property;
  @Input()
  initialValue: string;
  form: FormGroup;

  constructor(public portofino: PortofinoService, protected http: HttpClient) {}

  ngOnInit(): void {
    const relativeToParent = new FormControl({
      value: this.page.parent && !this.initialValue.startsWith('/'),
      disabled: !this.page.parent
    });
    const source = new FormControl(this.initialValue);
    this.form = new FormGroup({ source: source, relativeToParent: relativeToParent });
    relativeToParent.valueChanges.subscribe(value => {
      console.log('relative? ', value);
      const parentSourceUrl = this.page.parent.computeSourceUrl() + '/';
      if(value) {
        source.setValue(source.value.substring((parentSourceUrl).length));
      } else {
        source.setValue(parentSourceUrl + source.value);
      }
    });
    this.http.get(this.portofino.apiRoot + ':description')
      .subscribe(page => console.log("Page", page));
  }

  select() {
    //TODO implement with tree
  }

}

export class PageSettingsPanel {
  active: boolean;
  readonly form = new FormGroup({});
  readonly formDefinition = new Form();
  classAccessor: ClassAccessor = {
    name: 'configuration',
    properties: [],
    keyProperties: []
  };

  constructor(public page: Page) {}

  refresh() {
    this.formDefinition.contents = [{
      component: SourceSelector,
      dependencies: {
        page: this.page,
        property: Object.assign(new Property(), {
          name: 'source',
          type: 'string',
          label: 'Path or URL',
          annotations: [{ type: ANNOTATION_REQUIRED, properties: [true] }]
        }),
        initialValue: this.page.configuration.source
      }
    }];
    this.classAccessor.properties.forEach(p => {
      this.createFieldForProperty(p);
    });
  }

  protected createFieldForProperty(p) {
    const field = new Field();
    field.property = p;
    field.initialState = this.page.configuration[p.name];
    this.formDefinition.contents.push(field);
  }
}

export abstract class Page {

  configuration: PageConfiguration & any;
  configurationLocation: string;
  readonly settingsPanel = new PageSettingsPanel(this);
  path: string;
  baseUrl: string;
  url: string;
  segment: string;
  parent: Page;
  allowEmbeddedComponents: boolean = true;

  readonly operationsPath = '/:operations';
  readonly configurationPath = '/:configuration';
  readonly page = this;

  protected constructor(
    protected portofino: PortofinoService, protected http: HttpClient, protected router: Router,
    public authenticationService: AuthenticationService) {
    //Declarative approach does not work for some reason:
    //"Metadata collected contains an error that will be reported at runtime: Lambda not supported."
    //TODO investigate with newer versions
    declareButton({
      color: 'primary', icon: 'save', text: 'Save', list: 'configuration'
    }, this, 'saveConfiguration', null);
    declareButton({
      icon: 'arrow_back', text: 'Cancel', list: 'configuration'
    }, this, 'cancelConfiguration', null);
  }

  consumePathSegment(fragment: string): boolean {
    return true;
  }

  get children(): PageChild[] {
    return this.configuration.children
  }

  getChild(segment: string) {
    return this.children.find(c => c.path == segment);
  }

  getButtons(list: string = 'default'): ButtonInfo[] | null {
    const allButtons = this[BUTTONS];
    return allButtons ? allButtons[list] : null;
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
    let source = "";
    if(!this.configuration.source || !this.configuration.source.startsWith('/') ||
       !source.startsWith('http://') || !source.startsWith('https://')) {
      if(this.parent) {
        source = this.parent.computeSourceUrl() + '/';
      }
    }
    if(!source) {
      source = this.portofino.apiRoot;
    }
    return (source + (this.configuration.source ? this.configuration.source : ''))
      //replace double slash, but not in http://
      .replace(new RegExp("([^:])//"), '$1/');
  }

  operationAvailable(ops: Operation[], signature: string) {
    return ops.some(op => op.signature == signature && op.available);
  }

  get supportedSourceTypes(): string[] {
    return [];
  }

  configure() {
    this.settingsPanel.refresh();
    this.settingsPanel.active = true;
  }

  saveConfiguration() {
    const config = this.getConfigurationToSave(this.settingsPanel.form.value);
    this.portofino.saveConfiguration(this.configurationLocation, config).subscribe(
      () => {
        this.settingsPanel.active = false;
        this.router.navigateByUrl(this.router.url);
      },
      error => console.log(error));
  }

  protected getConfigurationToSave(formValue) {
    const config = Object.assign({}, this.configuration, formValue);
    delete config.relativeToParent;
    return config;
  }

  cancelConfiguration() {
    this.settingsPanel.active = false;
  }

}

class DummyPage extends Page {
  constructor(
    protected portofino: PortofinoService, protected http: HttpClient, protected router: Router,
    public authenticationService: AuthenticationService) {
    super(portofino, http, router, authenticationService);
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
  content: TemplateRef<any>
}

///////////
//Buttons//
///////////
export const BUTTONS = "__portofinoButtons__";

export class ButtonInfo {
  list: string = 'default';
  class: Function;
  methodName: string;
  propertyDescriptor: PropertyDescriptor;
  color: ThemePalette;
  action: (self, event: any | undefined) => void;
  presentIf: (self) => boolean = () => true;
  enabledIf: (self) => boolean = () => true;
  icon: string;
  text: string;
}

export function declareButton(info: ButtonInfo | any, target, methodName: string, descriptor: PropertyDescriptor) {
  info = Object.assign({}, new ButtonInfo(), info);
  info.class = target.constructor;
  info.methodName = methodName;
  info.propertyDescriptor = descriptor;
  info.action = (self, event) => {
    self[methodName].call(self, event);
  };
  if(!target.hasOwnProperty(BUTTONS)) {
    const parentButtons = target[BUTTONS];
    target[BUTTONS] = {};
    if(parentButtons) {
      for(let list in parentButtons) {
        target[BUTTONS][list] = parentButtons[list].slice();
      }
    }
  }
  if(!target[BUTTONS].hasOwnProperty(info.list)) {
    target[BUTTONS][info.list] = [];
  }
  target[BUTTONS][info.list].push(info);
}

export function Button(info: ButtonInfo | any) {
  return function (target, methodName: string, descriptor: PropertyDescriptor) {
    declareButton(info, target, methodName, descriptor)
  }
}

export class Operation {
  name: string;
  signature: string;
  parameters: string[];
  available: boolean;
}

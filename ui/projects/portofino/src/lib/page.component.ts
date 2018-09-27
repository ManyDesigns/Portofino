import {
  AfterViewInit,
  Component,
  ComponentFactoryResolver, Inject,
  InjectionToken, Input,
  OnDestroy,
  OnInit,
  ViewChild
} from '@angular/core';
import {ActivatedRoute, UrlSegment} from "@angular/router";
import {PortofinoAppComponent} from "./portofino-app.component";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {EmbeddedContentDirective, MainContentDirective, NavigationDirective} from "./content.directive";
import {Observable, Subscription} from "rxjs/index";
import {catchError, map, mergeMap} from "rxjs/operators";
import {ThemePalette} from "@angular/material/core/typings/common-behaviors/color";
import {PortofinoService} from "./portofino.service";
import {of} from "rxjs/index";
import {AuthenticationService, NO_AUTH_HEADER} from "./security/authentication.service";

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

  constructor(protected route: ActivatedRoute, protected http: HttpClient,
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
    let dummy = new DummyPage(this.portofino, this.http, this.authenticationService);
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

  private loadPageConfiguration(path: string) {
    return this.http.get<PageConfiguration>(`pages${path}/config.json`);
  }

  protected createPageComponent(config: PageConfiguration, path: string, parent: Page, embed: boolean): Observable<Page> {
    const componentType = PortofinoAppComponent.components[config.type];
    if (!componentType) {
      this.error = Error("Unknown component type: " + config.type);
      return;
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
  securityCheckPath: string = '/:page';
}

export class PageChild {
  path: string;
  title: string;
  embedded: boolean;
  accessible: boolean;
}

export abstract class Page {

  configuration: PageConfiguration & any;
  path: string;
  baseUrl: string;
  url: string;
  segment: string;
  parent: Page;
  allowEmbeddedComponents: boolean = true;

  readonly operationsPath = '/:operations';
  readonly configurationPath = '/:configuration';

  protected constructor(
    protected portofino: PortofinoService, protected http: HttpClient,
    public authenticationService: AuthenticationService) {}

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
    //TODO handle inheritance using prototype chain, either here or in @Button when registering
    const allButtons = this[BUTTONS];
    return allButtons ? allButtons[list] : null;
  }

  prepare(): Observable<Page> {
    return this.checkAccess(true).pipe(map(() => this));
  }

  checkAccess(askForLogin: boolean): Observable<any> {
    let headers = new HttpHeaders();
    if(!askForLogin) {
      headers = headers.set(NO_AUTH_HEADER, 'true');
    }
    return this.http.get<any>(
      this.computeSourceUrl() + (this.configuration.securityCheckPath || '/:page'),
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

}

class DummyPage extends Page {
  constructor(
    protected portofino: PortofinoService, protected http: HttpClient,
    public authenticationService: AuthenticationService) {
    super(portofino, http, authenticationService);
  }
}

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

export const BUTTONS = "__portofinoButtons__";

export function Button(info: ButtonInfo | any) {
  return function (target, methodName: string, descriptor: PropertyDescriptor) {
    info = Object.assign({}, new ButtonInfo(), info);
    info.class = target.constructor;
    info.methodName = methodName;
    info.propertyDescriptor = descriptor;
    info.action = (self, event) => {
      self[methodName].call(self, event);
    };
    if(!target.hasOwnProperty(BUTTONS)) {
      target[BUTTONS] = {};
    }
    if(!target[BUTTONS].hasOwnProperty(info.list)) {
      target[BUTTONS][info.list] = [];
    }
    target[BUTTONS][info.list].push(info);
  }
}

export class Operation {
  name: string;
  signature: string;
  parameters: string[];
  available: boolean;
}

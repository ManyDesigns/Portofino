import {Page, PageChild, PageConfiguration} from "./page";
import {
  Component,
  ComponentFactoryResolver,
  ComponentRef, EventEmitter,
  Injector,
  Input, OnChanges, OnInit, Optional, Output, SimpleChanges, Type,
  ViewContainerRef
} from "@angular/core";
import {PortofinoService} from "./portofino.service";
import {HttpClient} from "@angular/common/http";
import {ActivatedRoute, Router, UrlSegment} from "@angular/router";
import {AuthenticationService} from "./security/authentication.service";
import {Observable, of, throwError} from "rxjs";
import {map, mergeMap} from "rxjs/operators";
import {NotificationService} from "./notifications/notification.services";
import {TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'portofino-page',
  template: ''
})
export class PageFactoryComponent extends Page implements OnInit, OnChanges {

  static components: { [name: string]: PortofinoComponent } = {};

  @Input()
  parent: Page;
  @Input()
  embedded: boolean;
  @Input()
  segment: string;
  @Input()
  path: string;
  @Input()
  injector: Injector;
  @Output()
  pageCreated = new EventEmitter<Page>();

  loadRootPageConfiguration: () => Observable<PageConfiguration> = () => this.loadPageConfiguration("");

  constructor(portofino: PortofinoService, http: HttpClient, router: Router, @Optional() route: ActivatedRoute,
              authenticationService: AuthenticationService, notificationService: NotificationService,
              translate: TranslateService,
              protected componentFactoryResolver: ComponentFactoryResolver, injector: Injector,
              protected viewContainerRef: ViewContainerRef) {
    super(portofino, http, router, route, authenticationService, notificationService, translate);
    this.injector = injector;
  }

  ngOnInit(): void {
    this.createPage();
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.createPage();
  }

  protected createPage() {
    if (!this.path) {
      if (this.parent) {
        this.path = this.parent.path + '/' + this.segment;
      } else {
        this.path = this.segment;
      }
    }
    let configObservable: Observable<PageConfiguration>;
    if (this.configuration) {
      configObservable = of(this.configuration);
    } else {
      configObservable = this.loadPageConfiguration(this.path);
    }
    configObservable.pipe(mergeMap(config => this.create(config, this.path, this.parent))).subscribe(c => {
      const page = <Page>c.instance;
      page.embedded = this.embedded;
      if (page.parent) {
        page.baseUrl = page.parent.baseUrl + '/' + page.segment;
        page.url = page.parent.url + '/' + page.segment;
      } else {
        page.baseUrl = page.segment;
        page.url = page.segment;
      }
      page.initialize();
      this.viewContainerRef.clear();
      this.viewContainerRef.insert(c.hostView);
      this.pageCreated.emit(page);
    });
  }

  create(config: PageConfiguration, path: string, parent: Page): Observable<ComponentRef<any>> {
    const componentType = config.actualType ? { type: config.actualType } : PageFactoryComponent.components[config.type];
    if (!componentType) {
      return throwError(`Unknown component type '${config.type}' for path '${path}'`);
    }
    let componentFactory = this.componentFactoryResolver.resolveComponentFactory(componentType.type);
    let componentRef = componentFactory.create(this.injector);
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
    return component.prepare().pipe(map(() => componentRef));
  }

  loadPath(path: string) {
    const segments = path.split("/")
      .filter(s => s.length > 0)
      .slice(0, path.length - 1)
      .map(s => new UrlSegment(s, {}));
    return this.load(segments);
  }

  load(segments: UrlSegment[]) {
    return this.loadPage(null, null, segments, 0);
  }

  protected loadPage(parent: Page, child: PageChild, segments: UrlSegment[], index: number): Observable<ComponentRef<Page>> {
    const path = parent ? `${parent.path}/${child.path}` : "";
    const pageConfiguration = parent ? parent.loadChildConfiguration(child) : this.loadRootPageConfiguration();
    return pageConfiguration.pipe(
      mergeMap(config => this.create(config, path, parent)),
      mergeMap(componentRef => {
        const page = <Page>componentRef.instance;
        page.baseUrl = '/' + segments.slice(0, index).join('/');
        page.url = page.baseUrl;
        for (let i = index; i < segments.length; i++) {
          let s = segments[i];
          if (page.consumePathSegment(s.path)) {
            let child = page.getChild(s.path);
            if (child) {
              return this.loadPage(page, child, segments, i + 1);
            } else {
              return throwError(`Nonexistent child of ${page.url}: ${s.path}`);
            }
          } else {
            page.url += `/${s.path}`;
          }
        }
        return of(componentRef);
      }));
  }

  ngOnDestroy() {
    super.ngOnDestroy();
    this.pageCreated.complete();
  }
}

export type PortofinoComponentDefinition = {
  name: string, defaultActionClass?: string, hideFromCreateNewPage?: boolean,
  computeSecurityCheckUrl?: (apiRoot: string, parent: Page, source: string) => string
};
export type PortofinoComponent = { type: Type<any> } & PortofinoComponentDefinition;

export function PortofinoComponent(info: PortofinoComponentDefinition) {
  return function(target) {
    if(console) {
      console.debug("Registering Portofino component " + info.name, info, target);
    }
    PageFactoryComponent.components[info.name] = Object.assign({ type: target }, info);
  };
}

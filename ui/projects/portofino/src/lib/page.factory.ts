import {Page, PageConfiguration, PageService} from "./page";
import {
  Component,
  ComponentFactoryResolver,
  ComponentRef,
  Injector,
  Input, OnInit, Optional,
  Type,
  ViewContainerRef
} from "@angular/core";
import {PortofinoService} from "./portofino.service";
import {HttpClient} from "@angular/common/http";
import {ActivatedRoute, Router} from "@angular/router";
import {AuthenticationService} from "./security/authentication.service";
import {Observable, of, throwError} from "rxjs";
import {PortofinoAppComponent} from "./portofino-app.component";
import {map, mergeMap} from "rxjs/operators";

@Component({
  selector: 'portofino-page',
  template: ''
})
export class PageFactoryComponent extends Page implements OnInit {

  @Input()
  parent: Page;
  @Input()
  embedded: boolean;
  @Input()
  segment: string;
  @Input()
  path: string;

  constructor(portofino: PortofinoService, http: HttpClient, router: Router, @Optional() route: ActivatedRoute,
              authenticationService: AuthenticationService,
              protected componentFactoryResolver: ComponentFactoryResolver, protected injector: Injector,
              protected viewContainerRef: ViewContainerRef) {
    super(portofino, http, router, route, authenticationService);
  }

  ngOnInit(): void {
    if(!this.path) {
      if(this.parent) {
        this.path = this.parent.path + '/' + this.segment;
      } else {
        this.path = this.segment;
      }
    }
    let configObservable: Observable<PageConfiguration>;
    if(this.configuration) {
      configObservable = of(this.configuration);
    } else {
      configObservable = this.loadPageConfiguration(this.path);
    }
    configObservable.pipe(mergeMap(config => this.create(config, this.path, this.parent))).subscribe(c => {
      const page = <Page>c.instance;
      page.embedded = this.embedded;
      if(this.parent) {
        page.baseUrl = page.parent.baseUrl + '/' + page.segment;
        page.url = page.parent.url + '/' + page.segment;
      } else {
        page.baseUrl = page.segment;
        page.url = page.segment;
      }
      page.initialize();
      this.viewContainerRef.clear();
      this.viewContainerRef.insert(c.hostView);
    });
  }

  create(config: PageConfiguration, path: string, parent: Page): Observable<ComponentRef<any>> {
    const componentType = PortofinoAppComponent.components[config.type];
    if (!componentType) {
      return throwError(`Unknown component type '${config.type}' for path '${path}'`);
    }
    let componentFactory = this.componentFactoryResolver.resolveComponentFactory(componentType);
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
    return component.prepare().pipe(map(_ => componentRef));
  }

}

import {
  AfterViewInit,
  Component,
  ComponentFactoryResolver, OnDestroy,
  OnInit, ViewChild
} from '@angular/core';
import {ActivatedRoute, Router, UrlSegment} from "@angular/router";
import {PortofinoAppComponent} from "./portofino-app.component";
import {HttpClient} from "@angular/common/http";
import {EmbeddedContentDirective, MainContentDirective} from "./content.directive";
import {Observable, Subscription} from "rxjs";
import {mergeMap} from "rxjs/operators";
import {PortofinoService} from "./portofino.service";
import {AuthenticationService} from "./security/authentication.service";
import {Page, PageChild, PageConfiguration, PageService} from "./page";

@Component({
  selector: 'portofino-page',
  templateUrl: './page.component.html',
  styleUrls: ['./page.component.css']
})
export class PageComponent implements AfterViewInit, OnInit, OnDestroy {
  @ViewChild(MainContentDirective)
  contentHost: MainContentDirective;
  @ViewChild(EmbeddedContentDirective)
  embeddedContentHost: EmbeddedContentDirective;

  protected readonly subscriptions: Subscription[] = [];

  constructor(public pageService: PageService,
              protected route: ActivatedRoute, protected http: HttpClient, protected router: Router,
              protected componentFactoryResolver: ComponentFactoryResolver,
              public portofino: PortofinoService,
              protected authenticationService: AuthenticationService) { }

  ngOnInit() {
    const reload = () => this.reloadPage();
    this.subscriptions.push(this.authenticationService.logins.subscribe(reload));
    this.subscriptions.push(this.authenticationService.logouts.subscribe(reload));
  }

  ngAfterViewInit() {
    this.subscriptions.push(this.route.url.subscribe(segment => {
      this.pageService.reset();
      this.loadPageInPath("", null, segment, 0, false);
    }));
  }

  ngOnDestroy() {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }

  protected reloadPage() {
    if (this.router.url && this.router.url != "/") {
      this.router.navigateByUrl(this.router.url);
    } else {
      window.location.reload(); //TODO
    }
  }

  protected loadPageInPath(path: string, parent: Page, segments: UrlSegment[], index: number, embed: boolean) {
    this.loadPage(path, parent, embed).subscribe(
      (page: Page) => {
        page.embedded = embed;
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
              this.pageService.notifyError(`Nonexistent child of ${page.url}: ${s.path}`);
              return;
            }
          } else {
            page.url += `/${s.path}`;
          }
        }
        //If we arrive here, there are no more children in the URL to process
        if(!embed) {
          this.pageService.notifyPage(page);
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
    this.pageService.notifyError(error);
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
      this.pageService.notifyError("Unknown component type: " + config.type);
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

class DummyPage extends Page {
  constructor(
    protected portofino: PortofinoService, protected http: HttpClient, protected router: Router,
    public authenticationService: AuthenticationService) {
    super(portofino, http, router, authenticationService);
  }
}

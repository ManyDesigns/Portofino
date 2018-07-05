import {Component, ComponentFactoryResolver, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, UrlSegment} from "@angular/router";
import {Page, PageConfiguration, PortofinoComponent} from "./portofino.component";
import {HttpClient} from "@angular/common/http";
import {ContentDirective} from "./content.directive";
import {Observable, Subscription} from "rxjs/index";
import {map} from "rxjs/operators";

@Component({
  selector: 'portofino-page',
  templateUrl: './page.component.html',
  styleUrls: ['./page.component.css']
})
export class PageComponent implements OnInit, OnDestroy {

  @ViewChild(ContentDirective)
  contentHost: ContentDirective;

  page: Page = null;
  error;

  protected subscription: Subscription;

  constructor(protected route: ActivatedRoute, protected http: HttpClient,
              protected componentFactoryResolver: ComponentFactoryResolver) { }

  ngOnInit() {
    this.subscription = this.route.url.subscribe(segment => {
      this.page = null;
      this.error = null;
      this.loadPageInPath("", null, segment, 0);
    });
  }

  ngOnDestroy() {
    if(this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  protected loadPageInPath(path: string, parent: Page, segments: UrlSegment[], index: number) {
    this.loadPage(path).subscribe(
      (page: Page) => {
        page.parent = parent;
        page.url = '/' + segments.slice(0, index).join('/');
        this.page = page;
        for(let i = index; i < segments.length; i++) {
          let s = segments[i];
          if (page.consumePathSegment(s.path)) {
            path += `/${s.path}`;
            this.loadPageInPath(path, page, segments, i + 1);
            return;
          }
        }
      },
      error => this.error = error);
  }

  protected loadPage(path: string): Observable<Page> {
    return this.http.get<PageConfiguration>(`pages${path}/config.json`).pipe(
      map(
        config => {
          const componentType = PortofinoComponent.components[config.type];
          if (!componentType) {
            this.error = Error("Unknown component type: " + config.type);
            return;
          }

          let componentFactory = this.componentFactoryResolver.resolveComponentFactory(componentType);

          let viewContainerRef = this.contentHost.viewContainerRef;
          viewContainerRef.clear();

          let componentRef = viewContainerRef.createComponent(componentFactory);
          const component = <Page>componentRef.instance;
          component.configuration = config;
          component.path = path;
          const lastIndexOf = path.lastIndexOf('/');
          if(lastIndexOf >= 0) {
            component.segment = path.substring(lastIndexOf + 1);
          } else {
            component.segment = path;
          }
          return component;
        }));
  }
}

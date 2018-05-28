import {Component, ComponentFactoryResolver, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, UrlSegment} from "@angular/router";
import {Page, PageConfiguration, PortofinoComponent} from "./portofino.component";
import {HttpClient} from "@angular/common/http";
import {ContentDirective} from "./content.directive";
import {Observable} from "rxjs/index";
import {map} from "rxjs/operators";
import {AuthenticationService} from "./security/authentication.service";

@Component({
  selector: 'portofino-page',
  templateUrl: './page.component.html',
  styleUrls: ['./page.component.css']
})
export class PageComponent implements OnInit {

  @ViewChild(ContentDirective)
  contentHost: ContentDirective;

  page: Page = null;
  error;

  constructor(protected route: ActivatedRoute, protected http: HttpClient,
              protected componentFactoryResolver: ComponentFactoryResolver) { }

  ngOnInit() {
    this.route.url.subscribe(segment => {
      this.page = null;
      this.error = null;
      this.loadPageInPath("", segment, 0);
    });
  }

  protected loadPageInPath(path: string, segment: UrlSegment[], index: number) {
    this.loadPage(path).subscribe(
      page => {
        page.parent = this.page;
        this.page = page;
        for(let i = index; i < segment.length; i++) {
          let s = segment[i];
          path += `/${s.path}`;
          if (this.page.consumePathSegment(s.path)) {
            this.loadPageInPath(path, segment, i + 1);
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

import {Component, ComponentFactoryResolver, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, UrlSegment} from "@angular/router";
import {Page, PageConfiguration, PortofinoComponent} from "./portofino.component";
import {HttpClient} from "@angular/common/http";
import {ContentDirective} from "./content.directive";
import {Observable} from "rxjs/index";
import {map} from "rxjs/operators";

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

  constructor(private route: ActivatedRoute, private http: HttpClient,
              private componentFactoryResolver: ComponentFactoryResolver) { }

  ngOnInit() {
    this.route.url.subscribe(segment => {
      this.loadPageInPath("", segment, 0);
    });
  }

  protected loadPageInPath(path: string, segment: UrlSegment[], index: number) {
    this.loadPage(path).subscribe(
      page => {
        page.parent = this.page;
        if (page.parent) {
          page.parent.configuration.children.forEach(child =>
            child.path = `${page.parent.path}/${child.path}`
          );
        }
        this.page = page;
        segment.slice(index, segment.length).forEach(s => {
          path += `/${s.path}`;
          index++;
          if (this.page.consumePathFragment(s.path)) {
            this.loadPageInPath(path, segment, index);
            return;
          } else if (index == segment.length) {
            //page.show()
            return;
          }
        });
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
          return component;
        }));
  }
}

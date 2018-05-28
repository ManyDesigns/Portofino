import {Component, ComponentFactoryResolver, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
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

  page: Page;
  error;

  constructor(private route: ActivatedRoute, private http: HttpClient,
              private componentFactoryResolver: ComponentFactoryResolver) { }

  ngOnInit() {
    this.route.url.subscribe(segment => {
      let path = "";
      this.setPage(path);
      segment.forEach(s => {
        if(this.error) {
          return;
        }
        path += `/${s.path}`;
        if(this.page.consumePathFragment(s.path)) {
          this.setPage(path);
        }
      });
    });
  }

  protected setPage(path: string) {
    this.loadPage(path).subscribe(
      page => {
        page.path = path;
        page.parent = this.page;
        this.page = page;
        if(page.parent) {
          this.page.configuration.children.forEach(child =>
            child.path = `${page.parent.path}/${child.path}`
          );
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
          return component;
        }));
  }
}

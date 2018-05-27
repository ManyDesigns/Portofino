import {Component, ComponentFactoryResolver, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {PageConfiguration, PortofinoComponent} from "./portofino.component";
import {HttpClient} from "@angular/common/http";
import {ContentDirective} from "./content.directive";

@Component({
  selector: 'portofino-page',
  templateUrl: './page.component.html',
  styleUrls: ['./page.component.css']
})
export class PageComponent implements OnInit {

  @ViewChild(ContentDirective)
  contentHost: ContentDirective;

  parentPage;
  path = "";
  page: PageConfiguration;
  error;

  constructor(private route: ActivatedRoute, private http: HttpClient,
              private componentFactoryResolver: ComponentFactoryResolver) { }

  ngOnInit() {
    this.route.url.subscribe(segment => {
      this.path = "";
      this.parentPage = null;
      segment.forEach(s => {
        this.parentPage = { path: this.path };
        this.path += `/${s.path}`;
      });
      this.loadPage();
    });
  }

  protected loadPage() {
    if(this.parentPage) {
      this.http.get<PageConfiguration>(`pages${this.parentPage.path}/config.json`).subscribe(config => {
        config.children.forEach(c => {
          c.path = `${this.parentPage.path}/${c.path}`;
        });
        this.parentPage.configuration = config;
      });
    }
    this.http.get<PageConfiguration>(`pages${this.path}/config.json`).subscribe(config => {
      const componentType = PortofinoComponent.components[config.type];
      if (!componentType) {
        this.error = Error("Unknown component type: " + config.type);
        return;
      }

      let componentFactory = this.componentFactoryResolver.resolveComponentFactory(componentType);

      let viewContainerRef = this.contentHost.viewContainerRef;
      viewContainerRef.clear();

      let componentRef = viewContainerRef.createComponent(componentFactory);
      componentRef.instance['configuration'] = config;
      this.page = config;
    }, error => {
      this.error = error;
    });
  }

}

import {Component, ComponentFactoryResolver, Input, OnInit, ViewChild} from '@angular/core';
import {PortofinoService} from "./portofino.service";
import {ContentDirective} from "./content.directive";
import {HttpClient} from "@angular/common/http";

@Component({
  selector: 'portofino-app',
  templateUrl: './portofino.component.html',
  styleUrls: ['./portofino.component.css']
})
export class PortofinoComponent implements OnInit {

  static components: any = {};

  title = 'Portofino UI';
  @Input()
  apiPath: string = 'http://localhost:8080/demo-tt/api/';

  @ViewChild(ContentDirective)
  contentHost: ContentDirective;

  path = "";
  page: PageConfiguration;

  constructor(
    public portofinoService: PortofinoService, private componentFactoryResolver: ComponentFactoryResolver,
    private http: HttpClient) {}

  ngOnInit(): void {
    if(this.apiPath) {
      this.portofinoService.apiPath = this.apiPath;
    }
    this.loadPage();
  }

  protected loadPage() {
    this.http.get<PageConfiguration>(`pages${this.path}/config.json`).subscribe(config => {
      const componentType = PortofinoComponent.components[config.type];
      if (!componentType) {
        throw new Error("Unknown component type: " + config.type);
      }

      let componentFactory = this.componentFactoryResolver.resolveComponentFactory(componentType);

      let viewContainerRef = this.contentHost.viewContainerRef;
      viewContainerRef.clear();

      let componentRef = viewContainerRef.createComponent(componentFactory);
      componentRef.instance['configuration'] = config;
      this.page = config;
    });
  }
}

export class PageConfiguration {
  type: string;
  title: string;
  children: PageChild[];
}

export class PageChild {
  path: string;
  title: string;
}

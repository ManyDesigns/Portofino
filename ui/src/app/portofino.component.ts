import {Component, ComponentFactoryResolver, Input, OnInit, ViewChild} from '@angular/core';
import {PortofinoService} from "./portofino.service";
import {ContentDirective} from "./content.directive";
import {HttpClient} from "@angular/common/http";
import {AuthenticationService} from "./security/authentication.service";

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

  constructor(public portofinoService: PortofinoService, public authenticationService: AuthenticationService) {}

  ngOnInit(): void {
    if(this.apiPath) {
      this.portofinoService.apiPath = this.apiPath;
    }
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

export abstract class Page {

  configuration: PageConfiguration & any;
  path: string;
  url: string;
  segment: string;
  parent: Page;

  consumePathSegment(fragment: string): boolean {
    return true;
  }

  get children(): PageChild[] {
    return this.configuration.children
  }
}

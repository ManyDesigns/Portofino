import {Component, Input, OnInit} from '@angular/core';
import {PortofinoService} from "./portofino.service";
import {AuthenticationService} from "./security/authentication.service";

@Component({
  selector: 'portofino-app',
  templateUrl: './portofino-app.component.html',
  styleUrls: ['./portofino-app.component.css']
})
export class PortofinoAppComponent implements OnInit {

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

export function PortofinoComponent(info: { name: string }) {
  return function(target) {
    PortofinoAppComponent.components[info.name] = target;
  };
}

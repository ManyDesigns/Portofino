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
  apiRoot: string = 'http://localhost:8080/demo-tt/api/'; //TODO @Input() does not appear to work!

  constructor(public portofino: PortofinoService, public authenticationService: AuthenticationService) {}

  ngOnInit(): void {
    if(this.apiRoot) {
      this.portofino.defaultApiRoot = this.apiRoot;
      this.portofino.localApiPath = null;
    }
    this.portofino.init();
  }
}

export function PortofinoComponent(info: { name: string }) {
  return function(target) {
    PortofinoAppComponent.components[info.name] = target;
  };
}

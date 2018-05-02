import {Component, Input, OnInit} from '@angular/core';
import {PortofinoService} from "./portofino.service";

@Component({
  selector: 'portofino-app',
  templateUrl: './portofino.component.html',
  styleUrls: ['./portofino.component.css']
})
export class PortofinoComponent implements OnInit {
  title = 'Portofino UI';
  @Input() apiPath: string = 'http://localhost:8080/demo-tt/api/';

  config = {
    path: 'projects/P4/tickets'
  };

  constructor(public portofinoService: PortofinoService) {}

  ngOnInit(): void {
    if(this.apiPath) {
      this.portofinoService.apiPath = this.apiPath;
    }
  }
}

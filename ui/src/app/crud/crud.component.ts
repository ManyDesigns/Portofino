import {Component, Input, OnInit} from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import {PortofinoService} from "../portofino.service";
import {tap} from "rxjs/operators";
import ClassAccessor = portofino.accessors.ClassAccessor;

@Component({
  selector: 'portofino-crud',
  templateUrl: './crud.component.html',
  styleUrls: ['./crud.component.css']
})
export class CrudComponent implements OnInit {

  @Input() config: any;

  classAccessor: ClassAccessor;

  constructor(private http: HttpClient, public portofinoService: PortofinoService) { }

  ngOnInit() {
    this.http.get<ClassAccessor>(this.portofinoService.apiPath + this.config.path + '/:classAccessor').subscribe(
      classAccessor => this.classAccessor = classAccessor
    );
  }

}

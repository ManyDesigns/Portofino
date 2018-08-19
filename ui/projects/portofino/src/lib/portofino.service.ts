import {Injectable, OnInit} from '@angular/core';
import {HttpClient} from "@angular/common/http";

@Injectable()
export class PortofinoService implements OnInit {

  defaultApiRoot = 'http://localhost:8080/';
  apiRoot: string;
  localApiPath = '/portofino';

  constructor(public http: HttpClient) { }

  ngOnInit(): void {
    this.init();
  }

  init(): void {
    if(!this.localApiPath) {
      this.fallbackInit();
      return;
    }
    this.http.get<ApiInfo>(this.localApiPath).subscribe(response => {
      this.apiRoot = response.apiRoot;
    }, error => {
      this.fallbackInit();
    });
  }

  private fallbackInit() {
    this.localApiPath = null;
    this.apiRoot = this.defaultApiRoot;
  }
}

class ApiInfo {
  apiRoot: string;
}

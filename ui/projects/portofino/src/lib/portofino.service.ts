import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";

@Injectable()
export class PortofinoService {

  defaultApiRoot = 'http://localhost:8080/';
  apiRoot: string;
  localApiPath = 'portofino';

  constructor(public http: HttpClient) { }

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

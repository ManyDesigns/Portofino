import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";

@Injectable()
export class PortofinoService {

  defaultApiRoot = 'http://localhost:8080/';
  apiRoot: string;
  sideNavOpen: boolean;
  localApiPath = 'portofino';
  loginPath = 'login';

  constructor(public http: HttpClient) { }

  init(): void {
    if(!this.localApiPath) {
      this.fallbackInit();
      return;
    }
    this.http.get<ApiInfo>(this.localApiPath).subscribe(response => {
      this.apiRoot = this.sanitizeApiRoot(response.apiRoot);
      if(response.loginPath) {
        let loginPath = response.loginPath;
        if(loginPath.startsWith('/')) {
          loginPath = loginPath.substring(1);
        }
        this.loginPath = loginPath;
      }
    }, error => {
      this.fallbackInit();
    });
  }

  private fallbackInit() {
    this.localApiPath = null;
    this.apiRoot = this.sanitizeApiRoot(this.defaultApiRoot);
  }

  private sanitizeApiRoot(apiRoot) {
    if (!apiRoot.endsWith('/')) {
      apiRoot += '/';
    }
    return apiRoot;
  }

  get localApiAvailable() {
    return !!this.localApiPath;
  }

  saveConfiguration(path: string, config: any) {
    if(!this.localApiAvailable) {
      throw "Local Portofino API not available"
    }
    return this.http.put(`${this.localApiPath}/pages/${path}?loginPath=${this.loginPath}`, config)
  }

  public toggleSidenav(){
    this.sideNavOpen=!this.sideNavOpen;
  }
}

class ApiInfo {
  apiRoot: string;
  loginPath: string;
}

import {EventEmitter, Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {TranslateService} from "@ngx-translate/core";
import {TRANSLATIONS_EN} from "./i18n/en";
import {TRANSLATIONS_IT} from "./i18n/it";
import {BehaviorSubject} from "rxjs";

@Injectable()
export class PortofinoService {

  defaultApiRoot = 'http://localhost:8080/';
  apiRoot: string;
  localApiPath = 'portofino';
  loginPath = 'login';
  sideNavPosition: SideNavPosition = 'page';
  sideNavOpen: boolean;

  readonly DEFAULT_LANGUAGE = 'en';
  readonly languages = {};
  readonly languageChange = new EventEmitter<Language>();

  constructor(public http: HttpClient, protected translate: TranslateService) {
    this.setupTranslateService();
  }

  configureLanguage(lang: Language) {
    this.languages[lang.key] = lang;
    if(lang.translations) {
      this.translate.setTranslation(lang.key, lang.translations, true);
    }
  }

  removeLanguage(key: string) {
    delete this.languages[key];
    this.translate.resetLang(key);
  }

  get languageKeys() {
    const keys = [];
    for(let k in this.languages) {
      keys.push(k);
    }
    return keys;
  }

  get currentLanguage(): Language {
    return this.languages[this.translate.currentLang];
  }

  set currentLanguage(lang: Language) {
    this.translate.use(lang.key);
    this.languageChange.emit(lang);
  }

  protected setupTranslateService() {
    this.translate.setDefaultLang(this.DEFAULT_LANGUAGE);
    this.configureLanguage({ key: 'en', name: 'English', translations: TRANSLATIONS_EN });
    this.configureLanguage({ key: 'it', name: 'Italiano', translations: TRANSLATIONS_IT });
    this.translate.use(this.translate.getBrowserLang());
  }

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

  saveConfiguration(path: string, pageConfiguration: any, actionConfiguration: any, actionPath: string) {
    if(!this.localApiAvailable) {
      throw "Local Portofino API not available"
    }
    let data = new FormData();
    data.append("pageConfiguration", JSON.stringify(pageConfiguration));
    data.append("actionConfiguration", JSON.stringify(actionConfiguration));
    data.append("actionPath", actionPath);
    return this.http.put(`${this.localApiPath}/pages/${path}?loginPath=${this.loginPath}`, data);
  }

  public toggleSidenav(){
    this.sideNavOpen=!this.sideNavOpen;
  }
}

class ApiInfo {
  apiRoot: string;
  loginPath: string;
}

export declare type SideNavPosition = 'body' | 'page' | undefined;

export declare type Language = { key: string, name: string, translations?: Object };

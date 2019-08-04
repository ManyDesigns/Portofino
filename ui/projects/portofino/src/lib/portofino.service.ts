import {EventEmitter, Inject, Injectable, InjectionToken, TemplateRef} from '@angular/core';
import {HttpClient, HttpEvent, HttpEventType, HttpHandler, HttpInterceptor, HttpRequest} from "@angular/common/http";
import {TranslateService} from "@ngx-translate/core";
import { DateAdapter } from "@angular/material/core";
import {DatetimeAdapter} from "@mat-datetimepicker/core";
import {WebStorageService} from "ngx-store";
import {Observable} from "rxjs";
import {catchError, map} from "rxjs/operators";

export const LOCALE_STORAGE_SERVICE = new InjectionToken('Locale Storage');
export const LOCALES = new InjectionToken('Locales');

@Injectable()
export class PortofinoService {

  applicationName: string;
  defaultApiRoot = 'http://localhost:8080/';
  apiRoot: string;
  localApiPath = 'portofino';
  loginPath = 'login';
  sideNavPosition: SideNavPosition = 'page';
  sideNavOpen = true;
  upstairsLink = "/portofino-upstairs";
  callsInProgress = 0;

  readonly DEFAULT_LOCALE = 'en';
  readonly localeDefinitions = {};
  readonly localeChange = new EventEmitter<Locale>();
  readonly templates: { [name: string]: { template: TemplateRef<any>, description?: string }} = {};

  constructor(public http: HttpClient, protected translate: TranslateService,
              @Inject(LOCALE_STORAGE_SERVICE) protected storage: WebStorageService,
              @Inject(LOCALES) locales: Locale[],
              protected dateAdapter: DateAdapter<any>, protected datetimeAdapter: DatetimeAdapter<any>) {
    this.setupTranslateService(locales);
  }

  configureLocale(lang: Locale) {
    this.localeDefinitions[lang.key] = lang;
    if(lang.translations) {
      this.translate.setTranslation(lang.key, lang.translations, true);
    }
  }

  removeLanguage(key: string) {
    delete this.localeDefinitions[key];
    this.translate.resetLang(key);
  }

  get locales() {
    const keys = [];
    for(let k in this.localeDefinitions) {
      keys.push(k);
    }
    return keys;
  }

  get currentLocale(): Locale {
    return this.localeDefinitions[this.translate.currentLang];
  }

  set currentLocale(lang: Locale) {
    this.setLocale(lang.key);
    this.localeChange.emit(lang);
  }

  protected setLocale(locale) {
    this.translate.use(locale);
    this.dateAdapter.setLocale(locale);
    this.datetimeAdapter.setLocale(locale);
    this.storage.set('locale', locale);
  }

  protected setupTranslateService(locales: Locale[]) {
    this.translate.setDefaultLang(this.DEFAULT_LOCALE);
    locales.forEach(l => { this.configureLocale(l); });
    this.setLocale(this.getInitialLocale());
  }

  protected getInitialLocale() {
    let locale = this.storage.get('locale');
    const predicate = e => e == locale;
    if (!this.locales.find(predicate)) {
      locale = this.translate.getBrowserLang();
    }
    if (!this.locales.find(predicate)) {
      locale = this.translate.getDefaultLang();
    }
    if (!this.locales.find(predicate)) {
      locale = this.locales[0];
    }
    return locale;
  }

  init(): void {
    if(!this.localApiPath) {
      this.fallbackInit();
      return;
    }
    this.http.get<ApiInfo>(this.localApiPath).subscribe(response => {
      this.apiRoot = this.sanitizeApiRoot(response.apiRoot);
      if(response.loginPath) {
        this.loginPath = this.sanitizeLoginPath(response.loginPath);
      } else {
        this.initLoginPath();
      }
    }, error => {
      this.fallbackInit();
    });
  }

  private initLoginPath() {
    this.http.get<any>(this.apiRoot + ':description').subscribe(response => {
      if (response.loginPath) {
        this.loginPath = this.sanitizeLoginPath(response.loginPath);
      }
    }); //TODO warn about failed init in case of error because only reloading the page will potentially fix it.
  }

  private sanitizeLoginPath(loginPath) {
    if (loginPath.startsWith('/')) {
      loginPath = loginPath.substring(1);
    }
    return loginPath;
  }

  private fallbackInit() {
    this.localApiPath = null;
    this.apiRoot = this.sanitizeApiRoot(this.defaultApiRoot);
    this.initLoginPath();
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

  public toggleSidenav(){
    this.sideNavOpen=!this.sideNavOpen;
  }
}

class ApiInfo {
  apiRoot: string;
  loginPath: string;
}

export declare type SideNavPosition = 'body' | 'page' | undefined;

export declare type Locale = { key: string, name: string, translations?: Object };

@Injectable()
export class ProgressInterceptor implements HttpInterceptor {

  constructor(protected portofino: PortofinoService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    let added = false;
    return next.handle(req).pipe(map(e => {
      if(e.type == HttpEventType.Response) {
        if(added) {
          this.portofino.callsInProgress--;
        }
      } else if(!added && e.type != HttpEventType.User) {
        this.portofino.callsInProgress++;
        added = true;
      }
      return e;
    }), catchError((e) => {
      if(added) {
        this.portofino.callsInProgress--;
      }
      throw e;
    }));
  }
}

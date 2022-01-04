import {EventEmitter, Inject, Injectable, InjectionToken, Injector, TemplateRef} from '@angular/core';
import {HttpClient, HttpEvent, HttpEventType, HttpHandler, HttpInterceptor, HttpRequest} from "@angular/common/http";
import {TranslateService} from "@ngx-translate/core";
import {DateAdapter} from "@angular/material/core";
import {Observable} from "rxjs";
import {catchError, map} from "rxjs/operators";
import {WebStorageService} from "./storage/storage.services";
import {NO_REFRESH_TOKEN_HEADER} from "./security/authentication.headers";
import {AuthenticationStrategy} from "./security/authentication.service";

export const LOCALE_STORAGE_SERVICE = new InjectionToken('Locale Storage');
export const LOCALES = new InjectionToken('Locales');

export type TemplateDescriptor = { template: TemplateRef<any>, description: string, sections: string[] };

@Injectable()
export class PortofinoService {

  applicationName: string;
  defaultApiRoot = 'http://localhost:8080/';
  apiRoot: string;
  localApiPath = 'portofino';
  upstairsLink = "/portofino-upstairs";
  callsInProgress = 0;

  readonly DEFAULT_LOCALE = 'en';
  readonly localeDefinitions = {};
  readonly localeChange = new EventEmitter<Locale>();
  readonly templates: { [name: string]: TemplateDescriptor} = {};

  constructor(public http: HttpClient, protected translate: TranslateService,
              @Inject(LOCALE_STORAGE_SERVICE) protected storage: WebStorageService,
              @Inject(LOCALES) locales: Locale[],
              protected dateAdapter: DateAdapter<any>,
              protected injector: Injector) {
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
    //Avoid refreshing the token on startup as this might hit the wrong login action
    const headers = {};
    headers[NO_REFRESH_TOKEN_HEADER] = true;
    this.http.get<ApiInfo>(this.localApiPath, { headers: headers }).subscribe(response => {
      this.apiRoot = this.sanitizeApiRoot(response.apiRoot);
      this.injector.get(AuthenticationStrategy).init({
        apiRoot: this.apiRoot
      });
      if(response.disableAdministration) {
        this.localApiPath = null; //Disable local API
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
}

export class ApiInfo {
  apiRoot: string;
  disableAdministration?: boolean = false;
}

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

@Injectable()
export class ApiVersionInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    let request = req.clone({
      headers: req.headers.append("X-Portofino-API-Version", "5.2")
    });
    return next.handle(request);
  }
}

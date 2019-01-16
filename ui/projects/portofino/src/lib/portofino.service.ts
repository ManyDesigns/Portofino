import {EventEmitter, Inject, Injectable, InjectionToken} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {TranslateService} from "@ngx-translate/core";
import {TRANSLATIONS_EN} from "./i18n/en";
import {TRANSLATIONS_IT} from "./i18n/it";
import {DateAdapter} from "@angular/material";
import {DatetimeAdapter} from "@mat-datetimepicker/core";
import {WebStorageService} from "ngx-store";

export const LOCALE_STORAGE_SERVICE = new InjectionToken('Locale Storage');

@Injectable()
export class PortofinoService {

  defaultApiRoot = 'http://localhost:8080/';
  apiRoot: string;
  localApiPath = 'portofino';
  loginPath = 'login';
  sideNavPosition: SideNavPosition = 'page';
  sideNavOpen: boolean;

  readonly DEFAULT_LOCALE = 'en';
  readonly localeDefinitions = {};
  readonly localeChange = new EventEmitter<Locale>();

  constructor(public http: HttpClient, protected translate: TranslateService,
              @Inject(LOCALE_STORAGE_SERVICE) protected storage: WebStorageService,
              protected dateAdapter: DateAdapter<any>, protected datetimeAdapter: DatetimeAdapter<any>) {
    this.setupTranslateService();
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

  protected setupTranslateService() {
    this.translate.setDefaultLang(this.DEFAULT_LOCALE);
    this.configureLocale({ key: 'en', name: 'English', translations: TRANSLATIONS_EN });
    this.configureLocale({ key: 'it', name: 'Italiano', translations: TRANSLATIONS_IT });
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

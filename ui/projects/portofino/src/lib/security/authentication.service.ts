import {EventEmitter, Inject, Injectable, InjectionToken} from '@angular/core';
import {
  HttpClient,
  HttpEvent, HttpHandler, HttpHeaders, HttpInterceptor, HttpParams, HttpRequest
} from "@angular/common/http";
import {MatDialog, MatDialogRef} from "@angular/material";
import {Observable, throwError} from "rxjs";
import {catchError, map, mergeMap, share} from "rxjs/operators";
import {PortofinoService} from "../portofino.service";
import {NotificationService} from "../notifications/notification.service";
import {WebStorageService} from "ngx-store";
import {TranslateService} from "@ngx-translate/core";

export const LOGIN_COMPONENT = new InjectionToken('Login Component');
export const TOKEN_STORAGE_SERVICE = new InjectionToken('JSON Web Token Storage');

@Injectable()
export class AuthenticationService {

  dialogRef: MatDialogRef<any>;
  currentUser: UserInfo;
  retryUnauthenticatedOnSessionExpiration = true;
  readonly logins = new EventEmitter<UserInfo>();
  readonly logouts = new EventEmitter<void>();

  constructor(private http: HttpClient, protected dialog: MatDialog,
              @Inject(TOKEN_STORAGE_SERVICE) protected storage: WebStorageService,
              private portofino: PortofinoService, @Inject(LOGIN_COMPONENT) protected loginComponent,
              protected notifications: NotificationService, protected translate: TranslateService) {
    const userInfo = this.storage.get('user');
    if(userInfo) {
      this.currentUser = new UserInfo(userInfo.displayName, userInfo.administrator, userInfo.groups);
    }
  }

  request(req: HttpRequest<any>, observable: Observable<HttpEvent<any>>): Observable<HttpEvent<any>> {
    return observable.pipe(catchError((error) => {
      if (error.status === 401) {
        const hasToken = !!this.jsonWebToken;
        this.removeAuthenticationInfo();
        if(hasToken || !this.retryUnauthenticatedOnSessionExpiration) {
          req = req.clone({ headers: req.headers.delete("Authorization") });
          return this.doHttpRequest(req).pipe(map(result => {
            this.translate.get("You have been logged out because your session has expired.")
              .pipe(mergeMap(m => this.notifications.warn(m)))
              .subscribe();
            return result;
          }));
        } else {
          return this.askForCredentials().pipe(
            map(result => {
              if (!result) {
                throw new Error("User declined login");
              }
            }),
            mergeMap(() => this.doHttpRequest(this.withAuthenticationHeader(req))));
        }
      } else if(error.status === 403) {
        this.translate.get("You do not have the permission to do that!")
          .pipe(mergeMap(m => this.notifications.error(m)))
          .subscribe();
      }
      return throwError(error);
    }), share());
  }

  protected doHttpRequest(req) {
    return this.http.request(req);
  }

  protected askForCredentials() {
    if(!this.dialogRef) {
      this.dialogRef = this.dialog.open(this.loginComponent);
    }
    return this.dialogRef.afterClosed().pipe(map(result => {
      this.dialogRef = null;
      if (result && result.jwt) {
        this.setAuthenticationInfo(result);
        return result;
      } else {
        return null;
      }
    }));
  }

  public showLoginDialog() {
    this.askForCredentials().subscribe();
  }

  protected removeAuthenticationInfo() {
    this.storage.remove('jwt');
    this.storage.remove('user');
    this.currentUser = null;
  }

  protected setAuthenticationInfo(result) {
    this.storage.set('jwt', result.jwt);
    this.storage.set('user', {
      displayName: result.displayName,
      administrator: result.administrator,
      groups: result.groups
    });
    this.currentUser = new UserInfo(result.displayName, result.administrator, result.groups);
    this.logins.emit(this.currentUser);
  }

  withAuthenticationHeader(req: HttpRequest<any>) {
    if(!this.jsonWebToken) {
      return req;
    }
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${this.jsonWebToken}`
      }
    });
    return req;
  }

  public get jsonWebToken() {
    return this.storage.get('jwt');
  }

  login(username, password) {
    const headers = new HttpHeaders()
      .set('Content-Type', 'application/x-www-form-urlencoded')
      .set(NO_AUTH_HEADER, 'true');
    return this.http.post(
      this.loginPath,
      new HttpParams({fromObject: {"username": username, "password": password}}),
      {headers: headers}
    );
  }

  get loginPath() {
    return `${this.portofino.apiRoot}${this.portofino.loginPath}`;
  }

  logout() {
    const url = `${this.loginPath}`;
    this.http.delete(url).subscribe(() => {
      this.removeAuthenticationInfo();
      this.logouts.emit();
    });
  }

  get isAdmin() {
    return this.currentUser && this.currentUser.administrator
  }
}

@Injectable()
export class AuthenticationInterceptor implements HttpInterceptor {

  constructor(protected authenticationService: AuthenticationService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    req = this.authenticationService.withAuthenticationHeader(req);
    if(req.headers.has(NO_AUTH_HEADER)) {
      req = req.clone({ headers: req.headers.delete(NO_AUTH_HEADER) });
      return next.handle(req);
    } else {
      return this.authenticationService.request(req, next.handle(req));
    }
  }
}

export class UserInfo {
  constructor(public displayName: string, public administrator: boolean, public groups: string[]) {}
}

export const NO_AUTH_HEADER = "portofino-no-auth";

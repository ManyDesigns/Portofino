import {EventEmitter, Inject, Injectable, InjectionToken} from '@angular/core';
import {
  HttpClient,
  HttpEvent, HttpHandler, HttpHeaders, HttpInterceptor, HttpParams, HttpRequest, HttpResponse
} from "@angular/common/http";
import {MatDialog} from "@angular/material/dialog";
import {Observable, throwError} from "rxjs";
import {catchError, map, mergeMap, share} from "rxjs/operators";
import {PortofinoService} from "../portofino.service";
import {NotificationService} from "../notifications/notification.services";
import {WebStorageService} from "ngx-store";
import {TranslateService} from "@ngx-translate/core";
import moment from 'moment-with-locales-es6';

export const LOGIN_COMPONENT = new InjectionToken('Login Component');
export const CHANGE_PASSWORD_COMPONENT = new InjectionToken('Change Password Component');
export const RESET_PASSWORD_COMPONENT = new InjectionToken('Reset Password Component');
export const TOKEN_STORAGE_SERVICE = new InjectionToken('JSON Web Token Storage');

@Injectable()
export class AuthenticationService {

  credentialsObservable: Observable<any>;
  currentUser: UserInfo;
  retryUnauthenticatedOnSessionExpiration = true;
  lastRenew = moment(0);
  renewAfterSeconds = 600;
  readonly logins = new EventEmitter<UserInfo>();
  readonly logouts = new EventEmitter<void>();
  readonly declinedLogins = new EventEmitter<void>();

  constructor(private http: HttpClient, protected dialog: MatDialog,
              @Inject(TOKEN_STORAGE_SERVICE) protected storage: WebStorageService,
              private portofino: PortofinoService, protected notifications: NotificationService,
              protected translate: TranslateService,
              @Inject(LOGIN_COMPONENT) protected loginComponent,
              @Inject(CHANGE_PASSWORD_COMPONENT) protected changePasswordComponent,
              @Inject(RESET_PASSWORD_COMPONENT) protected resetPasswordComponent) {
    const userInfo = this.storage.get('user');
    if(userInfo) {
      this.currentUser = new UserInfo(userInfo.displayName, userInfo.administrator, userInfo.groups);
    }
  }

  request(req: HttpRequest<any>, httpHandler: HttpHandler): Observable<HttpEvent<any>> {
    const requestWithAuth = (original) => httpHandler.handle(original).pipe(catchError((error) => {
      if (error.status === 401) {
        return this.authenticate(original);
      } else if (error.status === 403) {
        this.notifications.error(this.translate.get("You do not have the permission to do that!"));
      }
      return throwError(error);
    }), share());

    if (req.headers.has(NO_RENEW_HEADER)) {
      req = req.clone({headers: req.headers.delete(NO_RENEW_HEADER)});
    } else if (!!this.jsonWebToken && this.portofino.apiRoot &&
      moment().diff(this.lastRenew, 'seconds') > this.renewAfterSeconds) {
      this.lastRenew = moment();
      //The body here is to work around CORS requests failing with an empty body (TODO investigate)
      this.http.request(this.withAuthenticationHeader(
        new HttpRequest<any>("POST", `${this.loginPath}/:renew-token`, "renew", {
          headers: new HttpHeaders().set(NO_RENEW_HEADER, 'true'), responseType: 'text'
        }))).subscribe(
        event => {
          if(event instanceof HttpResponse) {
            if(event.status == 200) {
              const token = event.body;
              this.setJsonWebToken(token);
            } else {
              this.notifications.error(this.translate.get("Failed to renew authentication token"));
            }
          }
        },
        () => this.notifications.error(this.translate.get("Failed to renew authentication token")));
    }
    return requestWithAuth(req);
  }

  protected setJsonWebToken(token) {
    this.storage.set('jwt', token);
  }

  protected authenticate(req: HttpRequest<any>) {
    const hasToken = !!this.jsonWebToken;
    this.removeAuthenticationInfo();
    if (hasToken || !this.retryUnauthenticatedOnSessionExpiration) {
      req = req.clone({headers: req.headers.delete("Authorization")});
      return this.http.request(req).pipe(map(result => {
        this.notifications.error(this.translate.get("You have been logged out because your session has expired."));
        return result;
      }));
    } else {
      return this.askForCredentials().pipe(
        map(result => {
          if (!result) {
            this.declinedLogins.emit();
            throw new LoginDeclinedException("User declined login");
          }
        }),
        mergeMap(() => this.http.request(this.withAuthenticationHeader(req))));
    }
  }

  protected askForCredentials() {
    if(this.credentialsObservable) {
      return this.credentialsObservable;
    }
    const dialogRef = this.dialog.open(this.loginComponent);
    this.credentialsObservable = dialogRef.afterClosed().pipe(map(result => {
      this.credentialsObservable = null;
      if (result && result.jwt) {
        this.lastRenew = moment();
        this.setAuthenticationInfo(result);
        return result;
      } else {
        return null;
      }
    }));
    return this.credentialsObservable;
  }

  public showLoginDialog() {
    this.askForCredentials().subscribe();
  }

  public showChangePasswordDialog() {
    return this.dialog.open(this.changePasswordComponent);
  }

  showResetPasswordDialog(token: string) {
    return this.dialog.open(this.resetPasswordComponent, {
      data: { token: token }
    });
  }

  protected removeAuthenticationInfo() {
    this.storage.remove('jwt');
    this.storage.remove('user');
    this.currentUser = null;
    this.lastRenew = moment(0);
  }

  protected setAuthenticationInfo(result) {
    this.setJsonWebToken(result.jwt);
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

  changePassword(oldPassword, newPassword) {
    const headers = new HttpHeaders()
      .set('Content-Type', 'application/x-www-form-urlencoded')
      .set(NO_AUTH_HEADER, 'true');
    return this.http.put(
      `${this.loginPath}/password`,
      new HttpParams({fromObject: {"oldPassword": oldPassword, "newPassword": newPassword}}),
      {headers: headers}
    );
  }

  sendForgotPasswordEmail(email, loginPageUrl) {
    return this.http.post(`${this.loginPath}/:send-reset-password-email`,{
      email: email, loginPageUrl: loginPageUrl, siteNameOrAddress: this.portofino.applicationName
    });
  }

  resetPassword(newPassword, token) {
    return this.http.post(`${this.loginPath}/:reset-password`,{
      newPassword: newPassword, token: token
    });
  }

  signup(user: any, confirmationUrl) {
    const headers = new HttpHeaders().set(NO_AUTH_HEADER, 'true');
    const params = new FormData();
    params.append('portofino:confirmationUrl', confirmationUrl);
    params.append('portofino:siteNameOrAddress', this.portofino.applicationName);
    for(let k in user) {
      params.append(k, user[k]);
    }
    return this.http.post(
      `${this.loginPath}/user`, params,{headers: headers});
  }

  confirmSignup(token) {
    return this.http.post(`${this.loginPath}/user/:confirm`,{ token: token });
  }

  get loginPath() {
    return `${this.portofino.apiRoot}${this.portofino.loginPath}`;
  }

  logout() {
    const url = `${this.loginPath}`;
    this.http.delete(url, {
      headers: new HttpHeaders().set(NO_RENEW_HEADER, 'true')
    }).subscribe(() => {
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
      return this.authenticationService.request(req, next);
    }
  }
}

export class UserInfo {
  constructor(public displayName: string, public administrator: boolean, public groups: string[]) {}
}

export const NO_AUTH_HEADER = "X-Portofino-no-auth";
export const NO_RENEW_HEADER = "X-Portofino-no-renew";

export class LoginDeclinedException extends Error {
  constructor(public message: string) {
    super();
  }
}

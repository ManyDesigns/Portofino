import {EventEmitter, Inject, Injectable, InjectionToken} from '@angular/core';
import {HttpClient, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from "@angular/common/http";
import {Observable, of, throwError} from "rxjs";
import {catchError, map, mergeMap, share} from "rxjs/operators";
import {NotificationService} from "../notifications/notification.services";
import {TranslateService} from "@ngx-translate/core";
import {WebStorageService} from "../storage/storage.services";
import {NO_AUTH_HEADER, NO_REFRESH_TOKEN_HEADER} from "./authentication.headers";
import jwt_decode from 'jwt-decode';
import { DateTime } from "luxon";
import {ApiInfo} from "../portofino.service";

export const TOKEN_STORAGE_SERVICE = new InjectionToken('JSON Web Token Storage');

@Injectable()
export class AuthenticationService {

  credentialsObservable: Observable<any>;
  currentUser: UserInfo;
  retryUnauthenticatedOnSessionExpiration = true;
  tokenExpirationThresholdMs = 10 * 60 * 1000; //Ten minutes before the token expires, refresh it

  readonly logins = new EventEmitter<UserInfo>();
  readonly logouts = new EventEmitter<void>();
  readonly declinedLogins = new EventEmitter<void>();

  constructor(public strategy: AuthenticationStrategy, protected http: HttpClient,
              @Inject(TOKEN_STORAGE_SERVICE) protected storage: WebStorageService,
              protected notifications: NotificationService,
              protected translate: TranslateService) {
    this.strategy.authenticationService = this;
    const userInfo = this.storage.get('user');
    if(userInfo) {
      this.currentUser = new UserInfo(userInfo.userId, userInfo.displayName, userInfo.administrator, userInfo.groups);
    }
  }

  request(req: HttpRequest<any>, httpHandler: HttpHandler): Observable<HttpEvent<any>> {
    if (req.headers.has(NO_REFRESH_TOKEN_HEADER)) {
      req = req.clone({headers: req.headers.delete(NO_REFRESH_TOKEN_HEADER)});
    }
    return httpHandler.handle(req).pipe(catchError((error) => {
      if (error.status === 401) {
        return this.retry(req);
      } else if (error.status === 403) {
        this.notifications.error(this.translate.get("You do not have the permission to do that!"));
      }
      return throwError(error);
    }), share());
  }

  setJsonWebToken(token) {
    this.storage.set('jwt', token);
  }

  protected retry(req: HttpRequest<any>) {
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
        mergeMap(() => this.withAuthenticationHeader(req)),
        mergeMap(req => this.http.request(req)));
    }
  }

  protected askForCredentials() {
    if(this.credentialsObservable) {
      return this.credentialsObservable;
    }
    this.credentialsObservable = this.strategy.askForCredentials().pipe(map(result => {
      this.credentialsObservable = null;
      if (result && result.jwt) {
        this.setAuthenticationInfo(result);
        return result;
      } else {
        return null;
      }
    }));
    return this.credentialsObservable;
  }

  public goToAuthentication() {
    this.askForCredentials().subscribe();
  }

  public goToChangePassword() {
    return this.strategy.goToChangePassword();
  }

  goToResetPassword(token: string) {
    return this.strategy.goToResetPassword(token);
  }

  protected removeAuthenticationInfo() {
    this.storage.remove('jwt');
    this.storage.remove('user');
    this.currentUser = null;
  }

  protected setAuthenticationInfo(result) {
    this.setJsonWebToken(result.jwt);
    this.storage.set('user', {
      userId: result.userId,
      displayName: result.displayName,
      administrator: result.administrator,
      groups: result.groups
    });
    this.currentUser = new UserInfo(result.userId, result.displayName, result.administrator, result.groups);
    this.logins.emit(this.currentUser);
  }

  withAuthenticationHeader(req: HttpRequest<any>): Observable<HttpRequest<any>> {
    if(!this.jsonWebToken) {
      return of(req);
    }
    let token: any;
    try {
      token = jwt_decode(this.jsonWebToken);
    } catch (e) {
      this.notifications.error(this.translate.get("Invalid authentication token, you've been logged out"));
      this.removeAuthenticationInfo();
      if(console && console.error) {
        console.error(e);
      }
      if(this.retryUnauthenticatedOnSessionExpiration) {
        return of(req);
      } else {
        return this.askForCredentials().pipe(
          map(result => {
            if (!result) {
              this.declinedLogins.emit();
              throw new LoginDeclinedException("User declined login");
            }
          }),
          mergeMap(() => this.withAuthenticationHeader(req)));
      }
    }
    const requestWithHeader = r => r.clone({
      setHeaders: {
        Authorization: `Bearer ${this.jsonWebToken}`
      }
    });
    if (req.headers.has(NO_REFRESH_TOKEN_HEADER)) {
      return of(requestWithHeader(req));
    }

    if(token.exp &&
      DateTime.now() < (DateTime.fromMillis(token.exp * 1000)) &&
      DateTime.now() > (DateTime.fromMillis(token.exp * 1000 - this.tokenExpirationThresholdMs))) {
      return this.strategy.refreshToken(this.jsonWebToken).pipe(map(token => {
        this.setJsonWebToken(token);
        return requestWithHeader(req);
      }), catchError(() => {
        this.notifications.error(this.translate.get("Failed to refresh access token"));
        return requestWithHeader(req);
      }));
    } else {
      return of(requestWithHeader(req));
    }
  }

  public get jsonWebToken() {
    return this.storage.get('jwt');
  }

  confirmSignup(token) {
    return this.strategy.confirmSignup(token);
  }

  logout() {
    this.strategy.logout().subscribe(() => {
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
    return this.authenticationService.withAuthenticationHeader(req).pipe(
      mergeMap(req => {
        if(req.headers.has(NO_AUTH_HEADER)) {
          req = req.clone({ headers: req.headers.delete(NO_AUTH_HEADER) });
          return next.handle(req);
        } else {
          return this.authenticationService.request(req, next);
        }
      })
    );
  }
}

export class UserInfo {
  constructor(public userId: string, public displayName: string, public administrator: boolean, public groups: string[]) {}
}

export class LoginDeclinedException extends Error {
  constructor(public message: string) {
    super();
  }
}

export abstract class AuthenticationStrategy {
  protected authentication: AuthenticationService;

  abstract askForCredentials(): Observable<any>;

  abstract goToChangePassword();

  abstract goToResetPassword(token: string);

  abstract refreshToken(token: string): Observable<string>;

  abstract logout(): Observable<any>;

  abstract confirmSignup(token: string): Observable<any>;

  abstract get supportsSelfRegistration(): boolean;

  init(response: ApiInfo) {}

  set authenticationService(auth) {
    this.authentication = auth;
  }
}

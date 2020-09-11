import {EventEmitter, Inject, Injectable, InjectionToken} from '@angular/core';
import {HttpClient, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from "@angular/common/http";
import {Observable, throwError} from "rxjs";
import {catchError, map, mergeMap, share} from "rxjs/operators";
import {NotificationService} from "../notifications/notification.services";
import {TranslateService} from "@ngx-translate/core";
import {WebStorageService} from "../storage/storage.services";
import {NO_AUTH_HEADER} from "./authentication.headers";

export const TOKEN_STORAGE_SERVICE = new InjectionToken('JSON Web Token Storage');

@Injectable()
export class AuthenticationService {

  credentialsObservable: Observable<any>;
  currentUser: UserInfo;
  retryUnauthenticatedOnSessionExpiration = true;
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
    const requestWithAuth = (original) => httpHandler.handle(original).pipe(catchError((error) => {
      if (error.status === 401) {
        return this.authenticate(original);
      } else if (error.status === 403) {
        this.notifications.error(this.translate.get("You do not have the permission to do that!"));
      }
      return throwError(error);
    }), share());

    return requestWithAuth(this.strategy.preprocess(req));
  }

  setJsonWebToken(token) {
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

  abstract preprocess<T>(req: HttpRequest<T>): HttpRequest<T>;

  abstract logout(): Observable<any>;

  abstract confirmSignup(token: string);

  set authenticationService(auth) {
    this.authentication = auth;
  }
}

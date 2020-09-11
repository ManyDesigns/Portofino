import {AuthenticationStrategy} from "../authentication.service";
import {Inject, Injectable, InjectionToken} from "@angular/core";
import {MatDialog, MatDialogRef} from "@angular/material/dialog";
import {Observable} from "rxjs";
import {NO_RENEW_HEADER} from "../authentication.headers";
import moment from "moment-with-locales-es6";
import {HttpClient, HttpHeaders, HttpRequest, HttpResponse} from "@angular/common/http";
import {PortofinoService} from "../../portofino.service";
import {NotificationService} from "../../notifications/notification.services";
import {TranslateService} from "@ngx-translate/core";
import {map} from "rxjs/operators";

export const LOGIN_COMPONENT = new InjectionToken('Login Component');
export const CHANGE_PASSWORD_COMPONENT = new InjectionToken('Change Password Component');
export const RESET_PASSWORD_COMPONENT = new InjectionToken('Reset Password Component');

@Injectable()
export class InAppAuthenticationStrategy extends AuthenticationStrategy {
  protected lastRenew = moment(0);
  renewAfterSeconds = 600;

  constructor(
    protected portofino: PortofinoService, protected http: HttpClient,
    protected notifications: NotificationService, protected translate: TranslateService,
    protected dialog: MatDialog,
    @Inject(LOGIN_COMPONENT) protected loginComponent,
    @Inject(CHANGE_PASSWORD_COMPONENT) protected changePasswordComponent,
    @Inject(RESET_PASSWORD_COMPONENT) protected resetPasswordComponent) {
    super();
  }

  askForCredentials(): Observable<any> {
    const dialogRef = this.dialog.open(this.loginComponent);
    return dialogRef.afterClosed().pipe(map(r => {
      if(r && r.jwt) {
        this.lastRenew = moment();
      }
      return r;
    }));
  }

  goToChangePassword(): MatDialogRef<unknown> {
    return this.dialog.open(this.changePasswordComponent);
  }

  goToResetPassword(token: string): MatDialogRef<unknown> {
    return this.dialog.open(this.resetPasswordComponent, {
      data: { token: token }
    });
  }

  preprocess<T>(req: HttpRequest<T>): HttpRequest<T> {
    if (req.headers.has(NO_RENEW_HEADER)) {
      req = req.clone({headers: req.headers.delete(NO_RENEW_HEADER)});
    } else if (!!this.authentication.jsonWebToken && this.portofino.apiRoot &&
      moment().diff(this.lastRenew, 'seconds') > this.renewAfterSeconds) {
      this.lastRenew = moment();
      //The body here is to work around CORS requests failing with an empty body (TODO investigate)
      this.http.request(this.authentication.withAuthenticationHeader(
        new HttpRequest<any>("POST", `${this.loginPath}/:renew-token`, "renew", {
          headers: new HttpHeaders().set(NO_RENEW_HEADER, 'true'), responseType: 'text'
        }))).subscribe(
        event => {
          if (event instanceof HttpResponse) {
            if (event.status == 200) {
              const token = event.body;
              this.authentication.setJsonWebToken(token);
            } else {
              this.notifications.error(this.translate.get("Failed to renew authentication token"));
            }
          }
        },
        () => this.notifications.error(this.translate.get("Failed to renew authentication token")));
    }
    return req;
  }

  logout(): Observable<any> {
    this.lastRenew = moment(0);
    const url = `${this.loginPath}`;
    return this.http.delete(url, {
      headers: new HttpHeaders().set(NO_RENEW_HEADER, 'true')
    });
  }

  confirmSignup(token: string) {
    this.http.post(`${this.loginPath}/user/:confirm`,{ token: token });
  }

  get loginPath() {
    return `${this.portofino.apiRoot}${this.portofino.loginPath}`;
  }


}

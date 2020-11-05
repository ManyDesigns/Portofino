import {AuthenticationStrategy} from "../authentication.service";
import {Inject, Injectable, InjectionToken} from "@angular/core";
import {MatDialog, MatDialogRef} from "@angular/material/dialog";
import {Observable} from "rxjs";
import {NO_REFRESH_TOKEN_HEADER} from "../authentication.headers";
import {HttpClient, HttpHeaders, HttpRequest, HttpResponse} from "@angular/common/http";
import {ApiInfo, PortofinoService} from "../../portofino.service";
import {NotificationService} from "../../notifications/notification.services";
import {TranslateService} from "@ngx-translate/core";
import {filter, map, mergeMap} from "rxjs/operators";

export const LOGIN_COMPONENT = new InjectionToken('Login Component');
export const CHANGE_PASSWORD_COMPONENT = new InjectionToken('Change Password Component');
export const RESET_PASSWORD_COMPONENT = new InjectionToken('Reset Password Component');

@Injectable()
export class InAppAuthenticationStrategy extends AuthenticationStrategy {

  supportsSelfRegistration = false;

  constructor(
    protected portofino: PortofinoService, protected http: HttpClient,
    protected notifications: NotificationService, protected translate: TranslateService,
    protected dialog: MatDialog,
    @Inject(LOGIN_COMPONENT) protected loginComponent,
    @Inject(CHANGE_PASSWORD_COMPONENT) protected changePasswordComponent,
    @Inject(RESET_PASSWORD_COMPONENT) protected resetPasswordComponent) {
    super();
  }

  init(response: ApiInfo) {
    super.init(response);
    this.http.get<any>(`${this.loginPath}/capabilities`).subscribe(capabilities => {
      this.supportsSelfRegistration = capabilities.supportsSelfRegistration;
    });
  }

  askForCredentials(): Observable<any> {
    const dialogRef = this.dialog.open(this.loginComponent);
    return dialogRef.afterClosed();
  }

  goToChangePassword(): MatDialogRef<unknown> {
    return this.dialog.open(this.changePasswordComponent);
  }

  goToResetPassword(token: string): MatDialogRef<unknown> {
    return this.dialog.open(this.resetPasswordComponent, {
      data: { token: token }
    });
  }

  refreshToken(): Observable<string> {
    //The body here is to work around CORS requests failing with an empty body (TODO investigate)
    return this.authentication.withAuthenticationHeader(
      new HttpRequest<any>("POST", `${this.loginPath}/:refresh-token`, "renew", {
        headers: new HttpHeaders().set(NO_REFRESH_TOKEN_HEADER, 'true'), responseType: 'text'
      })).pipe(
        mergeMap(req => this.http.request(req)),
        filter(event => event instanceof HttpResponse),
        map(
          (event: HttpResponse<any>) => {
        if (event.status == 200) {
          return event.body as string;
        } else {
          throw "Failed to refresh access token";
        }
      }));
  }

  logout(): Observable<any> {
    const url = `${this.loginPath}`;
    return this.http.delete(url, {
      headers: new HttpHeaders().set(NO_REFRESH_TOKEN_HEADER, 'true')
    });
  }

  confirmSignup(token: string) {
    return this.http.post(`${this.loginPath}/user/:confirm`,{ token: token });
  }

  get loginPath() {
    return `${this.portofino.apiRoot}${this.portofino.loginPath}`;
  }


}

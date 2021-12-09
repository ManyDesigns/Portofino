import { Component, OnInit } from '@angular/core';
import {MatDialog, MatDialogRef} from "@angular/material/dialog";
import {AuthenticationService} from "../authentication.service";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {NotificationService} from "../../notifications/notification.services";
import {TranslateService} from "@ngx-translate/core";
import {SignupComponent} from "./signup.component";
import {ForgottenPasswordComponent} from "./forgotten-password.component";
import {ResetPasswordComponent} from "./reset-password.component";
import {HttpClient, HttpHeaders, HttpParams} from "@angular/common/http";
import {NO_AUTH_HEADER} from "../authentication.headers";
import {InAppAuthenticationStrategy} from "./in-app-authentication-strategy";

@Component({
  selector: 'portofino-login',
  templateUrl: '../../../../assets/security/login/login.component.html',
  styleUrls: ['../../../../assets/security/login/login.component.scss']
})
export class LoginComponent {

  loginForm: FormGroup;
  forgottenPasswordComponent = ForgottenPasswordComponent;
  resetPasswordComponent = ResetPasswordComponent;
  signupComponent = SignupComponent;

  constructor(public authenticationService: AuthenticationService,
              protected dialog: MatDialog, protected dialogRef: MatDialogRef<LoginComponent>,
              protected formBuilder: FormBuilder, protected notificationService: NotificationService,
              protected translate: TranslateService, protected http: HttpClient) {
    this.loginForm = this.formBuilder.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  login() {
    const username = this.loginForm.get('username').value;
    const password = this.loginForm.get('password').value;
    const headers = new HttpHeaders()
      .set('Content-Type', 'application/x-www-form-urlencoded')
      .set(NO_AUTH_HEADER, 'true');
    this.http.post(
      (this.authenticationService.strategy as InAppAuthenticationStrategy).loginPath,
      new HttpParams({fromObject: {"username": username, "password": password}}),
      {headers: headers}
    ).subscribe(
      result => {
        this.dialogRef.close(result);
        this.loginForm.get('password').setValue("");
      },
      () => {
        this.notificationService.error(this.translate.get("Login failed"));
        this.loginForm.get('password').setValue("");
      });
  }

  openForgotPasswordDialog() {
    this.dialog.open(this.forgottenPasswordComponent);
  }

  openSignUpDialog() {
    this.dialog.open(this.signupComponent);
  }

}


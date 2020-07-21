import { Component, OnInit } from '@angular/core';
import {MatDialog, MatDialogRef} from "@angular/material/dialog";
import {AuthenticationService} from "../authentication.service";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {NotificationService} from "../../notifications/notification.services";
import {TranslateService} from "@ngx-translate/core";
import {SignupComponent} from "./signup.component";
import {ForgottenPasswordComponent} from "./forgotten-password.component";
import {ResetPasswordComponent} from "./reset-password.component";

@Component({
  selector: 'portofino-login',
  templateUrl: '../../../../assets/security/login/login.component.html',
  styleUrls: ['../../../../assets/security/login/login.component.scss']
})
export class LoginComponent implements OnInit {

  loginForm: FormGroup;
  forgottenPasswordComponent = ForgottenPasswordComponent;
  resetPasswordComponent = ResetPasswordComponent;
  signupComponent = SignupComponent;

  constructor(protected dialog: MatDialog, protected dialogRef: MatDialogRef<LoginComponent>,
              protected authenticationService: AuthenticationService,
              protected formBuilder: FormBuilder, protected notificationService: NotificationService,
              protected translate: TranslateService) {
    this.loginForm = this.formBuilder.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  ngOnInit() {}

  login() {
    this.authenticationService.login(this.loginForm.get('username').value, this.loginForm.get('password').value).subscribe(
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


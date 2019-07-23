import { Component, OnInit } from '@angular/core';
import {MatDialog, MatDialogRef} from "@angular/material/dialog";
import {AuthenticationService} from "../authentication.service";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {NotificationService} from "../../notifications/notification.service";
import {TranslateService} from "@ngx-translate/core";
import {SignupComponent} from "./signup.component";
import {ForgottenPasswordComponent} from "./forgotten-password.component";
import {ResetPasswordComponent} from "./reset-password.component";

@Component({
  selector: 'portofino-login',
  template: `
    <h4 mat-dialog-title>{{ 'Sign in' | translate }}</h4>
    <mat-dialog-content>
      <form (submit)="login()" [formGroup]="loginForm">
        <mat-form-field>
          <input matInput placeholder="{{ 'Username' | translate }}" name="username" formControlName="username">
          <mat-error *ngIf="loginForm.get('username').hasError('required')" class="alert alert-danger">
            {{ 'Username is required.' | translate }}
          </mat-error>
        </mat-form-field>
        <mat-form-field>
          <input matInput placeholder="{{ 'Password' | translate }}" type="password" name="password" formControlName="password">
          <mat-error *ngIf="loginForm.get('password').hasError('required')" class="alert alert-danger">
            {{ 'Password is required.' | translate }}
          </mat-error>
        </mat-form-field>
        <button type="submit" style="display:none">{{ 'Sign in' | translate }}</button>
      </form>
      <hr />
      <a href="#" (click)="openForgotPasswordDialog(); $event.preventDefault();">{{ 'Forgot password?' | translate }}</a><br />
      <a href="#" (click)="openSignUpDialog(); $event.preventDefault();">{{ 'Sign up' | translate }}</a> {{ "if you don't have an account" | translate }}
    </mat-dialog-content>
    <mat-dialog-actions>
      <button mat-button (click)="login()" color="accent">{{ 'Sign in' | translate }}</button>
      <button mat-button mat-dialog-close>{{ 'Cancel' | translate }}</button>
    </mat-dialog-actions>`
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


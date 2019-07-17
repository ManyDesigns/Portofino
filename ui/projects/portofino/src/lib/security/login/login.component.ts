import { Component, OnInit } from '@angular/core';
import { MatDialogRef } from "@angular/material/dialog";
import {AuthenticationService} from "../authentication.service";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {NotificationService} from "../../notifications/notification.service";
import {TranslateService} from "@ngx-translate/core";
import {Location} from "@angular/common";

@Component({
  selector: 'portofino-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  loginForm: FormGroup;
  forgotPassword = false;
  forgotPasswordForm: FormGroup;

  constructor(protected dialogRef: MatDialogRef<LoginComponent>, protected authenticationService: AuthenticationService,
              protected formBuilder: FormBuilder, protected notificationService: NotificationService,
              protected translate: TranslateService, protected location: Location) {
    this.loginForm = this.formBuilder.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
    this.forgotPasswordForm = this.formBuilder.group({ email: ['', [Validators.required, Validators.email]] });
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

  sendForgotPasswordEmail() {
    this.authenticationService.sendForgotPasswordEmail(this.forgotPasswordForm.get('email').value,
      this.location.path(false) + "?token=TOKEN").subscribe(
      () => {
        this.notificationService.info(this.translate.get("Check your mailbox and follow the instructions."));
        this.toggleForgotPassword();
      }
    );
  }

  toggleForgotPassword() {
    this.forgotPassword = !this.forgotPassword;
  }

}


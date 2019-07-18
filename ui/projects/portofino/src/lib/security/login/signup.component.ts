import { Component, OnInit } from '@angular/core';
import {MatDialog, MatDialogRef} from "@angular/material/dialog";
import {AuthenticationService} from "../authentication.service";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {NotificationService} from "../../notifications/notification.service";
import {TranslateService} from "@ngx-translate/core";
import {Location} from "@angular/common";

@Component({
  selector: 'portofino-signup',
  template: `
    <h4 mat-dialog-title>{{ 'Sign up' | translate }}</h4>
    <mat-dialog-content>
      TODO
    </mat-dialog-content>
    <mat-dialog-actions>
      <button mat-button (click)="signUp()" color="accent">{{ 'Sign up' | translate }}</button>
      <button mat-button mat-dialog-close>{{ 'Cancel' | translate }}</button>
    </mat-dialog-actions>`
})
export class SignupComponent implements OnInit {

  loginForm: FormGroup;

  constructor(protected dialog: MatDialog, protected dialogRef: MatDialogRef<SignupComponent>,
              protected authenticationService: AuthenticationService,
              protected formBuilder: FormBuilder, protected notificationService: NotificationService,
              protected translate: TranslateService, protected location: Location) {
    this.loginForm = this.formBuilder.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  ngOnInit() {}

  signUp() {
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

}


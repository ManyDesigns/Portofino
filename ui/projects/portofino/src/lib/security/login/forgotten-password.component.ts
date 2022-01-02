import {Component, OnInit} from '@angular/core';
import {MatDialog, MatDialogRef} from "@angular/material/dialog";
import {AuthenticationService} from "../authentication.service";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {NotificationService} from "../../notifications/notification.services";
import {TranslateService} from "@ngx-translate/core";
import {Location} from "@angular/common";
import {PortofinoService} from "../../portofino.service";
import {HttpClient} from "@angular/common/http";
import {InAppAuthenticationStrategy} from "./in-app-authentication-strategy";

@Component({
  selector: 'portofino-forgotten-password',
  template: `
    <h4 mat-dialog-title>{{ 'Forgot password' | translate }}</h4>
    <mat-dialog-content>
      <form (submit)="sendForgotPasswordEmail()" [formGroup]="form">
        <mat-form-field>
          <input matInput placeholder="{{ 'Email' | translate }}" name="email" formControlName="email">
          <mat-error *ngIf="form.get('email').hasError('required')" class="alert alert-danger">
            {{ 'Email is required.' | translate }}
          </mat-error>
        </mat-form-field>
        <button type="submit" style="display:none">{{ 'Send the email' | translate }}</button>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions>
      <button mat-button (click)="sendForgotPasswordEmail()" color="accent">{{ 'Send the email' | translate }}</button>
      <button mat-button mat-dialog-close="">{{ 'Cancel' | translate }}</button>
    </mat-dialog-actions>`
})
export class ForgottenPasswordComponent {

  form: FormGroup;

  constructor(protected dialog: MatDialog, protected dialogRef: MatDialogRef<ForgottenPasswordComponent>,
              protected authenticationService: AuthenticationService,
              protected formBuilder: FormBuilder, protected notificationService: NotificationService,
              protected translate: TranslateService, protected location: Location,
              protected portofino: PortofinoService, protected http: HttpClient) {
    this.form = this.formBuilder.group({email: ['', [Validators.required, Validators.email]]});
  }

  sendForgotPasswordEmail() {
    return this.http.post(`${(this.authenticationService.strategy as InAppAuthenticationStrategy).loginPath}/:send-reset-password-email`,{
      email: this.form.get('email').value,
      loginPageUrl:
        //TODO alternative to document.baseUri?
        document.baseURI + "?resetPassword=x&token=TOKEN",
      siteNameOrAddress: this.portofino.applicationName
    }).subscribe(
      () => {
        this.notificationService.info(this.translate.get("Check your mailbox and follow the instructions."));
        this.dialogRef.close();
      }
    );
  }

}


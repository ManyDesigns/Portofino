import {Component, OnInit} from '@angular/core';
import {MatDialog, MatDialogRef} from "@angular/material/dialog";
import {AuthenticationService} from "../authentication.service";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {NotificationService} from "../../notifications/notification.services";
import {TranslateService} from "@ngx-translate/core";
import {Location} from "@angular/common";

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
export class ForgottenPasswordComponent implements OnInit {

  form: FormGroup;

  constructor(protected dialog: MatDialog, protected dialogRef: MatDialogRef<ForgottenPasswordComponent>,
              protected authenticationService: AuthenticationService,
              protected formBuilder: FormBuilder, protected notificationService: NotificationService,
              protected translate: TranslateService, protected location: Location) {
    this.form = this.formBuilder.group({email: ['', [Validators.required, Validators.email]]});
  }

  ngOnInit() {}

  sendForgotPasswordEmail() {
    this.authenticationService.sendForgotPasswordEmail(this.form.get('email').value,
      //TODO alternative to window.location?
      window.location.origin + this.location.normalize("/") + "?resetPassword=x&token=TOKEN").subscribe(
      () => {
        this.notificationService.info(this.translate.get("Check your mailbox and follow the instructions."));
        this.dialogRef.close();
      }
    );
  }

}


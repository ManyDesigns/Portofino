import {
  FormBuilder,
  FormControl,
  FormGroup,
  FormGroupDirective, NgForm,
  Validators
} from "@angular/forms";
import { ErrorStateMatcher } from "@angular/material/core";
import { MatDialogRef } from "@angular/material/dialog";
import {AuthenticationService} from "../authentication.service";
import {Component} from "@angular/core";
import {NotificationService} from "../../notifications/notification.services";
import {TranslateService} from "@ngx-translate/core";
import {samePasswordChecker} from "../../form";
import {HttpClient, HttpHeaders, HttpParams} from "@angular/common/http";
import {NO_AUTH_HEADER} from "../authentication.headers";
import {InAppAuthenticationStrategy} from "./in-app-authentication-strategy";

@Component({
  selector: 'portofino-change-password',
  template: `
    <h4 mat-dialog-title>{{ 'Change password' | translate }}</h4>
    <mat-dialog-content>
      <form (submit)="change()" [formGroup]="form">
        <mat-form-field>
          <input matInput placeholder="{{ 'Old password' | translate }}" type="password" name="oldPassword" formControlName="oldPassword">
          <mat-error *ngIf="form.get('oldPassword').hasError('required')" class="alert alert-danger">
            {{ 'Old password is required.' | translate }}
          </mat-error>
        </mat-form-field>
        <mat-form-field>
          <input matInput placeholder="{{ 'New password' | translate }}" type="password" name="newPassword" formControlName="newPassword">
          <mat-error *ngIf="form.get('newPassword').hasError('required')" class="alert alert-danger">
            {{ 'New password is required.' | translate }}
          </mat-error>
        </mat-form-field>
        <mat-form-field>
          <input matInput placeholder="{{ 'Confirm new password' | translate }}" type="password"
                 name="confirmNewPassword" formControlName="confirmNewPassword" [errorStateMatcher]="crossFieldErrorMatcher">
          <mat-error *ngIf="form.get('confirmNewPassword').hasError('required')" class="alert alert-danger">
            {{ 'Confirm new password is required.' | translate }}
          </mat-error>
          <mat-error *ngIf="form.hasError('passwordsDontMatch')"
                     class="cross-validation-error-message alert alert-danger">
            {{ "Passwords don't match." | translate }}
          </mat-error>
        </mat-form-field>
        <button type="submit" style="display:none">{{ 'Change password' | translate }}</button>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions>
      <button mat-button [disabled]="form.invalid" (click)="change()" color="accent">{{ 'Change password' | translate }}</button>
      <button mat-button mat-dialog-close>{{ 'Cancel' | translate }}</button>
    </mat-dialog-actions>
  `,
  styles: [`
    form > mat-form-field {
      margin: 0 5px;
    }`]
})
export class ChangePasswordComponent {

  readonly crossFieldErrorMatcher: ErrorStateMatcher = {
    isErrorState(control: FormControl | null, form: FormGroupDirective | NgForm | null): boolean {
      return control.dirty && form.invalid;
    }
  };
  form: FormGroup;

  constructor(protected dialogRef: MatDialogRef<ChangePasswordComponent>, protected authenticationService: AuthenticationService,
              protected formBuilder: FormBuilder, protected notificationService: NotificationService,
              protected translate: TranslateService, protected http: HttpClient) {
    this.form = this.formBuilder.group({
      oldPassword: ['', Validators.required],
      newPassword: ['', Validators.required],
      confirmNewPassword: ['', Validators.required]
    }, { validators: samePasswordChecker("newPassword", "confirmNewPassword") });
  }

  change() {
    this.changePassword(this.form.get('oldPassword').value, this.form.get('newPassword').value).subscribe(
      result => {
        this.dialogRef.close(result);
        this.notificationService.info(this.translate.get("Password successfully changed."));
      });
  }

  changePassword(oldPassword, newPassword) {
    const headers = new HttpHeaders()
      .set('Content-Type', 'application/x-www-form-urlencoded')
      .set(NO_AUTH_HEADER, 'true');
    return this.http.put(
      `${(this.authenticationService.strategy as InAppAuthenticationStrategy).loginPath}/password`,
      new HttpParams({fromObject: {"oldPassword": oldPassword, "newPassword": newPassword}}),
      {headers: headers}
    );
  }

}

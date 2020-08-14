import { Component, OnInit } from '@angular/core';
import {MatDialogRef} from "@angular/material/dialog";
import {AuthenticationService} from "../authentication.service";
import {FormGroup} from "@angular/forms";
import {NotificationService} from "../../notifications/notification.services";
import {TranslateService} from "@ngx-translate/core";
import {Location} from "@angular/common";
import {HttpClient} from "@angular/common/http";
import {Form} from "../../form";
import {ClassAccessor} from "../../class-accessor";

@Component({
  selector: 'portofino-signup',
  template: `
    <h4 mat-dialog-title>{{ 'Sign up' | translate }}</h4>
    <mat-dialog-content>
      <form (submit)="signUp()">
        <portofino-form [form]="signupFormDefinition" [controls]="signupForm"></portofino-form>
        <button type="submit" style="display:none">{{ 'Sign up' | translate }}</button>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions>
      <button mat-button (click)="signUp()" [disabled]="signupForm.invalid" color="accent">{{ 'Sign up' | translate }}</button>
      <button mat-button mat-dialog-close>{{ 'Cancel' | translate }}</button>
    </mat-dialog-actions>`
})
export class SignupComponent implements OnInit {

  signupFormDefinition: Form;
  signupForm = new FormGroup({});

  constructor(protected dialogRef: MatDialogRef<SignupComponent>, protected http: HttpClient,
              protected authenticationService: AuthenticationService, protected location: Location,
              protected notificationService: NotificationService,
              protected translate: TranslateService) {}

  ngOnInit() {
    this.http.get<ClassAccessor>(`${this.authenticationService.loginPath}/user/classAccessor`).subscribe(
      c => this.signupFormDefinition = Form.fromClassAccessor(c),
      () => this.dialogRef.close()
    );
  }

  signUp() {
    //TODO alternative to window.location?
    const confirmationUrl = window.location.origin + this.location.normalize("/") + "?confirmSignup=x&token=TOKEN";
    const user = {};
    for(var k in this.signupForm.value) {
      const value = this.signupForm.value[k];
      if(value && value.hasOwnProperty("password")) {
        user[k] = value.password;
        user[k + "_confirm"] = value.confirmPassword;
      } else {
        user[k] = value;
      }
    }
    this.authenticationService.signup(user, confirmationUrl).subscribe(
      result => {
        this.dialogRef.close(result);
      });
  }

}


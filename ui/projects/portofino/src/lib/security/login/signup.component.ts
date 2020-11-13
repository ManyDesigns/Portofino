import { Component, OnInit } from '@angular/core';
import {MatDialogRef} from "@angular/material/dialog";
import {AuthenticationService} from "../authentication.service";
import {FormGroup} from "@angular/forms";
import {NotificationService} from "../../notifications/notification.services";
import {TranslateService} from "@ngx-translate/core";
import {Location} from "@angular/common";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Form} from "../../form";
import {ClassAccessor, isDateProperty} from "../../class-accessor";
import {NO_AUTH_HEADER} from "../authentication.headers";
import {PortofinoService} from "../../portofino.service";
import {InAppAuthenticationStrategy} from "./in-app-authentication-strategy";

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

  signupClassAccessor: ClassAccessor;
  signupFormDefinition: Form;
  signupForm = new FormGroup({});

  constructor(protected dialogRef: MatDialogRef<SignupComponent>, protected http: HttpClient,
              protected authenticationService: AuthenticationService, protected location: Location,
              protected notificationService: NotificationService,
              protected translate: TranslateService, protected portofino: PortofinoService) {}

  ngOnInit() {
    this.http.get<ClassAccessor>(`${(this.authenticationService.strategy as InAppAuthenticationStrategy).loginPath}/user/classAccessor`).subscribe(
      c => {
        this.signupClassAccessor = c;
        this.signupFormDefinition = Form.fromClassAccessor(c);
      },
      () => this.dialogRef.close()
    );
  }

  signUp() {
    //TODO alternative to document.baseUri?
    const confirmationUrl = document.baseURI + "?confirmSignup=x&token=TOKEN";
    const user = {};
    for(const k in this.signupForm.value) {
      const value = this.signupForm.value[k];
      if(value && value.hasOwnProperty("password")) {
        user[k] = value.password;
        user[k + "_confirm"] = value.confirmPassword;
      } else if(value !== undefined) {
        if(isDateProperty(ClassAccessor.getProperty(this.signupClassAccessor, k))) {
          user[k] = value.valueOf();
        } else {
          user[k] = value;
        }
      }
    }
    this.signup(user, confirmationUrl).subscribe(
      result => {
        this.dialogRef.close(result);
        this.notificationService.info(this.translate.get("Sign up successful! We've sent a confirmation email at your address. Please click on the link in it to activate your new account."))
      });
  }

  signup(user: any, confirmationUrl) {
    const headers = new HttpHeaders().set(NO_AUTH_HEADER, 'true');
    const params = new FormData();
    params.append('portofino:confirmationUrl', confirmationUrl);
    params.append('portofino:siteNameOrAddress', this.portofino.applicationName);
    for(let k in user) {
      params.append(k, user[k]);
    }
    return this.http.post(
      `${(this.authenticationService.strategy as InAppAuthenticationStrategy).loginPath}/user`, params,{headers: headers});
  }

}


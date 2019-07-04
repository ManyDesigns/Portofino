import { Component, OnInit } from '@angular/core';
import { MatDialogRef } from "@angular/material/dialog";
import {AuthenticationService} from "../authentication.service";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";

@Component({
  selector: 'portofino-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  public loginRecord: FormGroup;
  formErrors: any;

  message: string;

  constructor(private dialogRef: MatDialogRef<LoginComponent>, private authenticationService: AuthenticationService,
              private builder: FormBuilder) {
    this.loginRecord = this.builder.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });

    this.formErrors = {
      username: {},
      password: {},
    };
  }

  ngOnInit() {
  }

  login() {
    const loginRecord = this.loginRecord;
    this.authenticationService.login(loginRecord.get('username').value, loginRecord.get('password').value).subscribe(
      result => {
        this.dialogRef.close(result);
        this.loginRecord.get('password').setValue("");
      },
      error => {
        this.message = "Login failed";
        this.loginRecord.get('password').setValue("");
      });
  }

}


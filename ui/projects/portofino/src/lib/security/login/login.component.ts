import { Component, OnInit } from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {PortofinoService} from "../../portofino.service";
import {MatDialogRef} from "@angular/material";
import {AuthenticationService} from "../authentication.service";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";

@Component({
  selector: 'portofino-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  public loginRecord:       FormGroup;
  formErrors: any;

  username: string;
  password: string;
  message: string;

  constructor(private dialogRef: MatDialogRef<LoginComponent>, private authenticationService: AuthenticationService,
              private builder: FormBuilder) {
    this.loginRecord = this.builder.group({
      username:              ['', Validators.required],
      password:                  ['', Validators.required]
    });

    this.formErrors = {
      username:        {},
      password:            {},
    };
  }

  ngOnInit() {
  }

  login() {
    this.authenticationService.login(this.username, this.password).subscribe(
      result => {
        this.dialogRef.close(result);
        this.password = "";
      },
      error => {
        this.message = "Login failed";
        this.password = "";
      });
  }

}


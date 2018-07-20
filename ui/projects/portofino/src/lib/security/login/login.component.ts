import { Component, OnInit } from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {PortofinoService} from "../../portofino.service";
import {MatDialogRef} from "@angular/material";
import {AuthenticationService} from "../authentication.service";

@Component({
  selector: 'portofino-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  username: string;
  password: string;
  message: string;

  constructor(private dialogRef: MatDialogRef<LoginComponent>, private authenticationService: AuthenticationService) {}

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


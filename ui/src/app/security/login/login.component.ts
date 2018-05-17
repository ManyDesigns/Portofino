import { Component, OnInit } from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {PortofinoService} from "../../portofino.service";
import {MatDialogRef} from "@angular/material";

@Component({
  selector: 'portofino-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  username: string;
  password: string;
  message: string;
  loginPath: string = "login";

  constructor(private dialogRef: MatDialogRef<LoginComponent>, private http: HttpClient, private portofino: PortofinoService) { }

  ngOnInit() {
  }

  login() {
    this.http.post(
      `${this.portofino.apiPath}${this.loginPath}`,
      new HttpParams({fromObject: {"username": this.username, "password": this.password}}),
      {headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
    ).subscribe(
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


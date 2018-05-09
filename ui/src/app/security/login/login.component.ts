import { Component, OnInit } from '@angular/core';
import {NgbActiveModal, NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {Observable} from "rxjs/Observable";
import {HttpClient, HttpParams} from "@angular/common/http";
import {PortofinoService} from "../../portofino.service";

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

  constructor(private modal: NgbActiveModal, private http: HttpClient, private portofino: PortofinoService) { }

  ngOnInit() {
  }

  login() {
    this.http.post(
      `${this.portofino.apiPath}${this.loginPath}`,
      new HttpParams({fromObject: {"username": this.username, "password": this.password}}),
      {headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
    ).subscribe(
      result => {
        this.modal.close(result);
        this.password = "";
      },
      error => {
        if(error.status == 401) {
          this.message = "Login failed";
        } else {
          this.modal.dismiss(error);
        }
        this.password = "";
      });
  }

  close() {
    this.modal.dismiss(new UserDeclinedLogin('User declined login'));
  }

}

export class UserDeclinedLogin {
  constructor(public message: string) {}
}

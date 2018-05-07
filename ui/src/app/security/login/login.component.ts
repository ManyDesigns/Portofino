import { Component, OnInit } from '@angular/core';
import {NgbActiveModal, NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {Observable} from "rxjs/Observable";
import {HttpClient, HttpParams} from "@angular/common/http";

@Component({
  selector: 'portofino-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  constructor(private modal: NgbActiveModal, private http: HttpClient) { }

  ngOnInit() {
  }

  login() {
    this.http.post(
      "http://localhost:8080/demo-tt/api/login",
      new HttpParams({fromObject: {"username": "alessiostalla@gmail.com", "password": "adminww"}}),
      {headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
    ).subscribe(result => this.modal.close(result), error => this.modal.dismiss(error));
  }

  close() {
    this.modal.dismiss('User declined login');
  }

}
